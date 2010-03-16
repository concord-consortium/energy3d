package org.concord.energy3d.scene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.scenegraph.Node;

public class Scene implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final Node root = new Node("House Root");
	private static Scene instance;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();

	public static Scene getInstance() {
		if (instance == null) {
//			instance = new Scene();
			open();
		}
		return instance;
	}

	private Scene() {

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

	public void newFile() {
		root.detachAllChildren();
		parts.clear();
	}

	public static void open() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("house.ser"));
			instance = (Scene)in.readObject();
			in.close();
			for (HousePart housePart : instance.getParts())
				root.attachChild(housePart.getRoot());
		} catch (Throwable e) {
			e.printStackTrace();
			instance = new Scene();
		}		
	}

}
