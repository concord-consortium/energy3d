package org.concord.energy3d.util;

import com.ardor3d.framework.Updater;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.SceneHints;
import com.ardor3d.util.ReadOnlyTimer;

public class Blinker implements Updater {
	private static final Blinker instance = new Blinker();
	private Spatial target = null;
	private double lastTime;	

	public static Blinker getInstance() {
		return instance;
	}

	private Blinker() {
	}

	public void init() {

	}

	public void update(ReadOnlyTimer timer) {
		final double t = timer.getTimeInSeconds();
		if (target != null && t > lastTime + 0.3) {
			lastTime = t;
			final SceneHints sceneHints = target.getSceneHints();
			sceneHints.setCullHint(sceneHints.getCullHint() == CullHint.Always ? CullHint.Inherit : CullHint.Always);
		}

	}

	public Spatial getTarget() {
		return target;
	}

	public void setTarget(final Spatial target) {
		if (this.target != null && this.target != target)
			this.target.getSceneHints().setCullHint(CullHint.Inherit);
		this.target = target;
	}
}
