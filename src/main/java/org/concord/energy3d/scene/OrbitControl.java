package org.concord.energy3d.scene;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Vector4;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

public class OrbitControl extends CameraControl {

    private static final double FRONT_DISTANCE_DEFAULT = 8;
    private static double frontDistance = FRONT_DISTANCE_DEFAULT;
    private final Matrix3 _workerMatrix_2 = new Matrix3();
    private final Vector4 _workerVector4 = new Vector4();
    private final Vector3 _center = new Vector3(1, 0, 1);

    OrbitControl(final ReadOnlyVector3 upAxis) {
        super(upAxis);
    }

    @Override
    protected void rotate(final Camera camera, final double dx, final double dy) {
        if (_center.length() == 0) {
            _center.set(camera.getDirection()).multiplyLocal(frontDistance).addLocal(camera.getLocation());
        }
        _workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dx, _upAxis != null ? _upAxis : camera.getUp());
        _workerMatrix_2.fromAngleNormalAxis(_mouseRotateSpeed * dy, camera.getLeft());
        _workerMatrix.multiplyLocal(_workerMatrix_2);
        camera.getLocation().subtract(_center, _workerVector);
        _workerMatrix.applyPost(_workerVector, _workerVector);
        _workerVector.addLocal(_center);
        final Vector3 d = _workerVector.subtract(_center, null).normalizeLocal();
        final double MIN = 0.99;
        if (!(d.dot(Vector3.UNIT_Z) > MIN || d.dot(Vector3.NEG_UNIT_Z) > MIN)) {
            if (_workerVector.length() > SceneManager.SKY_RADIUS) {
                zoom(SceneManager.getInstance().getCanvas(), 1, -0.1);
                return;
            }
            camera.setLocation(_workerVector);
            camera.lookAt(_center, _upAxis);
        }
    }

    @Override
    protected void move(final Camera camera, final double dx, final double dy) {
        _workerVector4.set(dx * _moveSpeed / 500, dy * _moveSpeed / 500, 0, 0);
        camera.getModelViewProjectionMatrix().applyPost(_workerVector4, _workerVector4);
        _workerVector.set(_workerVector4.getX(), _workerVector4.getY(), _workerVector4.getZ());
        _workerVector.addLocal(camera.getLocation());
        if (_workerVector.length() > SceneManager.SKY_RADIUS) {
            return;
        }
        camera.setLocation(_workerVector);
        clearOrbitCenter();
    }

    void clearOrbitCenter() {
        _center.set(0, 0, 0);
    }

    @Override
    public void reset() {
        clearOrbitCenter();
        computeNewFrontDistance();
    }

    void computeNewFrontDistance() {
        final Camera currentCamera = SceneManager.getInstance().getCamera();
        if (currentCamera != null) {
            frontDistance = _center.subtractLocal(currentCamera.getLocation()).length();
            clearOrbitCenter();
        }
    }

    @Override
    public void zoomAtPoint(final ReadOnlyVector3 clickedPoint) {
        super.zoomAtPoint(clickedPoint);
        _center.set(clickedPoint);
    }

}