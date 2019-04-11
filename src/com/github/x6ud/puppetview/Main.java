package com.github.x6ud.puppetview;

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

    private Screenshot screenshot = new Screenshot(this::showImage);
    private List<ReferenceImage> referenceImageList = new LinkedList<>();

    private void start() throws Exception {
        PopupMenu popupMenu = new PopupMenuBuilder()
                .menuItem("Screenshot", e -> {
                    try {
                        Thread.sleep(500); // wait for popup menu to disappear
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    screenshot.screenshot();
                })
                .menuItem("Load Clipboard", e -> {
                    BufferedImage image = Clipboard.getImage();
                    if (image != null) {
                        showImage(image);
                    }
                })
                .separator()
                .menuItem("Show All", e -> setAllVisible(true))
                .menuItem("Hide All", e -> setAllVisible(false))
                .separator()
                .menuItem("Close All", e -> closeAll())
                .separator()
                .menuItem("About", e -> {
                    try {
                        Desktop.getDesktop().browse(new URI("https://github.com/x6ud/puppet-view"));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                })
                .separator()
                .menuItem("Exit", e -> System.exit(0))
                .get();

        Frame popupMenuHolder = new Frame();
        popupMenuHolder.setType(Window.Type.UTILITY);
        popupMenuHolder.setUndecorated(true);
        popupMenuHolder.setResizable(false);
        popupMenuHolder.setVisible(true);

        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/github/x6ud/puppetview/tray-icon.png")),
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
