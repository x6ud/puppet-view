package com.github.x6ud.puppetview.misc;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClipboardUtils {

    public static BufferedImage getImage() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                Object data = transferable.getTransferData(DataFlavor.imageFlavor);
                if (data instanceof Image) {
                    return toBufferedImage((Image) transferable.getTransferData(DataFlavor.imageFlavor));
                }
            } catch (UnsupportedFlavorException | IOException e) {
                return null;
            }
        }
        return null;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
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

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
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
