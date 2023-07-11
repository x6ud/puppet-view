package com.github.x6ud.puppetview;

import com.github.x6ud.puppetview.misc.ClipboardUtils;
import com.github.x6ud.puppetview.misc.MenuBuilder;
import com.github.x6ud.puppetview.window.ColorPicker;
import com.github.x6ud.puppetview.window.ReferenceImage;
import com.github.x6ud.puppetview.window.Screenshot;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().start();
    }

    private List<ReferenceImage> referenceImageList = new LinkedList<>();
    private String colorPickerMode = "html";

    private void start() throws Exception {
        PopupMenu popupMenu = MenuBuilder.popup();
        MenuBuilder.item(popupMenu, "From Screenshot", e -> {
            waitForPopupMenuToDisappear();
            new Screenshot(this::showImage).screenshot();
        });
        MenuBuilder.item(popupMenu, "Load Clipboard", e -> {
            BufferedImage image = ClipboardUtils.getImage();
            if (image != null) {
                showImage(image);
            }
        });
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Screenshot", e -> {
            waitForPopupMenuToDisappear();
            new Screenshot(ClipboardUtils::setImage).screenshot();
        });
        MenuBuilder.item(popupMenu, "Pick Color", e -> {
            waitForPopupMenuToDisappear();
            new ColorPicker(colorPickerMode, ClipboardUtils::setString).pickColor();
        });
        MenuBuilder.radioGroup(
                        MenuBuilder.menu(popupMenu, "Color Picker Mode"),
                        colorPickerMode,
                        mode -> {
                            colorPickerMode = mode;
                        })
                .item("#ffffff", "html")
                .item("0xffffff", "hex")
                .item("rgb(255, 255, 255)", "css")
                .item("255, 255, 255", "rgb");
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Flip All Horizontally", e -> referenceImageList.forEach(ReferenceImage::flipHorizontal));
        MenuBuilder.item(popupMenu, "Flip All Vertically", e -> referenceImageList.forEach(ReferenceImage::flipVertical));
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Show All", e -> setAllVisible(true));
        MenuBuilder.item(popupMenu, "Hide All", e -> setAllVisible(false));
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Close All", e -> closeAll());
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "About", e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/x6ud/puppet-view"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        popupMenu.addSeparator();
        MenuBuilder.item(popupMenu, "Exit", e -> System.exit(0));

        Frame popupMenuHolder = new Frame();
        popupMenuHolder.setType(Window.Type.UTILITY);
        popupMenuHolder.setUndecorated(true);
        popupMenuHolder.setResizable(false);
        popupMenuHolder.setVisible(true);

        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/tray-icon.png")),
                "PuppetView"
        );
        trayIcon.setPopupMenu(popupMenu);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    popupMenuHolder.add(popupMenu);
                    popupMenu.show(popupMenuHolder, e.getXOnScreen(), e.getYOnScreen());
                }
            }
        });
        SystemTray.getSystemTray().add(trayIcon);
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

}
