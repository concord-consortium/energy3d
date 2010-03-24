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
import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.util.GeneratedImageFactory;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
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
	private double RADIUS = 0;

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
    	System.out.println("Scene.init()");
		TextureState ts;
		ts = (TextureState)root.getLocalRenderState(StateType.Texture);
		if (ts == null) {
			ts = new TextureState();
			root.setRenderState(ts);
		}
		C = 50;
		size = C * C * C * 3;
		tex = new Texture3D();
		texBuffer = BufferUtils.createByteBuffer(size);
		
        final Texture texture = createTexture();
//        texture.setApply(ApplyMode.Combine);
//        texture.setCombineFuncRGB(combineFuncRGB)
        texture.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);
		ts.setTexture(texture);
		ts.setNeedsRefresh(true);
	}
    
    int C; // = 20;
    int size; // = C * C * C * 3;
    ByteBuffer texBuffer;// = BufferUtils.createByteBuffer(size);
    Texture3D tex; // = new Texture3D();
    
    public void updateTexture() {
    	texBuffer.position(0);
//    	int color = (int)(Math.random() * 3);
//    	for (int i = 0; i < size-3; i+=3) {
//    		int r = color == 0 ? 255 : 0; 
//    		int g = color == 1 ? 255 : 0;
//    		int b = color == 2 ? 255 : 0;
//    		texBuffer.put((byte)r).put((byte)g).put((byte)b);
//    	}
    	
        for (int i = 0; i < C; i++) {
            for (int j = 0; j < C * C * 3; j+=3) {
            	int x = j/3/C;
            	int y = (j % (C*3)) / 3;
            	int z = (i);
            	byte val = (byte)(RADIUS/new Vector3(x, y, z).subtractLocal(0, 0, 0).length());
            	RADIUS = (RADIUS + 0.005)%100000;
//            	System.out.println(RADIUS);
            	texBuffer.position(i*C*C*3+j+0);
            	texBuffer.put(val);
            }
        }    	
    	
    	texBuffer.rewind();
    }
    
    public void renderTexture(Renderer renderer) {
    	texBuffer.rewind();
    	renderer.updateTexture3DSubImage(tex, 0, 0, 0, C, C, C, texBuffer, 0, 0, 0, C, C);
    }    

    private Texture createTexture() {
//        final Texture3D tex = new Texture3D();
        tex.setMinificationFilter(MinificationFilter.BilinearNoMipMaps);
        tex.setTextureKey(TextureKey.getRTTKey(MinificationFilter.BilinearNoMipMaps));
        
        final int size = C * C * 3;
        final Image img = new Image();
        img.setWidth(C);
        img.setHeight(C);
        img.setDepth(C);

        final List<ByteBuffer> data = Lists.newArrayList();
        for (int i = 0; i < C; i++) {
            final Image colorImage = GeneratedImageFactory.createSolidColorImage(ColorRGBA.BLACK, false, C);            
            ByteBuffer data_i = colorImage.getData(0);			
            for (int j = 0; j < size; j+=3) {
            	int x = j/3/C;
            	int y = (j % (C*3)) / 3;
            	int z = (i);
            	byte val = (byte)(RADIUS/new Vector3(x, y, z).subtractLocal(0, 0, 0).length());
            	RADIUS = (RADIUS + 0.005)%100000;
//            	System.out.println(RADIUS);
            	data_i.position(j+0);
            	data_i.put(val);
            }
            
            data_i.rewind();
            data.add(data_i);
            if (i == 0) {
                img.setDataFormat(colorImage.getDataFormat());
                img.setDataType(colorImage.getDataType());
            }
        }
        int ii = (int)(Math.random() * C);
        int jj = (int)(Math.random() * size);
        data.get(ii).put(jj, (byte)255);
        System.out.println("heat point = " + ii + "," + jj);
        
        img.setData(data);
        tex.setImage(img);
        tex.setWrap(WrapMode.BorderClamp);
        return tex;
    }    
//	private Texture createTexture1() {
//        final Texture3D tex = new Texture3D();
//        tex.setMinificationFilter(MinificationFilter.BilinearNoMipMaps);
////        tex.setTextureKey(TextureKey.getKey(null, false, TextureStoreFormat.RGBA8, MinificationFilter.BilinearNoMipMaps));
//        tex.setTextureKey(TextureKey.getRTTKey(MinificationFilter.BilinearNoMipMaps));
//        final Image img = new Image();
//        final int C = 10;
//        img.setWidth(C);
//        img.setHeight(C);
//        img.setDepth(C);
////        img.setFormat(Format.RGB8);
//
//        final int size = C * C * 4;
//        int ii = (int)(Math.random() * C);
//        int jj = (int)(Math.random() * size);
//        System.out.println("heat point = " + ii + "," + jj);
//        final List<ByteBuffer> data = Lists.newArrayList();
//        for (int i = 0; i < C; i++) {
//			final ByteBuffer layer = BufferUtils.createByteBuffer(size);
//        	for (int j=0; j<size; j++)
////        		layer.put((byte)(Math.random()*255));
//        	if (i == ii && j == jj) {
//        		layer.put((byte)255);
//        	} else
//        		layer.put((byte)0);
//        	layer.rewind();
////        	Image colorImage = new Image(ImageDataFormat.RGBA, ImageDataType.Byte, C, C, layer, null);
//        	final Image colorImage = GeneratedImageFactory.createSolidColorImage(ColorRGBA.randomColor(null), false, C);        	
//            data.add(colorImage.getData(0));
//            if (i == 0) {
//                img.setDataFormat(ImageDataFormat.RGBA);
//                img.setDataType(ImageDataType.Byte);
//            }            
//        }
//        img.setData(data);
//        tex.setImage(img);
//        tex.setWrap(WrapMode.BorderClamp);
//        return tex;
//    }		

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
