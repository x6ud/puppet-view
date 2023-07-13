package com.github.x6ud.puppetview.misc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClipboardUtils {

    public static List<BufferedImage> getImages() {
        List<BufferedImage> ret = new ArrayList<>();
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null) {
                if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    ret.add(toBufferedImage((Image) transferable.getTransferData(DataFlavor.imageFlavor)));
                } else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : files) {
                        try {
                            if (!file.isFile()) {
                                continue;
                            }
                            BufferedImage image = ImageIO.read(file);
                            if (image != null) {
                                ret.add(image);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            return ret;
        }
        return ret;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bi.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bi;
    }

    public static void setString(String str) {
        StringSelection selection = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    public static void setImage(Image image) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(image), null);
    }

    // https://stackoverflow.com/questions/4552045/copy-bufferedimage-to-clipboard
    private static class TransferableImage implements Transferable {
        private final Image image;

        public TransferableImage(Image i) {
            this.image = i;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor) && image != null) {
                return image;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (DataFlavor dataFlavor : flavors) {
                if (flavor.equals(dataFlavor)) {
                    return true;
                }
            }
            return false;
        }
    }

}
