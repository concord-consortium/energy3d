package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Charles Xie
 *
 */
public class PartGroup {

	private String type;
	private List<Long> ids;

	public PartGroup(String type) {
		this.type = type;
		ids = new ArrayList<Long>();
	}

	public PartGroup(String type, List<Long> listOfIds) {
		this.type = type;
		ids = new ArrayList<Long>(listOfIds);
	}

	public void addId(long id) {
		ids.add(id);
	}

	public String getType() {
		return type;
	}

	public List<Long> getIds() {
		return ids;
	}

}
