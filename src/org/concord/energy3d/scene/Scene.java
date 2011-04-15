package org.concord.energy3d.scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.util.Config;

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
	public static enum Unit {
		Meter("m"), Centimeter("cm"), Inches("\"");
		private final String notation;

		private Unit(String notation) {
			this.notation = notation;
		}

		public String getNotation() {
			return notation;
		}
	};

	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("House Root");
	private static final Node originalHouseRoot = new Node("Original House Root");
	private static Scene instance;
	private static URL url = null;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();
	private double RADIUS = 0;
	transient int C; // = 20;
	transient int size; // = C * C * C * 3;
	transient ByteBuffer texBuffer;// = BufferUtils.createByteBuffer(size);
	transient Texture3D tex; // = new Texture3D();
	private transient boolean redrawAll = false;
	private Unit unit = Unit.Meter;
	private double annotationScale = 1;
	static private boolean isTextureEnabled = true;
	static private boolean drawThickness = false;

	public static Scene getInstance() {
		if (instance == null) {
			instance = new Scene();
			try {
//				instance.open(new File("./Energy3D Projects/Default.ser"));
				if (!Config.isApplet())
					instance.open(new File("Energy3D Projects" + File.separator + "Default.ser").toURI().toURL());
				else if (Config.getApplet().getParameter("file") != null)
					instance.open(new URL(Config.getApplet().getCodeBase(), Config.getApplet().getParameter("file")));
				else
					instance.open(new URL(Config.getApplet().getCodeBase(), "Energy3D Projects/Default.ser"));
			} catch (Throwable e) {
				e.printStackTrace();
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
		System.out.print("Adding new house part...");
		originalHouseRoot.attachChild(housePart.getRoot());
		parts.add(housePart);
		System.out.println("done");
	}
	
	public void remove(HousePart housePart)  {
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().remove(housePart);
		removeTree(housePart);
		if (container != null)
			container.draw();
	}

	private void removeTree(HousePart housePart) {
		System.out.println("Removing: " + housePart);
		originalHouseRoot.detachChild(housePart.getRoot());
		parts.remove(housePart);
		housePart.delete();
		for (final HousePart child : housePart.getChildren())
			removeTree(child);
		housePart.getChildren().clear();
	}
	
	public ArrayList<HousePart> getParts() {
		return parts;
	}

	public void save(final URL url) throws FileNotFoundException, IOException {
		// remove dead objects
		final Iterator<HousePart> itr = parts.iterator();
		while (itr.hasNext()) {
			HousePart part = itr.next();
			if (part instanceof Roof || part instanceof Window || part instanceof Door)
				if (part.getContainer() == null)
					itr.remove();
		}
		
		Scene.url = url;
		System.out.print("Saving " + Scene.url + "...");
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(Scene.url.toURI().getPath()));
			out.writeObject(this);
			out.close();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}

	public void newFile() {
		final PrintController printController = PrintController.getInstance();
		if (printController.isPrintPreview()) {
//			printController.setPrintPreview(false);
//			MainFrame.getInstance().getPreviewMenuItem().setSelected(false);
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!printController.isFinished())
				Thread.yield();
		}
			
		SceneManager.taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				originalHouseRoot.detachAllChildren();
				for (Spatial child : root.getChildren())
					if (child != originalHouseRoot)
						root.detachChild(child);
				root.updateWorldBound(true);
				SceneManager.getInstance().updateHeliodonSize();
				return null;
			}
		});
		parts.clear();
		url = null;
	}

	public void open(final URL file) { // throws FileNotFoundException, IOException, ClassNotFoundException {
		instance.newFile();
//		if (!file.exists()) {
//			System.out.println("File does not exist: " + file.getAbsolutePath());
//			return;
//		}
		Scene.url = file;
		SceneManager.taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				System.out.print("Opening..." + file + "...");				
				ObjectInputStream in = new ObjectInputStream(file.openStream());
				instance = (Scene) in.readObject();				
				in.close();
				instance.init();
				for (HousePart housePart : instance.getParts())
					originalHouseRoot.attachChild(housePart.getRoot());
				for (HousePart housePart : instance.getParts())
					housePart.draw();
				System.out.println("done");
				SceneManager.getInstance().updateHeliodonSize();
				return null;
			}
		});
	}

	public void drawResizeBounds() {
		for (HousePart part : parts) {
			if (part instanceof Foundation)
				part.draw();
		}
	}

	public Node getOriginalHouseRoot() {
		return originalHouseRoot;
	}

	public static URL getURL() {
		return url;
	}

	public void setAnnotationsVisible(boolean visible) {
		for (HousePart part : parts)
			part.setAnnotationsVisible(visible);
		for (HousePart part : PrintController.getInstance().getPrintParts())
			part.setAnnotationsVisible(visible);

		if (visible)
			redrawAll = true;
	}

	public void setTextureEnabled(final boolean enabled) {
		isTextureEnabled  = enabled;
		for (HousePart part : parts)
			part.updateTextureAndColor(enabled);
		for (HousePart part : PrintController.getInstance().getPrintParts())
			part.updateTextureAndColor(enabled);

		if (enabled)
			redrawAll = true;
	}
	
	public void setDrawThickness(final boolean draw) {
		drawThickness = draw; 
		redrawAll = true;
	}
	
	public boolean isDrawThickness() {
		return drawThickness;
	}
	

//	public void redrawAll() {
//		Snap.clearAnnotationDrawn();
//		for (HousePart part : parts) {
//			// part.forceInit();
//			part.draw();
//		}
//		HousePart.setFlattenTime(HousePart.getFlattenTime());
////		PrintController.getInstance().drawPrintParts();
//	}

	public void update() {
		if (redrawAll) {
			Snap.clearAnnotationDrawn();
			for (HousePart part : parts)
				part.draw();
			for (HousePart part : PrintController.getInstance().getPrintParts())
				part.draw();
//			HousePart.setFlattenTime(HousePart.getFlattenTime());
//			PrintController.getInstance().drawPrintParts();
		}
		redrawAll = false;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
		redrawAll = true;
	}

	public Unit getUnit() {
		if (unit == null)
			unit = Unit.Meter;
		return unit;
	}

	public void setAnnotationScale(double scale) {
		this.annotationScale = scale;
		this.redrawAll = true;
	}

	public double getAnnotationScale() {
		if (annotationScale == 0)
			annotationScale = 1;
		return annotationScale;
	}

	public boolean isTextureEnabled() {
		return isTextureEnabled;
	}
}
