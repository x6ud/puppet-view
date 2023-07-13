package com.github.x6ud.puppetview;

import com.github.x6ud.puppetview.misc.ClipboardUtils;
import com.github.x6ud.puppetview.misc.MenuBuilder;
import com.github.x6ud.puppetview.window.ColorPicker;
import com.github.x6ud.puppetview.window.ReferenceImage;
import com.github.x6ud.puppetview.window.Screenshot;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().start();
    }

    private static final String WORKSPACE_EXTENSION = "pv-workspace";
    private static final String DEFAULT_WORKSPACE_PATH = "./auto-save." + WORKSPACE_EXTENSION;
    private static final FileFilter workspaceFilter = new FileNameExtensionFilter("Workspace", WORKSPACE_EXTENSION);

    private final List<ReferenceImage> referenceImageList = new ArrayList<>();
    private String colorPickerMode = "html";

    private void start() throws Exception {
        Frame mainFrame = new Frame();

        // create tray icon menu
        PopupMenu popupMenu = MenuBuilder.popup();
        MenuBuilder.item(popupMenu, "Screenshot", e -> {
            waitForPopupMenuToDisappear();
            new Screenshot(this::showImage).screenshot();
        });
        MenuBuilder.item(popupMenu, "Load Clipboard", e -> {
            for (BufferedImage image : ClipboardUtils.getImages()) {
                showImage(image);
            }
        });
        MenuBuilder.item(popupMenu, "Load Files...", e -> {
            FileDialog fileDialog = new FileDialog(mainFrame);
            fileDialog.setMultipleMode(true);
            fileDialog.setFile("*.png;*.jpg;*.jpeg;*.jfif;*.pjpeg;*.pjp;*.gif;*.bmp;*.tiff");
            fileDialog.setVisible(true);
            for (File file : fileDialog.getFiles()) {
                try {
                    showImage(ImageIO.read(file));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Flip All Horizontally", e -> referenceImageList.forEach(ReferenceImage::flipHorizontal));
        MenuBuilder.item(popupMenu, "Flip All Vertically", e -> referenceImageList.forEach(ReferenceImage::flipVertical));
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Show All", e -> setAllVisible(true));
        MenuBuilder.item(popupMenu, "Hide All", e -> setAllVisible(false));
        MenuBuilder.item(popupMenu, "Close All", e -> closeAll());
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Save Workspace...", e -> {
            // use JFileChooser rather than FileDialog, so it won't share a same default directory
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("./"));
            chooser.setSelectedFile(
                    new File("./" + new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()))
            );
            chooser.setFileFilter(workspaceFilter);
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getPath();
                if (!path.toLowerCase().endsWith("." + WORKSPACE_EXTENSION)) {
                    path = path + "." + WORKSPACE_EXTENSION;
                }
                saveWorkspace(path);
            }
        });
        MenuBuilder.item(popupMenu, "Load Workspace...", e -> {
            // use JFileChooser rather than FileDialog, so it won't share a same default directory
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("./"));
            chooser.setFileFilter(workspaceFilter);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                loadWorkspace(chooser.getSelectedFile().getPath());
            }
        });
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Screenshot to Clipboard", e -> {
            waitForPopupMenuToDisappear();
            new Screenshot(ClipboardUtils::setImage).screenshot();
        });
        MenuBuilder.item(popupMenu, "Color Picker", e -> {
            waitForPopupMenuToDisappear();
            new ColorPicker(colorPickerMode, ClipboardUtils::setString).pickColor();
        });
        MenuBuilder.radioGroup(
                        MenuBuilder.menu(popupMenu, "Color Format"),
                        colorPickerMode,
                        mode -> colorPickerMode = mode)
                .item("#ffffff", "html")
                .item("0xffffff", "hex")
                .item("rgb(255, 255, 255)", "css")
                .item("255, 255, 255", "rgb");
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "About", e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/x6ud/puppet-view"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Exit", e -> mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING)));

        // hide main window
        mainFrame.setType(Window.Type.UTILITY);
        mainFrame.setUndecorated(true);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);

        // create tray icon
        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/tray-icon.png")),
                "PuppetView"
        );
        trayIcon.setPopupMenu(popupMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mainFrame.add(popupMenu);
                    popupMenu.show(mainFrame, e.getXOnScreen(), e.getYOnScreen());
                }
            }
        });
        SystemTray.getSystemTray().add(trayIcon);

        // auto save current workspace before closing
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWorkspace(DEFAULT_WORKSPACE_PATH);
                System.exit(0);
            }
        });

        // load last workspace
        if (Files.exists(Paths.get(DEFAULT_WORKSPACE_PATH))) {
            loadWorkspace(DEFAULT_WORKSPACE_PATH);
        }

        // give jFileChooser a better look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // complete
        trayIcon.displayMessage("", "PuppetView is running.", TrayIcon.MessageType.NONE);
    }

    private void waitForPopupMenuToDisappear() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void showImage(BufferedImage image) {
        referenceImageList.add(new ReferenceImage(image, this::removeReferenceImage));
    }

    private void removeReferenceImage(ReferenceImage referenceImage) {
        referenceImageList.remove(referenceImage);
    }

    private void setAllVisible(boolean visible) {
        referenceImageList.forEach(referenceImage -> referenceImage.setVisible(visible));
    }

    private void closeAll() {
        Iterator<ReferenceImage> iterator = referenceImageList.iterator();
        while (iterator.hasNext()) {
            ReferenceImage referenceImage = iterator.next();
            referenceImage.close();
            iterator.remove();
        }
    }

    public static class WorkspaceImage implements Serializable {
        private static final long serialVersionUID = 1L;
        public transient BufferedImage image;
        public ReferenceImage.ImageState state;

        public WorkspaceImage(BufferedImage image, ReferenceImage.ImageState state) {
            this.image = image;
            this.state = state;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            ImageIO.write(image, "png", out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            image = ImageIO.read(in);
        }
    }

    private void saveWorkspace(String path) {
        try (FileOutputStream f = new FileOutputStream(path);
             ObjectOutputStream os = new ObjectOutputStream(f)
        ) {
            List<WorkspaceImage> workspace = new ArrayList<>();
            for (ReferenceImage item : referenceImageList) {
                workspace.add(new WorkspaceImage(item.getImage(), item.getImageState()));
            }
            os.writeObject(workspace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWorkspace(String path) {
        try (FileInputStream f = new FileInputStream(path);
             ObjectInputStream is = new ObjectInputStream(f)
        ) {
            @SuppressWarnings("unchecked")
            List<WorkspaceImage> workspace = (List<WorkspaceImage>) is.readObject();
            closeAll();
            for (WorkspaceImage record : workspace) {
                ReferenceImage image = new ReferenceImage(record.image, this::removeReferenceImage);
                image.setImageState(record.state);
                referenceImageList.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
