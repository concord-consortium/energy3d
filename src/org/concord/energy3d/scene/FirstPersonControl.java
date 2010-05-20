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
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Vector4;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

public class FirstPersonControl extends CameraControl {

    public FirstPersonControl(final ReadOnlyVector3 upAxis) {
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
            final Vector3 loc = _workerStoreA.zero();
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
        }

        // ROTATION
        int rotX = 0, rotY = 0;
        if (kb.isDown(Key.UP)) {
            rotY -= 1;
        }
        if (kb.isDown(Key.DOWN)) {
            rotY += 1;
        }
        if (kb.isDown(Key.LEFT)) {
            rotX += 1;
        }
        if (kb.isDown(Key.RIGHT)) {
            rotX -= 1;
        }
        if (rotX != 0 || rotY != 0) {
            rotate(camera, rotX * (_keyRotateSpeed / _mouseRotateSpeed) * tpf, rotY
                    * (_keyRotateSpeed / _mouseRotateSpeed) * tpf);
        }
    }

    protected void rotate(final Camera camera, final double dx, final double dy) {

        if (dx != 0) {
            _workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dx, _upAxis != null ? _upAxis : camera.getUp());
            _workerMatrix.applyPost(camera.getLeft(), _workerStoreA);
            camera.setLeft(_workerStoreA);
            _workerMatrix.applyPost(camera.getDirection(), _workerStoreA);
            camera.setDirection(_workerStoreA);
            _workerMatrix.applyPost(camera.getUp(), _workerStoreA);
            camera.setUp(_workerStoreA);
        }

        if (dy != 0) {
            _workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dy, camera.getLeft());
            _workerMatrix.applyPost(camera.getLeft(), _workerStoreA);
            camera.setLeft(_workerStoreA);
            _workerMatrix.applyPost(camera.getDirection(), _workerStoreA);
            camera.setDirection(_workerStoreA);
            _workerMatrix.applyPost(camera.getUp(), _workerStoreA);
            camera.setUp(_workerStoreA);
        }

        camera.normalize();
    }
    
	protected void move(final Camera camera, final double dx, final double dy) {
//		Vector4 v = new Vector4(-dx * _moveSpeed / 500, 0, -dy * _moveSpeed / 500, 0);
//		Vector4 newV = camera.getModelViewMatrix().applyPost(v, null);
//		camera.setLocation(camera.getLocation().add(new Vector3(newV.getX(), newV.getZ(), v.getZ()), null));
		
		Vector4 v = new Vector4(-dx * _moveSpeed / 500, -dy * _moveSpeed / 500, 0, 0);
		Vector4 newV = camera.getModelViewProjectionMatrix().applyPost(v, null);
		camera.setLocation(camera.getLocation().add(new Vector3(newV.getX(), newV.getY(), newV.getZ()), null));
		
	}    
}