package org.concord.energy3d.util;

import com.ardor3d.framework.Updater;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.SceneHints;
import com.ardor3d.util.ReadOnlyTimer;

public class Blinker implements Updater {
	private static final Blinker instance = new Blinker();
	private Node target = null;
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

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		if (this.target != null)
			this.target.getSceneHints().setCullHint(CullHint.Inherit);
		this.target = target;
	}
}
