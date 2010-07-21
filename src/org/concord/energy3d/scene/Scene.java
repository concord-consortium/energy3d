package org.concord.energy3d.scene;

import java.io.File;
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

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.EnvironmentalMapMode;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.Texture3D;
import com.ardor3d.image.util.GeneratedImageFactory;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;
import com.google.common.collect.Lists;

public class Scene implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("House Root");
	private static final Node originalHouseRoot = new Node("Original House Root");
	private static Scene instance;
	private static File file = null;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();
	// private transient ArrayList<HousePart> printParts;
	private double RADIUS = 0;
	transient int C; // = 20;
	transient int size; // = C * C * C * 3;
	transient ByteBuffer texBuffer;// = BufferUtils.createByteBuffer(size);
	transient Texture3D tex; // = new Texture3D();
	// private static double angle = 0;
	// private static Scene sceneClone = null;
	private transient boolean redrawAll = false;

	public static Scene getInstance() {
		if (instance == null) {
			instance = new Scene();
			try {
				open(new File("house.ser"));
			} catch (Throwable e) {
				instance = new Scene();
			}
			root.attachChild(originalHouseRoot);
		}
		return instance;
	}

	public static Node getRoot() {
		return root;
	}

	private Scene() {
		init();
	}

	public void init() {
		// printParts = new ArrayList<HousePart>();
		// initTexture3D();
	}

	public void initTexture3D() {
		TextureState ts;
		ts = (TextureState) root.getLocalRenderState(StateType.Texture);
		if (ts == null) {
			ts = new TextureState();
			root.setRenderState(ts);
		}
		C = 50;
		size = C * C * C * 3;
		tex = new Texture3D();
		texBuffer = BufferUtils.createByteBuffer(size);

		final Texture texture = createTexture();
		// texture.setApply(ApplyMode.Combine);
		// texture.setCombineFuncRGB(combineFuncRGB)
		texture.setEnvironmentalMapMode(EnvironmentalMapMode.ObjectLinear);
		ts.setTexture(texture);
		ts.setNeedsRefresh(true);
	}

	public void updateTexture() {
		texBuffer.position(0);
		// int color = (int)(Math.random() * 3);
		// for (int i = 0; i < size-3; i+=3) {
		// int r = color == 0 ? 255 : 0;
		// int g = color == 1 ? 255 : 0;
		// int b = color == 2 ? 255 : 0;
		// texBuffer.put((byte)r).put((byte)g).put((byte)b);
		// }

		for (int i = 0; i < C; i++) {
			for (int j = 0; j < C * C * 3; j += 3) {
				int x = j / 3 / C;
				int y = (j % (C * 3)) / 3;
				int z = (i);
				byte val = (byte) (RADIUS / new Vector3(x, y, z).subtractLocal(0, 0, 0).length());
				RADIUS = (RADIUS + 0.005) % 100000;
				// System.out.println(RADIUS);
				texBuffer.position(i * C * C * 3 + j + 0);
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
		// final Texture3D tex = new Texture3D();
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
			for (int j = 0; j < size; j += 3) {
				int x = j / 3 / C;
				int y = (j % (C * 3)) / 3;
				int z = (i);
				byte val = (byte) (RADIUS / new Vector3(x, y, z).subtractLocal(0, 0, 0).length());
				RADIUS = (RADIUS + 0.005) % 100000;
				// System.out.println(RADIUS);
				data_i.position(j + 0);
				data_i.put(val);
			}

			data_i.rewind();
			data.add(data_i);
			if (i == 0) {
				img.setDataFormat(colorImage.getDataFormat());
				img.setDataType(colorImage.getDataType());
			}
		}
		int ii = (int) (Math.random() * C);
		int jj = (int) (Math.random() * size);
		data.get(ii).put(jj, (byte) 255);
		System.out.println("heat point = " + ii + "," + jj);

		img.setData(data);
		tex.setImage(img);
		tex.setWrap(WrapMode.BorderClamp);
		return tex;
	}

	// private Texture createTexture1() {
	// final Texture3D tex = new Texture3D();
	// tex.setMinificationFilter(MinificationFilter.BilinearNoMipMaps);
	// // tex.setTextureKey(TextureKey.getKey(null, false, TextureStoreFormat.RGBA8, MinificationFilter.BilinearNoMipMaps));
	// tex.setTextureKey(TextureKey.getRTTKey(MinificationFilter.BilinearNoMipMaps));
	// final Image img = new Image();
	// final int C = 10;
	// img.setWidth(C);
	// img.setHeight(C);
	// img.setDepth(C);
	// // img.setFormat(Format.RGB8);
	//
	// final int size = C * C * 4;
	// int ii = (int)(Math.random() * C);
	// int jj = (int)(Math.random() * size);
	// System.out.println("heat point = " + ii + "," + jj);
	// final List<ByteBuffer> data = Lists.newArrayList();
	// for (int i = 0; i < C; i++) {
	// final ByteBuffer layer = BufferUtils.createByteBuffer(size);
	// for (int j=0; j<size; j++)
	// // layer.put((byte)(Math.random()*255));
	// if (i == ii && j == jj) {
	// layer.put((byte)255);
	// } else
	// layer.put((byte)0);
	// layer.rewind();
	// // Image colorImage = new Image(ImageDataFormat.RGBA, ImageDataType.Byte, C, C, layer, null);
	// final Image colorImage = GeneratedImageFactory.createSolidColorImage(ColorRGBA.randomColor(null), false, C);
	// data.add(colorImage.getData(0));
	// if (i == 0) {
	// img.setDataFormat(ImageDataFormat.RGBA);
	// img.setDataType(ImageDataType.Byte);
	// }
	// }
	// img.setData(data);
	// tex.setImage(img);
	// tex.setWrap(WrapMode.BorderClamp);
	// return tex;
	// }

	public void add(HousePart housePart) {
		originalHouseRoot.attachChild(housePart.getRoot());
		parts.add(housePart);
	}

	public void remove(HousePart housePart) {
		if (housePart == null)
			return;
		originalHouseRoot.detachChild(housePart.getRoot());
		housePart.delete();
		parts.remove(housePart);
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}

	// public ArrayList<HousePart> getPrintParts() {
	// return printParts;
	// }

	public void save(final File file) throws FileNotFoundException, IOException {
		Scene.file = file;
		// try {
		System.out.print("Saving " + file + "...");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(this);
		out.close();
		System.out.println("done");
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	public void newFile() {
		originalHouseRoot.detachAllChildren();
		for (Spatial child : root.getChildren())
			if (child != originalHouseRoot)
				root.detachChild(child);
		parts.clear();
	}

	public static void open(final File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		System.out.print("Opening..." + file);
		Scene.file = file;
		// try {
		// ObjectInputStream in = new ObjectInputStream(new FileInputStream("house.ser"));
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		instance = (Scene) in.readObject();
		instance.init();
		in.close();
		for (HousePart housePart : instance.getParts()) {
			originalHouseRoot.attachChild(housePart.getRoot());
		}
		for (HousePart housePart : instance.getParts())
			housePart.draw();
		for (HousePart housePart : instance.getParts())
			housePart.draw();

		// } catch (FileNotFoundException e) {
		// System.out.println("Energy3D saved file not found...creating a new file...");
		// instance = new Scene();
		// } catch (Throwable e) {
		// e.printStackTrace();
		// instance = new Scene();
		// }
		System.out.println("done");
	}

	public void drawResizeBounds() {
		for (HousePart part : parts) {
			if (part instanceof Foundation) {
				part.draw();
//				part.showPoints();
			}
		}
	}

	// public void flatten(final boolean flatten) {
	// if (flatten) {
	// HousePart.flattenPos = 0;
	// sceneClone = (Scene) ObjectCloner.deepCopy(this);
	// printParts.clear();
	// for (int i = 0; i < sceneClone.getParts().size(); i++) {
	// final HousePart newPart = sceneClone.getParts().get(i);
	// root.attachChild(newPart.getRoot());
	// newPart.draw();
	// newPart.setOriginal(parts.get(i));
	// if (newPart.isPrintable() && newPart.isDrawCompleted())
	// printParts.add(newPart);
	// }
	// }
	//
	// for (HousePart part : getParts())
	// part.getRoot().getSceneHints().setCullHint(CullHint.Always);
	//
	// for (double t = 0; t < 1.1; t += 0.05) {
	// // double t = 1;
	// if (flatten)
	// HousePart.setFlattenTime(t);
	// else
	// HousePart.setFlattenTime(1 - t);
	// for (HousePart part : sceneClone.getParts())
	// // TODO If draw not completed then it shouldn't even exist at this point!
	// if (part.isDrawCompleted())
	// part.draw();
	// try {
	// Thread.sleep(30);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// }
	// if (!flatten) {
	// // HousePart.setFlatten(false);
	// // for (HousePart part : parts)
	// // part.draw();
	// originalHouseRoot.setRotation(new Matrix3().fromAngles(0, 0, 0));
	// angle = 0;
	// for (HousePart housePart : sceneClone.getParts())
	// root.detachChild(housePart.getRoot());
	// }
	// originalHouseRoot.setScale(flatten ? 2 : 1);
	//
	// for (HousePart part : getParts())
	// part.getRoot().getSceneHints().setCullHint(CullHint.Inherit);
	//
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

	// public void rotate() {
	// angle += 0.01;
	// originalHouseRoot.setRotation(new Matrix3().fromAngles(0, 0, angle));
	// }

	public Spatial getOriginalHouseRoot() {
		return originalHouseRoot;
	}

	public static File getFile() {
		return file;
	}

	public void setAnnotationsVisible(boolean visible) {		
		for (HousePart part : parts)
			part.setAnnotationsVisible(visible);
		for (HousePart part : PrintController.getInstance().getPrintParts())
			part.setAnnotationsVisible(visible);
		
		if (visible)
			redrawAll = true;
	}

	public void update() {
		if (redrawAll ) {
			for (HousePart part : parts)
				part.draw();
			HousePart.setFlattenTime(HousePart.getFlattenTime());
			PrintController.getInstance().drawPrintParts();
		}
		redrawAll = false;
	}
}
