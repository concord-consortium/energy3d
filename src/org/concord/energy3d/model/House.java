package org.concord.energy3d.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.ardor3d.scenegraph.Node;

public class House implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final Node root = new Node("House Root");
	private static House instance;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();

	public static House getInstance() {
		if (instance == null) {
			instance = new House();
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("house.ser"));
				instance = (House)in.readObject();
				in.close();
				for (HousePart housePart : instance.getParts())
					root.attachChild(housePart.getRoot());
			} catch (Exception e) {
				e.printStackTrace();
				instance = new House();
			}
		}
		return instance;
	}

	private House() {

	}

	public boolean add(HousePart e) {
		return parts.add(e);
	}

	public void clear() {
		parts.clear();
	}

	public boolean remove(Object o) {
		return parts.remove(o);
	}

	public int size() {
		return parts.size();
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}
	
	public void save() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("house.ser"));
			out.writeObject(this);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
