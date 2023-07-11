package com.github.x6ud.puppetview.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ColorPicker extends JFrame {

    private static final int REPAINT_FPS = 60;
    private static final int WINDOW_SIZE = 105;
    private static final int CLIP_SIZE_HALF = 6;
    private static final int FONT_SIZE = 12;
    private static final int STRING_PAD_X = 2;
    private static final int STRING_PAD_Y = 2;

    private Font font = new Font("Arial", Font.PLAIN, FONT_SIZE);

    private Robot robot;

    private BufferedImage captureImage;
    private int screenWidth;
    private int screenHeight;

    private Timer repaintTimer;

    private Consumer<String> resultHandler;

    private String mode;
    private String result = "";
    private int targetX = 0;
    private int targetY = 0;
    private int windowX = 0;
    private int windowY = 0;

    public ColorPicker(String mode, Consumer<String> resultHandler) {
        this.mode = mode;
        this.resultHandler = resultHandler;

        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        repaintTimer = new Timer(1000 / REPAINT_FPS, e -> {
            update();
            repaint();
        });

        add(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                render((Graphics2D) g);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        close();
                        break;
                    case KeyEvent.VK_UP:
                        moveMouse(0, -1);
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveMouse(+1, 0);
                        break;
                    case KeyEvent.VK_DOWN:
                        moveMouse(0, +1);
                        break;
                    case KeyEvent.VK_LEFT:
                        moveMouse(-1, 0);
                        break;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accept();
            }
        });
    }

    private void moveMouse(int dx, int dy) {
        if (robot != null) {
            robot.mouseMove(targetX + dx, targetY + dy);
        }
    }

    public void pickColor() {
        try {
            // get screen capture
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            robot = new Robot();
            captureImage = robot.createScreenCapture(new Rectangle(screenSize));
            screenWidth = screenSize.width;
            screenHeight = screenSize.height;

            getContentPane().setPreferredSize(screenSize);
            pack();
            setVisible(true);
            setLocation(0, 0);

            getContentPane().setPreferredSize(screenSize);
            pack();
            setVisible(true);
            setLocation(0, 0);
            repaintTimer.start();
        } catch (AWTException e) {
            e.getMessage();
        }
    }

    private void close() {
        repaintTimer.stop();
        setVisible(false);
        super.dispose();
    }

    private void accept() {
        resultHandler.accept(result);
        close();
    }

    private void update() {
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        targetX = mouse.x;
        targetY = mouse.y;
        windowX = targetX + 10;
        windowY = targetY - WINDOW_SIZE / 2;
        if (windowX > screenWidth - WINDOW_SIZE) {
            windowX = targetX - WINDOW_SIZE - 10;
        }
        if (windowX < 0) {
            windowX = 0;
        }
        windowY = Math.max(0, Math.min(screenHeight - (WINDOW_SIZE + FONT_SIZE + STRING_PAD_Y * 2), windowY));

        if (captureImage == null) {
            return;
        }
        int rgb = captureImage.getRGB(targetX, targetY);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);
        switch (mode) {
            case "html": {
                result = String.format("#%1$02x%2$02x%3$02x", r, g, b);
            }
            break;
            case "hex": {
                result = String.format("0x%1$02x%2$02x%3$02x", r, g, b);
            }
            break;
            case "css": {
                result = "rgb(" + r + ", " + g + ", " + b + ")";
            }
            break;
            case "rgb": {
                result = r + ", " + g + ", " + b;
            }
            break;
        }
    }

    private void render(Graphics2D g) {
        if (captureImage == null) {
            return;
        }
        g.drawImage(captureImage, 0, 0, null);

        g.setColor(new Color(0, 0, 0));
        g.fillRect(windowX, windowY, WINDOW_SIZE, WINDOW_SIZE);
        g.drawImage(captureImage,
                windowX, windowY,
                windowX + WINDOW_SIZE, windowY + WINDOW_SIZE,
                targetX - CLIP_SIZE_HALF, targetY - CLIP_SIZE_HALF,
                targetX + CLIP_SIZE_HALF + 1, targetY + CLIP_SIZE_HALF + 1,
                null);

        g.setColor(new Color(0, 174, 255));
        g.drawLine(windowX, windowY, windowX + WINDOW_SIZE, windowY);
        g.drawLine(windowX, windowY, windowX, windowY + WINDOW_SIZE);
        g.drawLine(windowX, windowY + WINDOW_SIZE, windowX + WINDOW_SIZE, windowY + WINDOW_SIZE);
        g.drawLine(windowX + WINDOW_SIZE, windowY, windowX + WINDOW_SIZE, windowY + WINDOW_SIZE);
        g.drawLine(windowX, windowY + WINDOW_SIZE / 2, windowX + WINDOW_SIZE, windowY + WINDOW_SIZE / 2);
        g.drawLine(windowX + WINDOW_SIZE / 2, windowY, windowX + WINDOW_SIZE / 2, windowY + WINDOW_SIZE);

        g.fillRect(windowX, windowY + WINDOW_SIZE, WINDOW_SIZE + 1, FONT_SIZE + STRING_PAD_Y * 2);
        g.setFont(font);
        g.setColor(new Color(255, 255, 255));
        g.drawString(result, windowX + STRING_PAD_X, windowY + WINDOW_SIZE + FONT_SIZE);
    }
}
