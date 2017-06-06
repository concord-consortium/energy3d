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

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class CameraControl {
	public enum ButtonAction {
		MOVE, ROTATE, ZOOM, NONE
	};

	protected final Vector3 _upAxis = new Vector3();
	protected final Matrix3 _workerMatrix = new Matrix3();
	protected final Vector3 _workerVector = new Vector3();
	protected InputTrigger _mouseTrigger;
	protected InputTrigger _keyTrigger;
	protected ButtonAction leftButtonAction = ButtonAction.ROTATE;
	protected ButtonAction rightButtonAction = ButtonAction.MOVE;
	protected double _mouseRotateSpeed = .005;
	protected double _moveSpeed = 50;
	protected double _keyRotateSpeed = 2.25;
	protected boolean enabled = true;
	private ReadOnlyVector3 orgCameraDirection;
	private ReadOnlyVector3 newCameraDirection;
	private ReadOnlyVector3 orgCameraLocation;
	private ReadOnlyVector3 newCameraLocation;
	private double animationTime = -1;
	private boolean mouseEnabled = true;
	private boolean leftMouseButtonEnabled = true;
	private boolean rightMouseButtonEnabled = true;

	public CameraControl(final ReadOnlyVector3 upAxis) {
		_upAxis.set(upAxis);
	}

	public ReadOnlyVector3 getUpAxis() {
		return _upAxis;
	}

	public void setUpAxis(final ReadOnlyVector3 upAxis) {
		_upAxis.set(upAxis);
	}

	public double getMouseRotateSpeed() {
		return _mouseRotateSpeed;
	}

	public void setMouseRotateSpeed(final double speed) {
		_mouseRotateSpeed = speed;
	}

	public double getMoveSpeed() {
		return _moveSpeed;
	}

	public void setMoveSpeed(final double speed) {
		_moveSpeed = speed;
	}

	public double getKeyRotateSpeed() {
		return _keyRotateSpeed;
	}

	public void setKeyRotateSpeed(final double speed) {
		_keyRotateSpeed = speed;
	}

	protected abstract void move(final Camera camera, final double dx, final double dy);

	protected abstract void rotate(final Camera camera, final double dx, final double dy);

	/**
	 * @param layer
	 *            the logical layer to register with
	 * @param upAxis
	 *            the up axis of the camera
	 * @param dragOnly
	 *            if true, mouse input will only rotate the camera if one of the mouse buttons (left, center or right) is down.
	 * @return a new FirstPersonControl object
	 */
	public void setupTriggers(final LogicalLayer layer, final ReadOnlyVector3 upAxis, final boolean dragOnly) {
		setupMouseTriggers(layer, dragOnly);
	}

	public void removeTriggers(final LogicalLayer layer) {
		if (_mouseTrigger != null) {
			layer.deregisterTrigger(_mouseTrigger);
		}
		if (_keyTrigger != null) {
			layer.deregisterTrigger(_keyTrigger);
		}
	}

	public void setupMouseTriggers(final LogicalLayer layer, final boolean dragOnly) {
		// Mouse look
		final Predicate<TwoInputStates> someMouseDown = Predicates.or(TriggerConditions.leftButtonDown(), Predicates.or(TriggerConditions.rightButtonDown(), TriggerConditions.middleButtonDown()));
		final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), Predicates.and(someMouseDown, Predicates.not(new KeyHeldCondition(Key.LCONTROL))));
		final TriggerAction dragAction = new TriggerAction() {

			// Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
			private boolean firstPing = true;

			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!enabled || !mouseEnabled) {
					return;
				}
				final MouseState mouse = inputStates.getCurrent().getMouseState();
				if (mouse.getDx() != 0 || mouse.getDy() != 0) {
					if (!firstPing) {
						final boolean left = leftMouseButtonEnabled && mouse.getButtonState(MouseButton.LEFT) == ButtonState.DOWN;
						final boolean right = rightMouseButtonEnabled && mouse.getButtonState(MouseButton.RIGHT) == ButtonState.DOWN;
						final boolean middle = mouse.getButtonState(MouseButton.MIDDLE) == ButtonState.DOWN;
						if (left && leftButtonAction == ButtonAction.MOVE || right && rightButtonAction == ButtonAction.MOVE) {
							final double fac = Camera.getCurrentCamera().getLocation().length() * 150;
							final double dx = -mouse.getDx() * fac / Camera.getCurrentCamera().getWidth();
							final double dy = -mouse.getDy() * fac / Camera.getCurrentCamera().getHeight() / 4.0;
							move(source.getCanvasRenderer().getCamera(), dx, dy);
							SceneManager.getInstance().getCameraNode().updateFromCamera();
							Scene.getInstance().updateEditShapes();
						} else if (left && leftButtonAction == ButtonAction.ROTATE || right && rightButtonAction == ButtonAction.ROTATE) {
							rotate(source.getCanvasRenderer().getCamera(), -mouse.getDx(), -mouse.getDy());
							SceneManager.getInstance().getCameraNode().updateFromCamera();
							Scene.getInstance().updateEditShapes();
						} else if (middle || left && leftButtonAction == ButtonAction.ZOOM || right && rightButtonAction == ButtonAction.ZOOM) {
							zoom(source, tpf, -mouse.getDy() * getCurrentExtent() / 100);
						}
					} else {
						firstPing = false;
					}
				}
			}
		};

		_mouseTrigger = new InputTrigger(dragOnly ? dragged : TriggerConditions.mouseMoved(), dragAction);
		layer.registerTrigger(_mouseTrigger);

		layer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				zoom(source, tpf, inputStates.getCurrent().getMouseState().getDwheel() * getCurrentExtent() / 20);
			}
		}));
	}

	// the extent that depends on the camera orientation (not a perfect solution)
	private static double getCurrentExtent() {
		double extent = 10;
		final BoundingVolume volume = Scene.getRoot().getWorldBound();
		if (volume instanceof BoundingBox) {
			final BoundingBox box = (BoundingBox) volume;
			final ReadOnlyVector3 cameraDirection = SceneManager.getInstance().getCamera().getDirection();
			final Vector3 e1 = box.getExtent(null);
			final double extent1 = Math.abs(e1.cross(cameraDirection, null).length());
			final Vector3 e2 = new Matrix3().applyRotationZ(Math.PI / 2).applyPost(e1, null);
			final double extent2 = Math.abs(e2.cross(cameraDirection, null).length());
			extent = 0.5 * (extent1 + extent2);
		}
		return extent;
	}

	public InputTrigger getKeyTrigger() {
		return _keyTrigger;
	}

	public InputTrigger getMouseTrigger() {
		return _mouseTrigger;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setMouseButtonActions(final ButtonAction leftButtonAction, final ButtonAction rightButtonAction) {
		this.leftButtonAction = leftButtonAction;
		this.rightButtonAction = rightButtonAction;
	}

	public void setMouseEnabled(final boolean enabled) {
		mouseEnabled = enabled;
	}

	public boolean isMouseEnabled() {
		return mouseEnabled;
	}

	public void reset() {

	}

	protected void zoom(final Canvas canvas, final double tpf, final double val) {
		if (Camera.getCurrentCamera().getProjectionMode() == ProjectionMode.Parallel) {
			final double fac = val > 0 ? 1.1 : 0.9;
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setFrustumTop(camera.getFrustumTop() * fac);
			camera.setFrustumBottom(camera.getFrustumBottom() * fac);
			camera.setFrustumLeft(camera.getFrustumLeft() * fac);
			camera.setFrustumRight(camera.getFrustumRight() * fac);
			camera.update();
			_moveSpeed = 2 * camera.getFrustumTop();
		} else {
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			final Vector3 loc = new Vector3(camera.getDirection()).multiplyLocal(-val * (_moveSpeed * 10) * 2 * tpf).addLocal(camera.getLocation());
			if (loc.length() > SceneManager.SKY_RADIUS) {
				return;
			}
			camera.setLocation(loc);
			if (this instanceof OrbitControl) {
				((OrbitControl) this).computeNewFrontDistance();
			}
		}
		SceneManager.getInstance().getCameraNode().updateFromCamera();
		Scene.getInstance().updateEditShapes();
		SceneManager.getInstance().refresh();
	}

	public void zoomAtPoint(final ReadOnlyVector3 clickedPoint) {
		final boolean isPrintPreview = PrintController.getInstance().isPrintPreview();
		final double zoomInDistance = isPrintPreview ? 50 : 20;
		final double zoomDistance;
		final boolean zoomOut = Camera.getCurrentCamera().getLocation().distance(clickedPoint) < zoomInDistance + 1;
		if (zoomOut) {
			zoomDistance = 100.0;
		} else {
			zoomDistance = zoomInDistance;
		}

		orgCameraDirection = new Vector3(Camera.getCurrentCamera().getDirection());
		orgCameraLocation = new Vector3(Camera.getCurrentCamera().getLocation());
		if (isPrintPreview) {
			newCameraDirection = Vector3.UNIT_Y;
		} else {
			newCameraDirection = clickedPoint.subtract(Camera.getCurrentCamera().getLocation(), null).normalizeLocal();
		}
		if (isPrintPreview && zoomOut) {
			newCameraLocation = PrintController.getInstance().getZoomAllCameraLocation();
		} else {
			newCameraLocation = clickedPoint.subtract(newCameraDirection.multiply(zoomDistance, null), null);
		}
		animationTime = SceneManager.getInstance().getTimer().getTimeInSeconds();
	}

	public boolean isAnimating() {
		return animationTime != -1;
	}

	public void animate() {
		final double currentTime = SceneManager.getInstance().getTimer().getTimeInSeconds();
		final double t = currentTime - animationTime;
		final double animationDuration = 1.0;
		final ReadOnlyVector3 currentDirection = orgCameraDirection.multiply(animationDuration - t, null).addLocal(newCameraDirection.multiply(t, null)).normalizeLocal();
		final ReadOnlyVector3 currentLocation = orgCameraLocation.multiply(animationDuration - t, null).addLocal(newCameraLocation.multiply(t, null));
		Camera.getCurrentCamera().setLocation(currentLocation);
		Camera.getCurrentCamera().lookAt(currentLocation.add(currentDirection, null), Vector3.UNIT_Z);
		SceneManager.getInstance().getCameraNode().updateFromCamera();
		Scene.getInstance().updateEditShapes();
		if (t > animationDuration) {
			animationTime = -1;
		}
	}

	public ButtonAction getRightButtonAction() {
		return rightButtonAction;
	}

	public void setRightButtonAction(final ButtonAction rightButtonAction) {
		this.rightButtonAction = rightButtonAction;
	}

	public boolean isLeftMouseButtonEnabled() {
		return leftMouseButtonEnabled;
	}

	public void setLeftMouseButtonEnabled(final boolean leftMouseButtonEnabled) {
		this.leftMouseButtonEnabled = leftMouseButtonEnabled;
	}

	public boolean isRightMouseButtonEnabled() {
		return rightMouseButtonEnabled;
	}

	public void setRightMouseButtonEnabled(final boolean rightMouseButtonEnabled) {
		this.rightMouseButtonEnabled = rightMouseButtonEnabled;
	}

	public ButtonAction getLeftButtonAction() {
		return leftButtonAction;
	}

	public void setLeftButtonAction(final ButtonAction leftButtonAction) {
		this.leftButtonAction = leftButtonAction;
	}

}
