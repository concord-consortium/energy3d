package org.concord.energy3d.util;

import java.io.*;

public class ObjectCloner {
	// so that nobody can accidentally create an ObjectCloner object
	private ObjectCloner() {
	}

	// returns a deep copy of an object
	static public Object deepCopy(Object oldObj) {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); // A
			oos = new ObjectOutputStream(bos); // B
			// serialize and pass the object
			oos.writeObject(oldObj); // C
			oos.flush(); // D
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); // E
			ois = new ObjectInputStream(bin); // F
			// return the new object
			return ois.readObject(); // G
		} catch (Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			e.printStackTrace();
			return null;
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

}