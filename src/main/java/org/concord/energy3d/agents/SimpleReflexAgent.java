package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.concord.energy3d.undo.MyAbstractUndoableEdit;

/**
 * @author Charles Xie
 *
 */
public class SimpleReflexAgent implements Agent {

	private final List<Sensor> sensors;
	private final List<Actuator> actuators;

	public SimpleReflexAgent() {
		sensors = new ArrayList<Sensor>();
		actuators = new ArrayList<Actuator>();
	}

	public void addSensor(final Sensor s) {
		sensors.add(s);
	}

	public void removeSensor(final Sensor s) {
		sensors.remove(s);
	}

	public void addActuator(final Actuator a) {
		actuators.add(a);
	}

	public void removeActuator(final Actuator a) {
		actuators.remove(a);
	}

	@Override
	public void sense(final MyAbstractUndoableEdit edit) {
		System.out.println(this + ": sensing " + edit.getPresentationName() + " @" + new Date(edit.getTimestamp()));
		for (final Sensor s : sensors) {
			s.sense();
		}
	}

	@Override
	public void actuate() {
		System.out.println(this + ": actuating");
		for (final Actuator a : actuators) {
			a.actuate();
		}
	}

	@Override
	public String toString() {
		return "Simple Reflex Agent";
	}

}
