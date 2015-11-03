package org.concord.energy3d.util;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ClipImage implements Transferable, ClipboardOwner {

	private byte[] image;

	public ClipImage(byte[] im)

	{

		image = im;

	}

	public DataFlavor[] getTransferDataFlavors()

	{

		return new DataFlavor[] { DataFlavor.imageFlavor };

	}

	public boolean isDataFlavorSupported(DataFlavor flavor)

	{

		return DataFlavor.imageFlavor.equals(flavor);

	}

	public Object getTransferData(DataFlavor flavor) throws

	UnsupportedFlavorException

	{

		if (!isDataFlavorSupported(flavor))

			throw new UnsupportedFlavorException(flavor);

		return Toolkit.getDefaultToolkit().createImage(image);

	}

	public void lostOwnership(java.awt.datatransfer.Clipboard clip,

	java.awt.datatransfer.Transferable tr)

	{

		return;

	}

}