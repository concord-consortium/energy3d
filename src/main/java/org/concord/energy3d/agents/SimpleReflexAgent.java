package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.undo.ChangePartUValueCommand;

/**
 * @author Charles Xie
 *
 */
public class SimpleReflexAgent implements Agent {

	private final String name;
	private final List<Sensor> sensors;
	private final List<Actuator> actuators;

	public SimpleReflexAgent(final String name) {
		this.name = name;
		sensors = new ArrayList<Sensor>();
		actuators = new ArrayList<Actuator>();
		final EventCounter counter = new EventCounter(ChangePartUValueCommand.class);
		sensors.add(counter);
		actuators.add(new EventCounterActuator(counter));
	}

	@Override
	public String getName() {
		return name;
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
	public void sense(final MyEvent e) {
		for (final Sensor s : sensors) {
			s.sense(e);
		}
	}

	@Override
	public void actuate() {
		for (final Actuator a : actuators) {
			a.actuate();
		}
	}

	@Override
	public String toString() {
		return "Simple Reflex Agent";
	}

}
