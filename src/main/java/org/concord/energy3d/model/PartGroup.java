package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Charles Xie
 *
 */
public class PartGroup {

	private final String type;
	private final List<Long> ids;

	public PartGroup(final String type) {
		this.type = type;
		ids = new ArrayList<Long>();
	}

	public PartGroup(final String type, final List<Long> listOfIds) {
		this.type = type;
		ids = new ArrayList<Long>(listOfIds);
	}

	public void addId(final long id) {
		ids.add(id);
	}

	public String getType() {
		return type;
	}

	public List<Long> getIds() {
		return ids;
	}

}
