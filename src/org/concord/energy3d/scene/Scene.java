package org.concord.energy3d.scene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture3D;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;
import com.google.common.collect.Lists;

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
		init();
	}
	
    public void init() {
//    	System.out.println("Scene.init()");
//		final TextureState ts = new TextureState();
//        final Texture texture = createTexture();
//        texture.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);
//		ts.setTexture(texture);
//		root.clearRenderState(StateType.Texture);
//		root.setRenderState(ts);  
//		root.updateWorldRenderStates(true);
//		root.updateGeometricState(0);
	}

	private Texture createTexture() {
        final Texture3D tex = new Texture3D();
        tex.setMinificationFilter(MinificationFilter.BilinearNoMipMaps);
        tex.setTextureKey(TextureKey.getKey(null, false, Format.RGBA8, MinificationFilter.BilinearNoMipMaps));
        final Image img = new Image();
        final int C = 10;
        img.setWidth(C);
        img.setHeight(C);
        img.setDepth(C);
        img.setFormat(Format.RGB8);

        final int size = C * C * 3;
        int ii = (int)(Math.random() * C);
        int jj = (int)(Math.random() * size);
        System.out.println("heat point = " + ii + "," + jj);
        final List<ByteBuffer> data = Lists.newArrayList();
        for (int i = 0; i < C; i++) {
			final ByteBuffer layer = BufferUtils.createByteBuffer(size);
        	for (int j=0; j<size; j++)
//        		layer.put((byte)(Math.random()*255));
        	if (i == ii && j == jj) {
        		layer.put((byte)255);
        	} else
        		layer.put((byte)0);
        	layer.rewind();
        	Image colorImage = new Image(Image.Format.RGB8, C, C, layer);
            data.add(colorImage.getData(0));
        }
        img.setData(data);
        tex.setImage(img);
        tex.setWrap(WrapMode.BorderClamp);
//        tex.setEnvPlaneS(new Vector4(0.5, 0, 0, 0));
//        tex.setEnvPlaneT(new Vector4(0, 0.5, 0, 0));
//        tex.setEnvPlaneR(new Vector4(0, 0, 0.5, 0));        
        return tex;
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
		instance.init();
	}

}
