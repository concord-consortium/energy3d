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

import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Vector4;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

public class OrbitControl extends CameraControl {
	private static final double FRONT_DISTANCE_DEFAULT = 8;
	private static double frontDistance = FRONT_DISTANCE_DEFAULT;
	private Vector3 _center = new Vector3(1, 0, 1);
	private final Matrix3 _workerMatrix_2 = new Matrix3();
	private final Vector4 _workerVector4 = new Vector4();

	public OrbitControl(final ReadOnlyVector3 upAxis) {
		super(upAxis);
	}

	protected void move(final Camera camera, final KeyboardState kb, final double tpf) {
		// MOVEMENT
		int moveFB = 0, strafeLR = 0;
		if (kb.isDown(Key.W)) {
			moveFB += 1;
		}
		if (kb.isDown(Key.S)) {
			moveFB -= 1;
		}
		if (kb.isDown(Key.A)) {
			strafeLR += 1;
		}
		if (kb.isDown(Key.D)) {
			strafeLR -= 1;
		}

		if (moveFB != 0 || strafeLR != 0) {
			final Vector3 loc = _workerVector.zero();
			if (moveFB == 1) {
				loc.addLocal(camera.getDirection());
			} else if (moveFB == -1) {
				loc.subtractLocal(camera.getDirection());
			}
			if (strafeLR == 1) {
				loc.addLocal(camera.getLeft());
			} else if (strafeLR == -1) {
				loc.subtractLocal(camera.getLeft());
			}
			loc.normalizeLocal().multiplyLocal(_moveSpeed * tpf).addLocal(camera.getLocation());
			camera.setLocation(loc);
//			_center.set(camera.getDirection()).multiplyLocal(FRONT_DISTANCE_DEFAULT).addLocal(camera.getLocation());
			clearOrbitCenter();
		}

		// ROTATION
		int rotX = 0, rotY = 0;
		if (kb.isDown(Key.UP)) {
			rotY += 1;
		}
		if (kb.isDown(Key.DOWN)) {
			rotY -= 1;
		}
		if (kb.isDown(Key.LEFT)) {
			rotX += 1;
		}
		if (kb.isDown(Key.RIGHT)) {
			rotX -= 1;
		}
		if (rotX != 0 || rotY != 0) {
			rotate(camera, rotX * (_keyRotateSpeed / _mouseRotateSpeed) * tpf, rotY * (_keyRotateSpeed / _mouseRotateSpeed) * tpf);
		}
	}

	protected void rotate(final Camera camera, final double dx, final double dy) {
		if (_center.length() == 0)
			_center.set(camera.getDirection()).multiplyLocal(frontDistance).addLocal(camera.getLocation());
		_workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dx, _upAxis != null ? _upAxis : camera.getUp());
		_workerMatrix_2.fromAngleNormalAxis(_mouseRotateSpeed * dy, camera.getLeft());
		_workerMatrix.multiplyLocal(_workerMatrix_2);
		camera.getLocation().subtract(_center, _workerVector);
		_workerMatrix.applyPost(_workerVector, _workerVector);
		_workerVector.addLocal(_center);
		final Vector3 d = _workerVector.subtract(_center, null).normalizeLocal();
		final double MIN = 0.1;
		if (Math.abs(d.getX()) > MIN || Math.abs(d.getY()) > MIN) {
			camera.setLocation(_workerVector);
			camera.lookAt(_center, _upAxis);
		}
	}

	protected void move(final Camera camera, final double dx, final double dy) {
		_workerVector4.set(dx * _moveSpeed / 500, dy * _moveSpeed / 500, 0, 0);
		camera.getModelViewProjectionMatrix().applyPost(_workerVector4, _workerVector4);
		_workerVector.set(_workerVector4.getX(), _workerVector4.getY(), _workerVector4.getZ());
		_workerVector.addLocal(camera.getLocation());
		camera.setLocation(_workerVector);
//		_center.set(camera.getDirection()).multiplyLocal(FRONT_DISTANCE).addLocal(camera.getLocation());
		clearOrbitCenter();
	}
	
	private void clearOrbitCenter() {
		_center.set(0, 0, 0);
	}
	
	public void reset() {
		clearOrbitCenter();
		computeNewFrontDistance();
	}
	
	public void computeNewFrontDistance() {
//		final Camera currentCamera = Camera.getCurrentCamera();
		final Camera currentCamera = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera();
		if (currentCamera != null) {
			frontDistance = _center.subtractLocal(currentCamera.getLocation()).length();
			clearOrbitCenter();
		}
	}
}
