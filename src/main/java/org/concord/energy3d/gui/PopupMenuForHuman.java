package org.concord.energy3d.gui;

import javax.swing.JPopupMenu;

class PopupMenuForHuman extends PopupMenuFactory {

	private static JPopupMenu popupMenuForHuman;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, true, null);
		}
		return popupMenuForHuman;

	}

}
