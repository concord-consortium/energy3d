package org.concord.energy3d.agents;

/**
 * @author Charles Xie
 *
 */
public class EventCounter implements Sensor {

	private int count;
	private final Class<?> clazz;

	public EventCounter(final Class<?> c) {
		clazz = c;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return clazz.getSimpleName();
	}

	@Override
	public void sense(final MyEvent e) {
		if (clazz.isInstance(e)) {
			count++;
		}
	}

	public int getCount() {
		return count;
	}

}
