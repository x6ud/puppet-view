package com.github.x6ud.puppetview.window;

import com.github.x6ud.puppetview.Point2D;
import com.github.x6ud.puppetview.PopupMenuBuilder;
import com.sun.awt.AWTUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.function.Consumer;

public class ReferenceImage extends JFrame {

    private static final int MIN_SIZE = 50;
    private static final double MAX_SCALE = 10;
    private static final int RESCALE_SCROLL_SPEED = 30;
    private static final int ROTATE_SCROLL_SPEED = 5;
    private static final float MIN_OPACITY = 0.2f;
    private static final float OPACITY_SCROLL_SPEED = 0.1f;
    private static final int COLLAPSED_SIZE = 86;
    private static final float COLLAPSED_OPACITY = 0.75f;

    private BufferedImage image;
    private int imageWidth;
    private int imageHeight;

    private boolean mouseOver = false;

    private boolean collapsed = false;
    private CheckboxMenuItem collapsedMenu;

    private boolean flipHorizontal = false;
    private boolean flipVertical = false;
    private CheckboxMenuItem flipHorizontalMenu;
    private CheckboxMenuItem flipVerticalMenu;

    private boolean greyscale = false;
    private BufferedImage cachedGreyscaleImage = null;

    private double scale = 1;
    private int rotationDeg = 0;
    private float opacity = 1;

    private double actualScale;
    private double translateX = 0;
    private double translateY = 0;
    private Shape borderShape;
    private double lastCenterX;
    private double lastCenterY;

    public ReferenceImage(BufferedImage image, Consumer<ReferenceImage> closeCallback) throws HeadlessException {
        super();

        this.image = image;
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        lastCenterX = imageWidth / 2.0d;
        lastCenterY = imageHeight / 2.0d;

        setSize(imageWidth, imageHeight);
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setType(Type.UTILITY);

        Point mouse = MouseInfo.getPointerInfo().getLocation();
        setLocation(
                Math.max(0, mouse.x - imageWidth / 2),
                Math.max(0, mouse.y - imageHeight / 2)
        );

        add(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                render((Graphics2D) g);
            }
        });

        // right click menu
        PopupMenu popupMenu = new PopupMenuBuilder()
                .checkboxMenuItem("Flip Horizontally", flipHorizontal, menuItem -> flipHorizontalMenu = menuItem,
                        e -> {
                            flipHorizontal = e.getStateChange() == ItemEvent.SELECTED;
                            repaint();
                        }
                )
                .checkboxMenuItem("Flip Vertically", flipVertical, menuItem -> flipVerticalMenu = menuItem,
                        e -> {
                            flipVertical = e.getStateChange() == ItemEvent.SELECTED;
                            repaint();
                        }
                )
                .separator()
                .menuItem("1:1 Size", e -> {
                    scale = 1;
                    update();
                })
                .menuItem("Reset Rotation and Flip", e -> {
                    rotationDeg = 0;
                    flipHorizontal = false;
                    flipVertical = false;
                    flipHorizontalMenu.setState(false);
                    flipVerticalMenu.setState(false);
                    update();
                })
                .menuItem("Reset Opacity", e -> {
                    opacity = 1;
                    update();
                })
                .separator()
                .checkboxMenuItem("Collapsed", collapsed, menuItem -> collapsedMenu = menuItem,
                        e -> {
                            collapsed = e.getStateChange() == ItemEvent.SELECTED;
                            update();
                        }
                )
                .separator()
                .checkboxMenuItem("Greyscale", greyscale,
                        e -> {
                            greyscale = e.getStateChange() == ItemEvent.SELECTED;
                            repaint();
                        }
                )
                .separator()
                .menuItem("Hide", e -> {
                    setVisible(false);
                })

                .menuItem("Close", e -> {
                    close();
                    closeCallback.accept(this);
                })
                .get();

        // event listeners
        {
            Point dragStart = new Point();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // drag to move
                    dragStart.setLocation(e.getX(), e.getY());
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            // double click collapse
                            if (e.getClickCount() == 2) {
                                collapsed = !collapsed;
                                collapsedMenu.setState(collapsed);
                                update();
                            }
                            break;
                        case MouseEvent.BUTTON3:
                            // right click menu
                            add(popupMenu);
                            popupMenu.show(ReferenceImage.this, e.getX(), e.getY());
                            break;
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseOver = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseOver = false;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // drag move
                    int x0 = getLocation().x;
                    int y0 = getLocation().y;
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    setLocation(x0 + dx, y0 + dy);
                    repaint();
                }
            });

            addMouseWheelListener(e -> {
                if (mouseOver && !collapsed) {
                    if (e.isControlDown()) {
                        // ctrl+wheel set rotation
                        if (e.isShiftDown()) {
                            proportionalRotate(e.getWheelRotation());
                        } else {
                            rotate(e.getWheelRotation() * ROTATE_SCROLL_SPEED);
                        }
                    } else if (e.isAltDown()) {
                        // alt+wheel set opacity
                        opacity(-e.getWheelRotation() * OPACITY_SCROLL_SPEED);
                    } else {
                        // wheel set scale
                        if (e.isShiftDown()) {
                            proportionalScale(-e.getWheelRotation());
                        } else {
                            scale(-e.getWheelRotation() * RESCALE_SCROLL_SPEED);
                        }
                    }
                }
            });

            // repaint border
            addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        // show
        setVisible(true);
        update();
    }

    /**
     * Hide and dispose this window.
     */
    public void close() {
        setVisible(false);
        dispose();
    }

    /* ============================================== */

    private void scale(int diff) {
        double newScale;
        if (imageWidth > imageHeight) {
            int newWidth = (int) Math.max(imageWidth * scale + diff, MIN_SIZE);
            newScale = (double) newWidth / (double) imageWidth;
        } else {
            int newHeight = (int) Math.max(imageHeight * scale + diff, MIN_SIZE);
            newScale = (double) newHeight / (double) imageHeight;
        }
        scale = Math.min(newScale, MAX_SCALE);
        update();
    }

    private void proportionalScale(int diff) {
        scale = Math.max(0.25, Math.min(MAX_SCALE, Math.round(scale / 0.25 + diff) * 0.25));
        update();
    }

    private void rotate(int diff) {
        rotationDeg = (rotationDeg + diff) % 360;
        update();
    }

    private void proportionalRotate(int diff) {
        rotationDeg = ((int) (Math.round(rotationDeg / 45.0d + diff) * 45)) % 360;
        update();
    }

    private void opacity(float diff) {
        opacity = Math.min(1, Math.max(MIN_OPACITY, opacity + diff));
        update();
    }

    /* ============================================== */

    private synchronized void update() {
        actualScale = collapsed ?
                Math.min(scale, (double) COLLAPSED_SIZE / Math.max(imageWidth, imageHeight))
                : scale;

        double rad = Math.toRadians(rotationDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double sWidth = imageWidth * actualScale;
        double sHeight = imageHeight * actualScale;

        // rect after rotate
        Point2D v1 = new Point2D(0, 0);
        Point2D v2 = rotatePointAroundOrigin(sWidth, 0, sin, cos);
        Point2D v3 = rotatePointAroundOrigin(sWidth, sHeight, sin, cos);
        Point2D v4 = rotatePointAroundOrigin(0, sHeight, sin, cos);

        // shape AABB
        double left = Math.min(v1.x, Math.min(v2.x, Math.min(v3.x, v4.x)));
        double right = Math.max(v1.x, Math.max(v2.x, Math.max(v3.x, v4.x)));
        double top = Math.min(v1.y, Math.min(v2.y, Math.min(v3.y, v4.y)));
        double bottom = Math.max(v1.y, Math.max(v2.y, Math.max(v3.y, v4.y)));
        double width = right - left;
        double height = bottom - top;

        translateX = left;
        translateY = top;

        Shape windowShape = new Polygon(
                new int[]{(int) (v1.x - left), (int) (v2.x - left), (int) (v3.x - left), (int) (v4.x - left)},
                new int[]{(int) (v1.y - top), (int) (v2.y - top), (int) (v3.y - top), (int) (v4.y - top)},
                4
        );

        double centerX = (v1.x + v3.x) / 2 - left;
        double centerY = (v1.y + v3.y) / 2 - top;
        // shrink the border to origin by 1px so it won't out of the window shape
        borderShape = new Polygon(
                new int[]{
                        shrink1px(v1.x - left, centerX),
                        shrink1px(v2.x - left, centerX),
                        shrink1px(v3.x - left, centerX),
                        shrink1px(v4.x - left, centerX)
                },
                new int[]{
                        shrink1px(v1.y - top, centerY),
                        shrink1px(v2.y - top, centerY),
                        shrink1px(v3.y - top, centerY),
                        shrink1px(v4.y - top, centerY)
                },
                4
        );

        // adjust window location to keep image center stays in place
        double dx = centerX - lastCenterX;
        double dy = centerY - lastCenterY;
        lastCenterX = centerX;
        lastCenterY = centerY;
        setLocation(getX() - (int) dx, getY() - (int) dy);

        setSize((int) width, (int) height);
        setOpacity(collapsed ? COLLAPSED_OPACITY : opacity);
        repaint();
        AWTUtilities.setWindowShape(this, windowShape);
    }

    private Point2D rotatePointAroundOrigin(double x, double y, double sin, double cos) {
        return new Point2D(
                (int) (x * cos - y * sin),
                (int) (x * sin + y * cos)
        );
    }

    private int shrink1px(double val, double boundary) {
        return val > boundary ? (int) (val - 1) : (int) val;
    }

    /* ============================================== */

    private synchronized void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // draw image
        {
            AffineTransform originTransform = g.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.translate(-translateX, -translateY);
            transform.rotate(Math.toRadians(rotationDeg), 0, 0);
            g.setTransform(transform);

            int width = (int) (imageWidth * actualScale);
            int height = (int) (imageHeight * actualScale);
            g.drawImage(
                    greyscale ? getGreyscaleImage() : image,
                    flipHorizontal ? width : 0,
                    flipVertical ? height : 0,
                    flipHorizontal ? -width : width,
                    flipVertical ? -height : height,
                    null);

            g.setTransform(originTransform);
        }

        // draw border
        if (borderShape != null) {
            g.setColor(isActive() && mouseOver ? new Color(57, 122, 243) : new Color(0x98, 0x98, 0x98));
            g.draw(borderShape);
        }
    }

    private BufferedImage getGreyscaleImage() {
        if (cachedGreyscaleImage != null) {
            return cachedGreyscaleImage;
        }
        return cachedGreyscaleImage =
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(image, null);
    }

}
