package org.concord.energy3d.gui;

import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.ReadOnlyTimer;

public class Compass implements Scene, Updater {

	@Override
	public void init() {

	}

	@Override
	public void update(ReadOnlyTimer timer) {

	}

	@Override
	public boolean renderUnto(Renderer renderer) {
		return false;
	}

	@Override
	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

}
