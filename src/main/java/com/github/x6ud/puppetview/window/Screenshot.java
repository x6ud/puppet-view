package com.github.x6ud.puppetview.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class Screenshot extends JFrame {

    private static final int REPAINT_FPS = 60;
    private static final int RESIZE_INDICATOR_SIZE = 5;

    private BufferedImage captureImage;
    private int screenWidth;
    private int screenHeight;

    private Timer repaintTimer;

    private boolean draggingCropArea;
    private Point dragStart;
    private Point dragEnd;

    // crop area
    private boolean cropAreaExists;
    private int left;
    private int top;
    private int right;
    private int bottom;

    // move or resize
    private MouseBehavior nextClickBehavior = MouseBehavior.NORMAL;
    private MouseBehavior currMouseBehavior = MouseBehavior.NORMAL;
    private Point lastMouse;

    private Consumer<BufferedImage> resultHandler;

    private enum MouseBehavior {
        NORMAL, MOVE, RESIZE_NW, RESIZE_N, RESIZE_NE, RESIZE_W, RESIZE_E, RESIZE_SW, RESIZE_S, RESIZE_SE
    }

    public Screenshot(Consumer<BufferedImage> resultHandler) {
        this.resultHandler = resultHandler;

        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);

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


        // event listeners
        {
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            // exit on esc
                            close();
                            break;
                        case KeyEvent.VK_ENTER:
                            // accept on enter
                            accept();
                            break;
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (
                            cropAreaExists
                                    && currMouseBehavior == MouseBehavior.NORMAL
                                    && e.getButton() == MouseEvent.BUTTON1
                                    && e.getClickCount() == 2
                    ) {
                        // accept on double click
                        accept();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!cropAreaExists) {
                            // begin dragging crop area
                            draggingCropArea = true;
                            cropAreaExists = true;
                            dragStart = dragEnd = e.getPoint();
                        } else if (currMouseBehavior == MouseBehavior.NORMAL && nextClickBehavior != MouseBehavior.NORMAL) {
                            // begin move or resize
                            currMouseBehavior = nextClickBehavior;
                            lastMouse = e.getPoint();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            currMouseBehavior = MouseBehavior.NORMAL;

                            if (draggingCropArea) {
                                // stop crop area dragging
                                draggingCropArea = false;
                                dragEnd = e.getPoint();

                                if (top == bottom || left == right) {
                                    cropAreaExists = false;
                                }
                            }
                            break;

                        case MouseEvent.BUTTON3:
                            currMouseBehavior = MouseBehavior.NORMAL;

                            if (cropAreaExists) {
                                // reset crop area on right click
                                dragStart = dragEnd = null;
                                cropAreaExists = false;
                            } else {
                                // close on right click
                                close();
                            }
                            break;
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggingCropArea) {
                        // dragging crop area
                        dragEnd = e.getPoint();
                    } else if (currMouseBehavior != MouseBehavior.NORMAL && lastMouse != null) {
                        // move or resize
                        Point mouse = e.getPoint();
                        int dx = mouse.x - lastMouse.x;
                        int dy = mouse.y - lastMouse.y;
                        lastMouse = mouse;

                        if (currMouseBehavior == MouseBehavior.MOVE) { // move
                            int newLeft = left + dx;
                            int newTop = top + dy;
                            int newRight = right + dx;
                            int newBottom = bottom + dy;

                            if (newLeft < 0) {
                                newLeft = 0;
                                newRight = right + newLeft - left;
                            } else if (newRight > screenWidth) {
                                newRight = screenWidth;
                                newLeft = left + newRight - right;
                            }
                            if (newTop < 0) {
                                newTop = 0;
                                newBottom = bottom + newTop - top;
                            } else if (newBottom > screenHeight) {
                                newBottom = screenHeight;
                                newTop = top + newBottom - bottom;
                            }
                            left = newLeft;
                            top = newTop;
                            right = newRight;
                            bottom = newBottom;
                        } else { // resize
                            int newTop = top;
                            int newLeft = left;
                            int newBottom = bottom;
                            int newRight = right;
                            switch (currMouseBehavior) {
                                case RESIZE_NW:
                                    newLeft += dx;
                                    newTop += dy;
                                    break;
                                case RESIZE_N:
                                    newTop += dy;
                                    break;
                                case RESIZE_NE:
                                    newRight += dx;
                                    newTop += dy;
                                    break;
                                case RESIZE_W:
                                    newLeft += dx;
                                    break;
                                case RESIZE_E:
                                    newRight += dx;
                                    break;
                                case RESIZE_SW:
                                    newLeft += dx;
                                    newBottom += dy;
                                    break;
                                case RESIZE_S:
                                    newBottom += dy;
                                    break;
                                case RESIZE_SE:
                                    newRight += dx;
                                    newBottom += dy;
                                    break;
                            }
                            if (newTop > newBottom) {
                                int tmp = newTop;
                                newTop = newBottom;
                                newBottom = tmp;

                                switch (currMouseBehavior) {
                                    case RESIZE_NW:
                                        currMouseBehavior = MouseBehavior.RESIZE_SW;
                                        break;
                                    case RESIZE_N:
                                        currMouseBehavior = MouseBehavior.RESIZE_S;
                                        break;
                                    case RESIZE_NE:
                                        currMouseBehavior = MouseBehavior.RESIZE_SE;
                                        break;
                                    case RESIZE_SW:
                                        currMouseBehavior = MouseBehavior.RESIZE_NW;
                                        break;
                                    case RESIZE_S:
                                        currMouseBehavior = MouseBehavior.RESIZE_N;
                                        break;
                                    case RESIZE_SE:
                                        currMouseBehavior = MouseBehavior.RESIZE_NE;
                                        break;
                                }
                            }
                            if (newLeft > newRight) {
                                int tmp = newLeft;
                                newLeft = newRight;
                                newRight = tmp;

                                switch (currMouseBehavior) {
                                    case RESIZE_NW:
                                        currMouseBehavior = MouseBehavior.RESIZE_NE;
                                        break;
                                    case RESIZE_W:
                                        currMouseBehavior = MouseBehavior.RESIZE_E;
                                        break;
                                    case RESIZE_NE:
                                        currMouseBehavior = MouseBehavior.RESIZE_NW;
                                        break;
                                    case RESIZE_SW:
                                        currMouseBehavior = MouseBehavior.RESIZE_SE;
                                        break;
                                    case RESIZE_E:
                                        currMouseBehavior = MouseBehavior.RESIZE_W;
                                        break;
                                    case RESIZE_SE:
                                        currMouseBehavior = MouseBehavior.RESIZE_SW;
                                        break;
                                }
                            }
                            newLeft = Math.max(0, newLeft);
                            newTop = Math.max(0, newTop);
                            newRight = Math.min(screenWidth, newRight);
                            newBottom = Math.min(screenHeight, newBottom);
                            left = newLeft;
                            top = newTop;
                            right = newRight;
                            bottom = newBottom;
                        }
                    }
                }
            });
        }
    }

    /**
     * Start a new screenshot.
     */
    public void screenshot() {
        try {
            // reset
            left = 0;
            top = 0;
            right = 0;
            bottom = 0;
            dragStart = null;
            dragEnd = null;
            cropAreaExists = false;
            nextClickBehavior = currMouseBehavior = MouseBehavior.NORMAL;

            // get screen capture
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            captureImage = new Robot().createScreenCapture(new Rectangle(screenSize));
            screenWidth = screenSize.width;
            screenHeight = screenSize.height;

            getContentPane().setPreferredSize(screenSize);
            pack();
            setVisible(true);
            setLocation(0, 0);
            repaintTimer.start();
        } catch (AWTException e) {
            e.getMessage();
        }
    }

    /**
     * Close screenshot window.
     */
    private void close() {
        repaintTimer.stop();
        setVisible(false);
        super.dispose();
    }

    /**
     * Accept crop result.
     */
    private void accept() {
        close();
        if (cropAreaExists && left < right && top < bottom) {
            resultHandler.accept(captureImage.getSubimage(left, top, right - left, bottom - top));
        }
    }

    private void update() {
        if (cropAreaExists && draggingCropArea) {
            left = Math.min(dragStart.x, dragEnd.x);
            right = Math.max(dragStart.x, dragEnd.x);
            top = Math.min(dragStart.y, dragEnd.y);
            bottom = Math.max(dragStart.y, dragEnd.y);
        }

        if (currMouseBehavior == MouseBehavior.NORMAL) {
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            int cursor = Cursor.CROSSHAIR_CURSOR;
            nextClickBehavior = MouseBehavior.NORMAL;
            if (!draggingCropArea) {
                if (cropAreaExists) {
                    cursor = Cursor.DEFAULT_CURSOR;

                    if (isPointInRect(mouse, left, top, right, bottom)) {
                        cursor = Cursor.MOVE_CURSOR;
                        nextClickBehavior = MouseBehavior.MOVE;
                    }

                    if (isMouseOverIndicator(mouse, left, top)) {
                        cursor = Cursor.NW_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_NW;
                    } else if (isMouseOverIndicator(mouse, left + (right - left) / 2, top)) {
                        cursor = Cursor.N_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_N;
                    } else if (isMouseOverIndicator(mouse, right, top)) {
                        cursor = Cursor.NE_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_NE;
                    } else if (isMouseOverIndicator(mouse, left, top + (bottom - top) / 2)) {
                        cursor = Cursor.W_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_W;
                    } else if (isMouseOverIndicator(mouse, right, top + (bottom - top) / 2)) {
                        cursor = Cursor.E_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_E;
                    } else if (isMouseOverIndicator(mouse, left, bottom)) {
                        cursor = Cursor.SW_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_SW;
                    } else if (isMouseOverIndicator(mouse, left + (right - left) / 2, bottom)) {
                        cursor = Cursor.S_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_S;
                    } else if (isMouseOverIndicator(mouse, right, bottom)) {
                        cursor = Cursor.SE_RESIZE_CURSOR;
                        nextClickBehavior = MouseBehavior.RESIZE_SE;
                    }
                }
            }
            setCursor(new Cursor(cursor));
        } else {
            int cursor = Cursor.DEFAULT_CURSOR;
            switch (currMouseBehavior) {
                case MOVE:
                    cursor = Cursor.MOVE_CURSOR;
                    break;
                case RESIZE_NW:
                    cursor = Cursor.NW_RESIZE_CURSOR;
                    break;
                case RESIZE_N:
                    cursor = Cursor.N_RESIZE_CURSOR;
                    break;
                case RESIZE_NE:
                    cursor = Cursor.NE_RESIZE_CURSOR;
                    break;
                case RESIZE_W:
                    cursor = Cursor.W_RESIZE_CURSOR;
                    break;
                case RESIZE_E:
                    cursor = Cursor.E_RESIZE_CURSOR;
                    break;
                case RESIZE_SW:
                    cursor = Cursor.SW_RESIZE_CURSOR;
                    break;
                case RESIZE_S:
                    cursor = Cursor.S_RESIZE_CURSOR;
                    break;
                case RESIZE_SE:
                    cursor = Cursor.SE_RESIZE_CURSOR;
                    break;
            }
            setCursor(new Cursor(cursor));
        }
    }

    private void render(Graphics2D g) {
        if (captureImage != null) {
            g.drawImage(captureImage, 0, 0, null);
        }

        if (cropAreaExists) {
            // crop area grey background
            g.setColor(new Color(0, 0, 0, (int) (255 * 0.4)));
            g.fillRect(0, 0, screenWidth, top);
            g.fillRect(0, top, left, bottom - top);
            g.fillRect(right, top, screenWidth - right, bottom - top);
            g.fillRect(0, bottom, screenWidth, screenHeight - bottom);

            // crop area border
            g.setColor(new Color(0, 174, 255));
            g.drawRect(left, top, right - left, bottom - top);

            // resize indicators
            drawIndicator(g, left, top);
            drawIndicator(g, left + (right - left) / 2, top);
            drawIndicator(g, right, top);
            drawIndicator(g, left, top + (bottom - top) / 2);
            drawIndicator(g, right, top + (bottom - top) / 2);
            drawIndicator(g, left, bottom);
            drawIndicator(g, left + (right - left) / 2, bottom);
            drawIndicator(g, right, bottom);
        }
    }

    private void drawIndicator(Graphics2D g, int x, int y) {
        g.fillRect(
                x - RESIZE_INDICATOR_SIZE / 2,
                y - RESIZE_INDICATOR_SIZE / 2,
                RESIZE_INDICATOR_SIZE, RESIZE_INDICATOR_SIZE
        );
    }

    private boolean isPointInRect(Point point, int left, int top, int right, int bottom) {
        return point.x >= left && point.x <= right && point.y >= top && point.y <= bottom;
    }

    private boolean isMouseOverIndicator(Point mouse, int x, int y) {
        return isPointInRect(
                mouse,
                x - RESIZE_INDICATOR_SIZE / 2,
                y - RESIZE_INDICATOR_SIZE / 2,
                x + RESIZE_INDICATOR_SIZE / 2,
                y + RESIZE_INDICATOR_SIZE / 2
        );
    }

}
