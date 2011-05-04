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

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseMovedCondition;
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
	public enum ButtonAction {MOVE, ROTATE, ZOOM, NONE};
    protected final Vector3 _upAxis = new Vector3();
    protected double _mouseRotateSpeed = .005;
    protected double _moveSpeed = 50;
    protected double _keyRotateSpeed = 2.25;
    protected final Matrix3 _workerMatrix = new Matrix3();    
    protected final Vector3 _workerVector = new Vector3();
    protected InputTrigger _mouseTrigger;
    protected InputTrigger _keyTrigger;
    protected boolean enabled = true;
	protected ButtonAction leftButtonAction = ButtonAction.ROTATE;
	protected ButtonAction rightButtonAction = ButtonAction.MOVE;
	private boolean mouseEnabled = true;
//	private boolean zoomScale = true;

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
    
//	private void zoom(final Canvas canvas, final double tpf, int val) {
//		final Camera camera = canvas.getCanvasRenderer().getCamera();
//		final Vector3 loc = new Vector3(camera.getDirection()).multiplyLocal(-val * _moveSpeed * 10 * tpf).addLocal(camera.getLocation());
//		// final Vector3 loc = new Vector3(camera.getLocation()).addLocal(dir);
//		camera.setLocation(loc);
//	}    

    protected abstract void move(final Camera camera, final KeyboardState kb, final double tpf);
    
    protected abstract void move(final Camera camera, final double dx, final double dy);

    protected abstract void rotate(final Camera camera, final double dx, final double dy);

    /**
     * @param layer
     *            the logical layer to register with
     * @param upAxis
     *            the up axis of the camera
     * @param dragOnly
     *            if true, mouse input will only rotate the camera if one of the mouse buttons (left, center or right)
     *            is down.
     * @return a new FirstPersonControl object
     */
    public void setupTriggers(final LogicalLayer layer, final ReadOnlyVector3 upAxis, final boolean dragOnly) {
        this.setupKeyboardTriggers(layer);
        this.setupMouseTriggers(layer, dragOnly);
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
        final CameraControl control = this;
        // Mouse look
        final Predicate<TwoInputStates> someMouseDown = Predicates.or(TriggerConditions.leftButtonDown(), Predicates
                .or(TriggerConditions.rightButtonDown(), TriggerConditions.middleButtonDown()));
        final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), Predicates.and(someMouseDown, Predicates.not(new KeyHeldCondition(Key.LCONTROL))));
//        final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), someMouseDown);
        final TriggerAction dragAction = new TriggerAction() {

            // Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
            private boolean firstPing = true;

            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
            	if (!enabled || !mouseEnabled) return;
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDx() != 0 || mouse.getDy() != 0) {
                    if (!firstPing) {
                    	final boolean left = mouse.getButtonState(MouseButton.LEFT) == ButtonState.DOWN;
                    	final boolean right = mouse.getButtonState(MouseButton.RIGHT) == ButtonState.DOWN;
						if (left && leftButtonAction == ButtonAction.MOVE || right && rightButtonAction == ButtonAction.MOVE) {
							final double fac = Camera.getCurrentCamera().getLocation().length() * 150;
                    		final double dx = -mouse.getDx() * fac / Camera.getCurrentCamera().getWidth();
							final double dy = -mouse.getDy() * fac / Camera.getCurrentCamera().getHeight() / 4.0;
							control.move(source.getCanvasRenderer().getCamera(), dx, dy);
                    		SceneManager.getInstance().getCameraNode().updateFromCamera();
						} else if (left && leftButtonAction == ButtonAction.ROTATE || right && rightButtonAction == ButtonAction.ROTATE) {
                    		control.rotate(source.getCanvasRenderer().getCamera(), -mouse.getDx(), -mouse.getDy());
                    		SceneManager.getInstance().getCameraNode().updateFromCamera();
						} else if (left && leftButtonAction == ButtonAction.ZOOM || right && rightButtonAction == ButtonAction.ZOOM) {
							int dy = inputStates.getCurrent().getMouseState().getDy();
							if (dy < -4)
								dy = -4;
							if (dy > 4)
								dy = 4;
							zoom(source, tpf, -dy / 1.0);
						}
                    } else {
                        firstPing = false;
                    }
                }
            }
        };
        

        _mouseTrigger = new InputTrigger(dragOnly ? dragged : TriggerConditions.mouseMoved(), dragAction);
        layer.registerTrigger(_mouseTrigger);

        
//        layer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), new TriggerAction() {
//        	public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
//        		zoom(source, tpf, inputStates.getCurrent().getMouseState().getDwheel());
//        	}
//        })); 

        
		layer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				zoom(source, tpf, inputStates.getCurrent().getMouseState().getDwheel());
			}
		}));
		
		final KeyHeldCondition cond1 = new KeyHeldCondition(Key.LCONTROL);
		final MouseMovedCondition cond2 = new MouseMovedCondition();
		final Predicate<TwoInputStates> condition = Predicates.and(cond1, Predicates.and(cond2, someMouseDown));
		layer.registerTrigger(new InputTrigger(condition, new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				int dy = inputStates.getCurrent().getMouseState().getDy();
				if (dy < -4)
					dy = -4;
				if (dy > 4)
					dy = 4;
				zoom(source, tpf, -dy / 1.0);
			}
		}));	        
    }

    public Predicate<TwoInputStates> setupKeyboardTriggers(final LogicalLayer layer) {

        final CameraControl control = this;

        // WASD control
        final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.W, Key.A, Key.S, Key.D, Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN };

            public boolean apply(final TwoInputStates states) {
                for (final Key k : keys) {
                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
                        return true;
                    }
                }
                return false;
            }
        };

        final TriggerAction moveAction = new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
            	if (!enabled) return;
                control.move(source.getCanvasRenderer().getCamera(), inputStates.getCurrent().getKeyboardState(), tpf);
                SceneManager.getInstance().getCameraNode().updateFromCamera();
            }
        };
        _keyTrigger = new InputTrigger(keysHeld, moveAction);
        layer.registerTrigger(_keyTrigger);	
        
        return keysHeld;
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
    
    public void setEnabled(boolean enabled) {
    	this.enabled = enabled;
    }
	
	public void setMouseButtonActions(ButtonAction leftButtonAction, ButtonAction rightButtonAction) {
		this.leftButtonAction = leftButtonAction;
		this.rightButtonAction = rightButtonAction;
	}

	public void setMouseEnabled(boolean enabled) {		
		this.mouseEnabled  = enabled;
	}

	public boolean isMouseEnabled() {
		return mouseEnabled;
	}
	
	public void reset() {
		
	}
	
	private void zoom(final Canvas canvas, final double tpf, double val) {
		if (Camera.getCurrentCamera().getProjectionMode() == ProjectionMode.Parallel) {
			final double fac = val > 0 ? 1.1 : 0.9;
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setFrustumTop(camera.getFrustumTop() * fac);
			camera.setFrustumBottom(camera.getFrustumBottom() * fac);
			camera.setFrustumLeft(camera.getFrustumLeft() * fac);
			camera.setFrustumRight(camera.getFrustumRight() * fac);
			camera.update();
			setMoveSpeed(2 * camera.getFrustumTop() * camera.getFrustumTop());
		} else {
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			final Vector3 loc = new Vector3(camera.getDirection()).multiplyLocal(-val * _moveSpeed * 2 * tpf).addLocal(camera.getLocation());
			camera.setLocation(loc);

			if (this instanceof OrbitControl)
				((OrbitControl) this).computeNewFrontDistance();
		}
		SceneManager.getInstance().getCameraNode().updateFromCamera();
	}
}
