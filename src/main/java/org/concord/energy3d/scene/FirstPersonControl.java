/**
 * Copyright (c) 2008-2010 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package org.concord.energy3d.scene;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.Vector4;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

public class FirstPersonControl extends CameraControl {

    public FirstPersonControl(final ReadOnlyVector3 upAxis) {
        super(upAxis);
    }

    @Override
	protected void rotate(final Camera camera, final double dx, final double dy) {
        if (dx != 0) {
            _workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dx, _upAxis != null ? _upAxis : camera.getUp());
            _workerMatrix.applyPost(camera.getLeft(), _workerVector);
            camera.setLeft(_workerVector);
            _workerMatrix.applyPost(camera.getDirection(), _workerVector);
            camera.setDirection(_workerVector);
            _workerMatrix.applyPost(camera.getUp(), _workerVector);
            camera.setUp(_workerVector);
        }

        if (dy != 0) {
            _workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dy, camera.getLeft());
            _workerMatrix.applyPost(camera.getLeft(), _workerVector);
            camera.setLeft(_workerVector);
            _workerMatrix.applyPost(camera.getDirection(), _workerVector);
            camera.setDirection(_workerVector);
            _workerMatrix.applyPost(camera.getUp(), _workerVector);
            camera.setUp(_workerVector);
        }

        camera.normalize();
    }

	@Override
	protected void move(final Camera camera, final double dx, final double dy) {
		final Vector4 v = new Vector4(-dx * _moveSpeed / 500, -dy * _moveSpeed / 500, 0, 0);
		final Vector4 newV = camera.getModelViewProjectionMatrix().applyPost(v, null);
		final Vector3 loc = camera.getLocation().add(new Vector3(newV.getX(), newV.getY(), newV.getZ()), null);
		if (loc.length() > SceneManager.SKY_RADIUS)
			return;
		camera.setLocation(loc);
	}
}
