package org.concord.energy3d.model;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart {
	static private final long serialVersionUID = 1L;
	static protected final double GRID_SIZE = 0.5;
	static public final double OVERHANG_LENGHT = 0.2;
	// transient protected Mesh bottomMesh;
	transient protected Node flattenedMeshesRoot;
	transient private ArrayList<PolygonPoint> wallUpperPoints;
	transient private ArrayList<ReadOnlyVector3> wallNormals;
	transient private Map<Node, ReadOnlyVector3> orgCenters;
	transient private Line wireframeMesh;
	private ArrayList<Wall> gableWalls;
	transient private ArrayList<Wall> walls;
	transient protected Map<Integer, Wall> gableEditPointToWallMap;

	// transient private boolean isTextureApplied;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		relativeToHorizontal = true; // TODO move all parameters of HousePart constructor to init
		wallUpperPoints = new ArrayList<PolygonPoint>();
		wallNormals = new ArrayList<ReadOnlyVector3>();
		walls = new ArrayList<Wall>();
		gableEditPointToWallMap = new Hashtable<Integer, Wall>();
		// isTextureApplied = false;

		flattenedMeshesRoot = new Node("Roof Meshes Root");
		final OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(1);
		offsetState.setUnits(1);
		// flattenedMeshesRoot.setRenderState(offsetState);
		root.attachChild(flattenedMeshesRoot);

		mesh = new Mesh("Roof");
		mesh.setModelBound(new BoundingBox());

		// bottomMesh = new Mesh("Roof (bottom)");
		// final CullState cullState = new CullState();
		// cullState.setCullFace(com.ardor3d.renderer.state.CullState.Face.Front);
		// bottomMesh.setRenderState(cullState);

		// Add a material to the box, to show both vertex color and lighting/shading.
		// final MaterialState ms = new MaterialState();
		// ms.setColorMaterial(ColorMaterial.Diffuse);
		// bottomMesh.setRenderState(ms);

		wireframeMesh = new Line("Roof (wireframe)");
		// wireframeMesh.setAntialiased(true);
		// final BlendState blend = new BlendState();
		// blend.setBlendEnabled(true);
		// wireframeMesh.setRenderState(blend);
		// wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		// wireframeMesh.getSceneHints().setCastsShadows(false);
		// wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		Util.disablePickShadowLight(wireframeMesh);
		root.attachChild(wireframeMesh);

		getEditPointShape(0).setDefaultColor(ColorRGBA.CYAN);

		boolean convert = false;
		// for (final ReadOnlyVector3 p : points)
		// if (p.getX() < 0 || p.getX() > 1 || p.getY() < 0 || p.getY() > 1) {
		// convert = true;
		// break;
		// }
		if (convert) {
			for (final Vector3 p : points)
				p.set(toRelative(p, container.getContainer()));
		}

	}

	protected void drawMesh() {

		try {
			if (container == null) {
				flattenedMeshesRoot.getSceneHints().setCullHint(CullHint.Always);
				wireframeMesh.getSceneHints().setCullHint(CullHint.Always);
				// bottomMesh.getSceneHints().setCullHint(CullHint.Always);
				hidePoints();
				return;
			}
			flattenedMeshesRoot.getSceneHints().setCullHint(CullHint.Inherit);
			wireframeMesh.getSceneHints().setCullHint(CullHint.Inherit);
			// final boolean bottomMeshVisible = height > 0;
			// bottomMesh.getSceneHints().setCullHint(bottomMeshVisible ? CullHint.Inherit : CullHint.Always);
			// bottomMesh.getSceneHints().setPickingHint(PickingHint.Pickable, bottomMeshVisible);

			exploreWallNeighbors((Wall) container);
			processRoofPoints(wallUpperPoints, wallNormals);
			// moveEditPointsToInsideOfRoof(wallUpperPoints);
			computeGableEditPoints();
			final Vector3 p = getAbsPoint(0);
//			if (p.getY() < -3)
				System.out.println(p);
			// updateEditShapes();

			// fillMeshWithPolygon(bottomMesh, new Polygon(wallUpperPoints));
			// if (!root.hasChild(bottomMesh))
			// root.attachChild(bottomMesh);
			// drawOverhang(wallUpperPoints);
			final Polygon polygon = makePolygon(wallUpperPoints);
			// addGablePointsToPolygon(polygon);
			fillMeshWithPolygon(mesh, polygon);

			// create roof parts
			int meshIndex = 0;
			MeshLib.groupByPlanner(mesh, flattenedMeshesRoot);
			setAnnotationsVisible(drawAnnotations);
			hideGableMeshes();
			// final FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
			// wireframeVertexBuffer.rewind();
			// wireframeVertexBuffer.limit(wireframeVertexBuffer.capacity());
			for (final Spatial child : flattenedMeshesRoot.getChildren()) {
				if (child.getSceneHints().getCullHint() != CullHint.Always) {
					final Mesh mesh = (Mesh) ((Node) child).getChild(0);
					mesh.setUserData(new UserData(this, meshIndex, false));
					// MeshLib.addConvexWireframe(wireframeVertexBuffer, mesh.getMeshData().getVertexBuffer());
					if (!Scene.getInstance().isTextureEnabled())
						mesh.setDefaultColor(defaultColor);
					final MaterialState ms = new MaterialState();
					ms.setColorMaterial(ColorMaterial.Diffuse);
					mesh.setRenderState(ms);
				}
				meshIndex++;
			}
			// wireframeVertexBuffer.limit(wireframeVertexBuffer.position());
			// wireframeMesh.getMeshData().updateVertexCount();
			// wireframeMesh.updateModelBound();

			drawWireframe();

			// if (!isTextureApplied) {
			updateTextureAndColor(Scene.getInstance().isTextureEnabled());
			// isTextureApplied = true;
			// }

			// root.updateWorldBound(true);

			root.updateGeometricState(0);

			// computeGableEditPoints();

			// drawGableWalls();

			// drawOverhang();

			// for (int i = 0; i < points.size(); i++) {
			// Vector3 p = points.get(i);
			// getEditPointShape(i).setTranslation(p);
			// }

			// updateEditShapes();

			// if (bottomMesh != null)
			// bottomMesh.updateModelBound();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// drawWalls();
	}

	protected void drawWalls() {
		if (container != null)
			((Wall) container).visitNeighbors(new WallVisitor() {
				public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
					currentWall.draw();
				}
			});
	}

	// private void addGablePointsToPolygon(final Polygon polygon) {
	// // if (gablePoints != null)
	// // for (final Vector3 p : gablePoints)
	// // polygon.addSteinerPoint(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	// }

	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			throw e;
		}
		// for (TriangulationPoint p : polygon.getPoints())
		// System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");

		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);

		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 2, new TPoint(0, 0, 0), new TPoint(1, 0, 0), new TPoint(0, 1, 0));
		mesh.getMeshData().updateVertexCount();
		mesh.updateModelBound();
	}

	private void exploreWallNeighbors(final Wall startWall) {
		walls.clear();
		wallUpperPoints.clear();
		wallNormals.clear();
		// center.set(0, 0, 0);
		// final Vector3 min = new Vector3();
		// final Vector3 max = new Vector3();
		startWall.visitNeighbors(new WallVisitor() {
			public void visit(final Wall currentWall, final Snap prevSnap, final Snap nextSnap) {
				walls.add(currentWall);
				currentWall.setRoof(Roof.this);
				final int pointIndex2;
				if (nextSnap != null)
					pointIndex2 = nextSnap.getSnapPointIndexOf(currentWall) + 1;
				else
					pointIndex2 = 0 + 1;
				final int pointIndex1 = pointIndex2 == 1 ? 3 : 1;
				final Vector3 p1 = currentWall.getAbsPoint(pointIndex1);
				final Vector3 p2 = currentWall.getAbsPoint(pointIndex2);
				final ReadOnlyVector3 normal = currentWall.getFaceDirection();
				addPointToPolygon(p1, normal);
				addPointToPolygon(p2, normal);
				// min.set(Math.min(min.getX(), p1.getX()), Math.min(min.getY(), p1.getY()), Math.min(min.getZ(), p1.getZ()));
				// min.set(Math.min(min.getX(), p2.getX()), Math.min(min.getY(), p2.getY()), Math.min(min.getZ(), p2.getZ()));
				// max.set(Math.max(max.getX(), p1.getX()), Math.max(max.getY(), p1.getY()), Math.max(max.getZ(), p1.getZ()));
				// max.set(Math.max(max.getX(), p2.getX()), Math.max(max.getY(), p2.getY()), Math.max(max.getZ(), p2.getZ()));
			}
		});

		// center.multiplyLocal(1.0 / wallUpperPoints.size());
		// max.addLocal(min).multiplyLocal(0.5);
		// center.set(max).addLocal(min).multiplyLocal(0.5).setZ(max.getZ());
		// points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
	}

	protected void addPointToPolygon(final Vector3 p, final ReadOnlyVector3 normal) {
		final PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		final int index = wallUpperPoints.indexOf(polygonPoint);
		if (index == -1) {
			wallUpperPoints.add(polygonPoint);
			// center.addLocal(p);
			wallNormals.add(normal);
		} else {
			// calculate wall normal in such a way to help in drawing overhang of roofs
			final ReadOnlyVector3 n1 = wallNormals.get(index);
			final double d = 1.0 / MathUtils.cos(n1.normalize(null).smallestAngleBetween(normal) / 2.0); // assuming thickness is 1
			final Vector3 result = n1.add(normal, null).normalizeLocal().multiplyLocal(d);
			wallNormals.set(index, result);
		}
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		return new Polygon(wallUpperPoints);
	}

	public void flattenInit() {
		if (orgCenters == null)
			orgCenters = new HashMap<Node, ReadOnlyVector3>();
		else
			orgCenters.clear();
		flatten(1.0);
		for (final Spatial mesh : flattenedMeshesRoot.getChildren()) {
			mesh.setTranslation(0, 0, 0);

			// The following code is needed because the center of bounding box is not accurate. If oriented bounding box is usde then this code is no longer required.
			final FloatBuffer buf = ((Mesh) ((Node) mesh).getChild(0)).getMeshData().getVertexBuffer();
			buf.rewind();
			final Vector3 p = new Vector3(buf.get(), buf.get(), buf.get());
			mesh.getTransform().applyForward(p);

			final Vector3 orgCenter = new Vector3(mesh.getWorldBound().getCenter());
			orgCenter.setY(p.getY());
			orgCenters.put((Node) mesh, orgCenter);
		}
	}

	public void flatten(final double flattenTime) {
		for (Spatial child : getFlattenedMeshesRoot().getChildren()) {
			if (child.getSceneHints().getCullHint() != CullHint.Always)
				flattenQuadTriangle((Node) child, flattenTime);
			// break;
		}
		root.updateGeometricState(0);
		// mesh.updateModelBound();
		// drawAnnotations();
		// if (bottomMesh != null) {
		// bottomMesh.getSceneHints().setCullHint(CullHint.Always);
		// bottomMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		// }
	}

	// private void flattenQuadTriangle(final Mesh mesh, final double flattenTime) {
	// final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
	// buf.rewind();
	// final Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
	// final Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
	// final Vector3 p3 = new Vector3(buf.get(), buf.get(), buf.get());
	//
	// final Vector3 v = new Vector3(p3).subtractLocal(p1);
	// final Vector3 normal = new Vector3(p2).subtractLocal(p1).crossLocal(v).normalizeLocal();
	// // if (normal.dot(Vector3.UNIT_Z) < 0)
	// // normal.negateLocal();
	// final double angle = normal.smallestAngleBetween(Vector3.NEG_UNIT_Y);
	// final Vector3 rotAxis = normal.cross(Vector3.NEG_UNIT_Y, null);
	// mesh.setRotation(new Matrix3().fromAngleAxis(flattenTime * angle, rotAxis));
	//
	// // if (orgCenters == null)
	// // orgCenters = new HashMap<Mesh, Vector3>();
	// ReadOnlyVector3 orgCenter = orgCenters.get(mesh);
	// // Vector3 orgCenter = new Vector3(); //orgCenters.get(mesh));
	// // // if (orgCenter == null)
	// // // orgCenter = Vector3.ZERO;
	// // // if (orgCenter == null) {
	// // final Matrix3 m = new Matrix3().fromAngleAxis(angle, rotAxis);
	// // m.applyPost(p1, p1);
	// // m.applyPost(p2, p2);
	// // m.applyPost(p3, p3);
	// // orgCenter = new Vector3(p1).addLocal(p2).addLocal(p3);
	// // while (buf.hasRemaining())
	// // orgCenter.addLocal(m.applyPost(new Vector3(buf.get(), buf.get(), buf.get()), p1));
	// // orgCenter.divideLocal(buf.capacity() / 3);
	// // mesh.updateWorldTransform(true);
	// // mesh.updateWorldBound(true);
	// // orgCenter.set(mesh.getWorldBound().getCenter());
	// // // orgCenters.put(mesh, orgCenter);
	// // // }
	// final Vector3 targetPrintCenter = ((UserData) mesh.getUserData()).getPrintCenter();
	// // if (!targetPrintCenter.equals(Vector3.ZERO) && ((UserData) mesh.getUserData()).getIndex() != 3)
	// if (!targetPrintCenter.equals(Vector3.ZERO))
	// mesh.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
	// mesh.updateWorldTransform(true);
	// mesh.updateModelBound();
	// mesh.updateWorldBound(true);
	// }

	private void flattenQuadTriangle(final Node roofPartNode, final double flattenTime) {
		final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPartNode.getUserData();
		final double angle = normal.smallestAngleBetween(Vector3.NEG_UNIT_Y);
		final Vector3 rotAxis = normal.cross(Vector3.NEG_UNIT_Y, null);
		roofPartNode.setRotation(new Matrix3().fromAngleAxis(flattenTime * angle, rotAxis));

		ReadOnlyVector3 orgCenter = orgCenters.get(roofPartNode);
		if (orgCenter == null)
			orgCenter = Vector3.ZERO;
		final Vector3 targetPrintCenter = ((UserData) ((Node) roofPartNode).getChild(0).getUserData()).getPrintCenter();
		if (!targetPrintCenter.equals(Vector3.ZERO))
			roofPartNode.setTranslation(targetPrintCenter.subtract(orgCenter, null).multiplyLocal(flattenTime));
		// roofPartNode.updateWorldTransform(true);
		// roofPartNode.updateWorldBound(true);
		// roofPartNode.updateGeometricState(0);
	}

	@Override
	protected void drawAnnotations() {
		if (container == null)
			return;

		for (Spatial roofPart : flattenedMeshesRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				int annotCounter = 0, angleAnnotCounter = 0;
				final Node roofPartNode = (Node) roofPart;
				final FloatBuffer buf = ((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer();

				final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeConvexHull(buf);

				final ReadOnlyVector3 normal = (ReadOnlyVector3) roofPart.getUserData();
				final int n = convexHull.size() - 1;
				for (int i = 0; i < n; i++) {
					final ReadOnlyVector3 p1 = convexHull.get(i);
					final ReadOnlyVector3 p2 = convexHull.get(i + 1);
					final ReadOnlyVector3 p3 = convexHull.get((i + 2) % n);

					// Size annotation
					final ReadOnlyVector3 center = p1.add(p2, null).addLocal(p3).multiplyLocal(1.0 / 3.0);
					// fetchSizeAnnot(annotCounter++, (Node) roofPartNode.getChild(1)).setRange(p2, p3, center, normal, false, Align.Center, true, true, true);
					final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++, (Node) roofPartNode.getChild(1));
					sizeAnnot.setRange(p2, p3, center, normal, false, Align.Center, true, true, Scene.isDrawAnnotationsInside());
					if (Scene.isDrawAnnotationsInside())
						sizeAnnot.setColor(ColorRGBA.WHITE);
					else
						sizeAnnot.setColor(ColorRGBA.BLACK);

					// Angle annotations
					// fetchAngleAnnot(angleAnnotCounter++, (Node) roofPartNode.getChild(2)).setRange(p2, p1, p3, normal);
					fetchAngleAnnot(angleAnnotCounter++, (Node) roofPartNode.getChild(2)).setRange(p2, p1, p3, normal);
				}
			}
		}
	}

	protected void drawWireframe() {
		if (container == null)
			return;

		final ArrayList<ArrayList<ReadOnlyVector3>> convexHulls = new ArrayList<ArrayList<ReadOnlyVector3>>();
		int totalVertices = 0;

		for (Spatial roofPart : flattenedMeshesRoot.getChildren()) {
			if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
				final Node roofPartNode = (Node) roofPart;

				final ArrayList<ReadOnlyVector3> convexHull = MeshLib.computeConvexHull(((Mesh) roofPartNode.getChild(0)).getMeshData().getVertexBuffer());
				convexHulls.add(convexHull);
				totalVertices += convexHull.size();
			}
		}

		final FloatBuffer buf;
		if (wireframeMesh.getMeshData().getVertexBuffer().capacity() >= totalVertices * 2 * 3) {
			buf = wireframeMesh.getMeshData().getVertexBuffer();
			buf.limit(buf.capacity());
			buf.rewind();
		} else {
			buf = BufferUtils.createVector3Buffer(totalVertices * 2);
			wireframeMesh.getMeshData().setVertexBuffer(buf);
		}

		for (final ArrayList<ReadOnlyVector3> convexHull : convexHulls) {
			for (int i = 0; i < convexHull.size(); i++) {
				final ReadOnlyVector3 p1 = convexHull.get(i);
				final ReadOnlyVector3 p2 = convexHull.get((i + 1) % convexHull.size());

				buf.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
				buf.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
			}
		}
		buf.limit(buf.position());
		wireframeMesh.getMeshData().updateVertexCount();
	}

	public int drawLabels(int printSequence) {
		for (Spatial roofPartNode : flattenedMeshesRoot.getChildren()) {
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
				final String text = "(" + (printSequence++ + 1) + ")";
				final BMText label = (BMText) ((Node) roofPartNode).getChild(3);
				label.getSceneHints().setCullHint(CullHint.Inherit);
				label.setText(text);
			}
		}
		return printSequence;
	}

	public void hideLabels() {
		for (Spatial roofPartNode : flattenedMeshesRoot.getChildren())
			if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always)
				((Node) roofPartNode).getChild(3).getSceneHints().setCullHint(CullHint.Always);
	}

	// public void updateTextureAndColor(final boolean textureEnabled) {
	// if (textureEnabled) {
	// final TextureState ts = new TextureState();
	// ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
	// mesh.setRenderState(ts);
	// mesh.setDefaultColor(ColorRGBA.WHITE);
	//
	// if (flattenedMeshesRoot != null) {
	// flattenedMeshesRoot.setRenderState(ts);
	// for (Spatial s : flattenedMeshesRoot.getChildren()) {
	// Mesh mesh = (Mesh) s;
	// mesh.setDefaultColor(ColorRGBA.WHITE);
	// }
	// }
	// if (bottomMesh != null) {
	// bottomMesh.setRenderState(ts);
	// bottomMesh.setDefaultColor(ColorRGBA.WHITE);
	// }
	// } else {
	// mesh.clearRenderState(StateType.Texture);
	// mesh.setDefaultColor(defaultColor);
	// if (flattenedMeshesRoot != null) {
	// flattenedMeshesRoot.clearRenderState(StateType.Texture);
	// for (Spatial s : flattenedMeshesRoot.getChildren()) {
	// Mesh mesh = (Mesh) s;
	// mesh.setDefaultColor(defaultColor);
	// }
	// }
	// if (bottomMesh != null) {
	// bottomMesh.clearRenderState(StateType.Texture);
	// bottomMesh.setDefaultColor(defaultColor);
	// }
	// }
	// }

	public void updateTextureAndColor(final boolean textureEnabled) {
		if (flattenedMeshesRoot != null) {
			for (final Spatial roofPartNode : flattenedMeshesRoot.getChildren()) {
				if (roofPartNode.getSceneHints().getCullHint() != CullHint.Always) {
					final Mesh mesh = (Mesh) ((Node) roofPartNode).getChild(0);
					if (textureEnabled) {
						final TextureState ts = new TextureState();
						ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
						mesh.setRenderState(ts);
						mesh.setDefaultColor(ColorRGBA.WHITE);

					} else {
						mesh.clearRenderState(StateType.Texture);
						mesh.setDefaultColor(defaultColor);
					}
				}
			}
		}
	}

	public Node getFlattenedMeshesRoot() {
		return flattenedMeshesRoot;
	}

	protected String getDefaultTextureFileName() {
		return "roof.jpg";
	}

	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		final Vector3 op = new Vector3();
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final PolygonPoint p = wallUpperPoints.get(i);
			op.set(wallNormals.get(i)).multiplyLocal(OVERHANG_LENGHT);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ());
		}
		// points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
	}

	public void setGable(final int index, final boolean isGable) {
		if (gableWalls == null)
			gableWalls = new ArrayList<Wall>();
		final ArrayList<Vector3> meshUpperPoints = new ArrayList<Vector3>();
		final Vector3[] meshBase = findBasePoints((Mesh) ((Node) getFlattenedMeshesRoot().getChild(index)).getChild(0), meshUpperPoints);
		final Wall wall = findGableWall(meshBase);
		for (final Vector3 meshPoint : meshUpperPoints) {
			double smallestDistanceToEditPoint = Double.MAX_VALUE;
			// Vector3 nearestEditPoint = null;
			// Vector3 nearestEditPointRel = null;
			int nearestIndex = -1;
			// select the nearest point so that one edit point per upper mesh point is selected
			for (int i = 0; i < points.size(); i++) {
				final Vector3 editPoint = getAbsPoint(i);
				final double distanceToEditPoint = meshPoint.distance(editPoint);
				if (distanceToEditPoint < smallestDistanceToEditPoint) {
					smallestDistanceToEditPoint = distanceToEditPoint;
					// nearestEditPoint = editPoint;
					// nearestEditPointRel = points.get(i);
					nearestIndex = i;
				}
			}
			gableEditPointToWallMap.put(nearestIndex, wall);

			// final Vector3 n = meshBase[1].subtract(meshBase[0], null).crossLocal(Vector3.UNIT_Z).normalizeLocal();
			// double distance = -nearestEditPoint.subtract(meshBase[0], null).dot(n);
			// distance += -Math.signum(distance) * 0.0001; // in order to avoid empty roof part caused by being slightly out of range of roof, and crazy roof that stretches to floor
			// nearestEditPoint.addLocal(n.multiply(distance, null));
			// nearestEditPointRel.set(toRelative(nearestEditPoint, container.getContainer()));
		}

		if (meshBase != null) {
			if (isGable)
				gableWalls.add(wall);
			else
				gableWalls.remove(wall);
			draw();
			drawWalls();
		}
	}

	// private void computeGableEditPoints() {
	// if (gableWalls == null)
	// return;
	// gableEditPointToWallMap.clear();
	// final ArrayList<Vector3> meshUpperPoints = new ArrayList<Vector3>();
	// for (final Wall wall : gableWalls) {
	// final Vector3[] base = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
	// for (final Spatial roofPartNode : getFlattenedMeshesRoot().getChildren()) {
	// meshUpperPoints.clear();
	// final Vector3[] meshBase = findBasePoints((Mesh) ((Node) roofPartNode).getChild(0), meshUpperPoints);
	// if (meshBase != null && isSameBasePoints(base, meshBase)) {
	// final Vector3 n = meshBase[1].subtract(meshBase[0], null).crossLocal(Vector3.UNIT_Z).normalizeLocal();
	// // final ArrayList<Vector3> gableRoofMeshEditPoints = new ArrayList<Vector3>();
	// for (final Vector3 meshPoint : meshUpperPoints) {
	// double smallestDistanceToEditPoint = Double.MAX_VALUE;
	// Vector3 nearestEditPoint = null;
	// Vector3 nearestEditPointRel = null;
	// int nearestIndex = -1;
	// // select the nearest point so that one edit point per upper mesh point is selected
	// for (int i = 0; i < points.size(); i++) {
	// final Vector3 editPoint = getAbsPoint(i);
	// final double distanceToEditPoint = meshPoint.distance(editPoint);
	// // final double distanceToEditPoint = base[0].distance(editPoint) + base[1].distance(editPoint);
	// if (distanceToEditPoint < smallestDistanceToEditPoint) {
	// smallestDistanceToEditPoint = distanceToEditPoint;
	// nearestEditPoint = editPoint;
	// nearestEditPointRel = points.get(i);
	// nearestIndex = i;
	// }
	// }
	// double distance = -nearestEditPoint.subtract(meshBase[0], null).dot(n);
	// distance += -Math.signum(distance) * 0.0001; // in order to avoid empty roof part caused by being slightly out of range of roof, and crazy roof that stretches to floor
	// // distance += -Math.signum(distance) * 0.1; // in order to avoid crazy roof that stretches to floor
	// nearestEditPoint.addLocal(n.multiply(distance, null));
	// nearestEditPointRel.set(toRelative(nearestEditPoint, container.getContainer()));
	// // gableRoofMeshEditPoints.add(nearestEditPoint);
	// gableEditPointToWallMap.put(nearestIndex, wall);
	// }
	// // computeGableWallPoints(base, gableRoofMeshEditPoints);
	// break;
	// }
	// }
	// }
	// }

	private void computeGableEditPoints() {
		if (gableWalls == null)
			return;
		// gableEditPointToWallMap.clear();
		// final ArrayList<Vector3> meshUpperPoints = new ArrayList<Vector3>();
		// for (final Wall wall : gableWalls) {
		for (final int nearestIndex : gableEditPointToWallMap.keySet()) {
			final Wall wall = gableEditPointToWallMap.get(nearestIndex);
//			final Vector3[] base = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
//			for (final Spatial roofPartNode : getFlattenedMeshesRoot().getChildren()) {
				// meshUpperPoints.clear();
//				final Vector3[] meshBase = findBasePoints((Mesh) ((Node) roofPartNode).getChild(0), null);
//				if (meshBase != null && isSameBasePoints(base, meshBase)) {
					// for (final Vector3 meshPoint : meshUpperPoints) {
					// double smallestDistanceToEditPoint = Double.MAX_VALUE;
					// Vector3 nearestEditPoint = null;
					// Vector3 nearestEditPointRel = null;
					// int nearestIndex = -1;
					// select the nearest point so that one edit point per upper mesh point is selected
					// for (int i = 0; i < points.size(); i++) {
					// final Vector3 editPoint = getAbsPoint(i);
					// final double distanceToEditPoint = meshPoint.distance(editPoint);
					// if (distanceToEditPoint < smallestDistanceToEditPoint) {
					// smallestDistanceToEditPoint = distanceToEditPoint;
					// nearestEditPoint = editPoint;
					// nearestEditPointRel = points.get(i);
					// nearestIndex = i;
					// }
					// }
					final Vector3 nearestEditPoint = getAbsPoint(nearestIndex);
//					final Vector3 n = meshBase[1].subtract(meshBase[0], null).crossLocal(Vector3.UNIT_Z).normalizeLocal();
					final ReadOnlyVector3 n = wall.getFaceDirection();
//					double distance = -nearestEditPoint.subtract(meshBase[0], null).dot(n);
					double distance = -nearestEditPoint.subtract(wall.getAbsPoint(0).addLocal(n.multiply(OVERHANG_LENGHT, null)), null).dot(n);
					// distance += -Math.signum(distance) * 0.0001; // in order to avoid empty roof part caused by being slightly out of range of roof, and crazy roof that stretches to floor
					// distance += -Math.signum(distance) * 0.001; // in order to avoid empty roof part caused by being slightly out of range of roof, and crazy roof that stretches to floor
					nearestEditPoint.addLocal(n.multiply(distance, null));
					points.get(nearestIndex).set(toRelative(nearestEditPoint, container.getContainer()));
					// gableEditPointToWallMap.put(nearestIndex, wall);
					// }
					// break;
//				}
//			}
		}
	}

	public Vector3[] findBasePoints(final Mesh mesh, final ArrayList<Vector3> storeUpperPoints) {
		final Vector3[] base = new Vector3[2];
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		System.out.println(mesh);
		while (vertexBuffer.hasRemaining()) {
			final Vector3 meshPoint = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			System.out.println(meshPoint);
			if (meshPoint.getZ() - container.getAbsPoint(1).getZ() < MathUtils.ZERO_TOLERANCE) {
				if (base[0] == null)
					base[0] = meshPoint;
				else if (!meshPoint.equals(base[0])) {
					base[1] = meshPoint;
				}
			} else if (storeUpperPoints != null)
				storeUpperPoints.add(meshPoint);
		}
		if (base[1] == null)
			return null;
		else
			return base;
	}

	// private void hideGableMeshes() {
	// if (gableBases == null)
	// return;
	// for (final Vector3[] base_i : gableBases) {
	// for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
	// final Vector3[] base = findBasePoints((Mesh) mesh, null);
	// if (base != null && isSameBasePoints(base_i, base))
	// mesh.getSceneHints().setCullHint(CullHint.Always);
	// }
	// }
	// }

	private void hideGableMeshes() {
		if (gableWalls == null)
			return;
		for (final Wall wall : gableWalls) {
			final Vector3[] base_i = { wall.getAbsPoint(0), wall.getAbsPoint(2) };
			for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
				final Vector3[] base = findBasePoints((Mesh) ((Node) mesh).getChild(0), null);
				if (base != null && isSameBasePoints(base_i, base)) {
					mesh.getSceneHints().setCullHint(CullHint.Always);
					mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
					break;
				}
			}
		}
	}

	public boolean isSameBasePoints(final Vector3[] base_1, final Vector3[] base_2) {
		// return (base_2[0].equals(base_1[0]) && base_2[1].equals(base_1[1])) || (base_2[0].equals(base_1[1]) && base_2[1].equals(base_1[0]));
		final double maxOverhangDistance = MathUtils.sqrt(2 * OVERHANG_LENGHT * OVERHANG_LENGHT) * 2;
		final Vector2 p1a = new Vector2(base_1[0].getX(), base_1[0].getY());
		final Vector2 p1b = new Vector2(base_1[1].getX(), base_1[1].getY());
		final Vector2 p2a = new Vector2(base_2[0].getX(), base_2[0].getY());
		final Vector2 p2b = new Vector2(base_2[1].getX(), base_2[1].getY());

		return (p1a.distance(p2a) <= maxOverhangDistance && p1b.distance(p2b) <= maxOverhangDistance) || (p1a.distance(p2b) <= maxOverhangDistance && p1b.distance(p2a) <= maxOverhangDistance);
	}

	// private void computeGableWallPoints(final Vector3[] targetBase, final ArrayList<Vector3> gableRoofMeshEditPoints) {
	// Wall targetWall = findGableWall(targetBase);

	// final Wall wall = targetWall;

	// if (targetWall != null) {
	// Collections.sort(gableRoofMeshEditPoints, new Comparator<Vector3>() {
	// @Override
	// public int compare(Vector3 o1, Vector3 o2) {
	// final Vector3 wallFirstPoint = wall.getAbsPoint(0);
	// if (o1.distance(wallFirstPoint) > o2.distance(wallFirstPoint))
	// return -1;
	// else
	// return 1;
	// }
	// });
	// wall.setGablePoints(gableRoofMeshEditPoints);
	// }

	// }

	// private Wall findGableWall(final Vector3[] targetBase) {
	// Wall targetWall = null;
	// final ReadOnlyVector3 targetDirection = targetBase[1].subtract(targetBase[0], null).normalizeLocal();
	// double distance = Double.MAX_VALUE;
	//
	// for (final Wall wall : walls) {
	// Vector3 p1 = wall.getAbsPoint(0);
	// Vector3 p2 = wall.getAbsPoint(2);
	// final Vector3 wallDirection = p2.subtract(p1, null).normalizeLocal();
	// double dot = wallDirection.dot(targetDirection);
	// if (dot < 0) {
	// dot = -dot;
	// final Vector3 tmp = p1;
	// p1 = p2;
	// p2 = tmp;
	// }
	// if (1 - dot < MathUtils.ZERO_TOLERANCE) {
	// final double d = Math.max(p1.distance(targetBase[0]), p2.distance(targetBase[1]));
	// if (d < distance) {
	// distance = d;
	// targetWall = wall;
	// }
	// }
	// }
	// return targetWall;
	// }

	private Wall findGableWall(final Vector3[] targetBase) {
		for (final Wall wall : walls) {
			if (isSameBasePoints(targetBase, new Vector3[] { wall.getAbsPoint(0), wall.getAbsPoint(2) }))
				return wall;
		}
		return null;
	}

	// private void drawOverhang() {
	// for (int i = 0; i < flattenedMeshesRoot.getNumberOfChildren(); i++) {
	// final Mesh mesh = (Mesh) flattenedMeshesRoot.getChild(i);
	// final ArrayList<Vector3> upperPoints = new ArrayList<Vector3>();
	// final Vector3[] base = findBasePoints(mesh, upperPoints);
	// for (final Vector3 base_i : base) {
	// Vector3 closestUpperPoint = null;
	// for (final Vector3 upperPoint : upperPoints) {
	// if (closestUpperPoint == null || closestUpperPoint.distance(base_i) > upperPoint.distance(base_i))
	// closestUpperPoint = upperPoint;
	// }
	// final Vector3 direction = base_i.subtract(closestUpperPoint, null);
	// // final Vector3 newBase_i = direction.multiply(1 + OVERHANG_LENGHT / direction.length(), null).addLocal(closestUpperPoint);
	// // final Vector3 newBase_i = direction.multiply(1 + OVERHANG_LENGHT / direction.normalize(null).dot(Vector3.NEG_UNIT_Z) / direction.length(), null).addLocal(closestUpperPoint);
	// final Vector3 newBase_i = direction.multiply(1 + OVERHANG_LENGHT / MathUtils.sin(MathUtils.acos(direction.normalize(null).dot(Vector3.NEG_UNIT_Z))) / direction.length(), null).addLocal(closestUpperPoint);
	// final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
	// for (int j = 0; j < vertexBuffer.limit(); j += 3) {
	// if (vertexBuffer.get(j) == base_i.getXf() && vertexBuffer.get(j + 1) == base_i.getYf() && vertexBuffer.get(j + 2) == base_i.getZf()) {
	// vertexBuffer.position(j);
	// vertexBuffer.put(newBase_i.getXf()).put(newBase_i.getYf()).put(newBase_i.getZf());
	// }
	// }
	// }
	//
	// }
	// }

	// public Spatial findRoofMeshOfWall(final Wall wall) {
	// final Vector3[] base = {wall.getAbsPoint(0), wall.getAbsPoint(2)};
	// for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
	// final Vector3[] meshBase = findBasePoints((Mesh) mesh, null);
	// if (meshBase != null && isSameBasePoints(base, meshBase)) {
	// return mesh;
	// }
	// }
	// return null;
	// }

	public ArrayList<Spatial> findMeshesContainingEditPoints(final ArrayList<Vector3> editpoints) {
		final ArrayList<Spatial> meshes = new ArrayList<Spatial>();
		for (final Spatial mesh : getFlattenedMeshesRoot().getChildren()) {
			boolean foundAll = true;
			for (final Vector3 p : editpoints) {
				final FloatBuffer buf = ((Mesh) mesh).getMeshData().getVertexBuffer();
				buf.rewind();
				boolean found = false;
				while (buf.hasRemaining() && !found) {
					if (p.distance(new Vector3(buf.get(), buf.get(), buf.get())) < MathUtils.ZERO_TOLERANCE)
						found = true;
				}
				if (!found) {
					foundAll = false;
					break;
				}
			}
			if (foundAll)
				meshes.add(mesh);
		}
		return meshes;
	}

	@Override
	public void delete() {
		super.delete();
		for (final Wall wall : walls)
			wall.setRoof(null);
	}

	@Override
	public void setOriginal(final HousePart original) {
		this.original = original;
		this.root.detachChild(pointsRoot);
		this.root.detachChild(wireframeMesh);
		// this.root.detachChild(flattenedMeshesRoot);
		// this.center = original.center;
		// // this.flattenedMeshesRoot = ((Roof) original).flattenedMeshesRoot.makeCopy(true);;
		// final Node originalFlattenedMeshesRoot = ((Roof)original).flattenedMeshesRoot;
		// for (int i = 0; i < originalFlattenedMeshesRoot.getNumberOfChildren(); i++) {
		// if (originalFlattenedMeshesRoot.getChild(i).getSceneHints().getCullHint() != CullHint.Always) {
		// final Node node = new Node();
		// node.attachChild(((Node) originalFlattenedMeshesRoot.getChild(i)).getChild(0).makeCopy(true));
		// node.attachChild(new Node("Roof Size Annot"));
		// node.attachChild(new Node("Roof Angle Annot"));
		// node.attachChild(((Node) originalFlattenedMeshesRoot.getChild(i)).getChild(3).makeCopy(true));
		// flattenedMeshesRoot.attachChild(node);
		// final UserData orgUserData = (UserData) ((Node) originalFlattenedMeshesRoot.getChild(i)).getChild(0).getUserData();
		// ((Node) flattenedMeshesRoot.getChild(i)).getChild(0).setUserData(new UserData(this, orgUserData.getIndex(), false));
		// flattenedMeshesRoot.getChild(i).setUserData(originalFlattenedMeshesRoot.getChild(i).getUserData());
		// flattenedMeshesRoot.updateGeometricState(0);
		// }
		// }
		// root.attachChild(flattenedMeshesRoot);
		// drawAnnotations();

		this.root.detachChild(flattenedMeshesRoot);
		// this.center = original.center;
		this.flattenedMeshesRoot = ((Roof) original).flattenedMeshesRoot.makeCopy(true);
		for (int i = 0; i < flattenedMeshesRoot.getNumberOfChildren(); i++) {
			if (flattenedMeshesRoot.getChild(i).getSceneHints().getCullHint() != CullHint.Always) {
				final UserData orgUserData = (UserData) ((Node) ((Roof) original).flattenedMeshesRoot.getChild(i)).getChild(0).getUserData();
				((Node) flattenedMeshesRoot.getChild(i)).getChild(0).setUserData(new UserData(this, orgUserData.getIndex(), false));
				flattenedMeshesRoot.getChild(i).setUserData(((Roof) original).flattenedMeshesRoot.getChild(i).getUserData());
			}
		}
		root.attachChild(flattenedMeshesRoot);
		drawAnnotations();
		root.updateWorldBound(true);
	}

	// protected boolean isPointInsideRoof(final ReadOnlyVector3 point) {
	// ReadOnlyVector3 p1, p2;
	// double dis = Double.MAX_VALUE;
	// for (final PolygonPoint pp : wallUpperPoints) {
	// Vector3 p = new Vector3(pp.getX(), pp.getY(), pp.getZ());
	// final double d = point.distance(p);
	// if (d < dis) {
	// dis = d;
	// p1 = p;
	// }
	// }
	// double dis = Double.MAX_VALUE;
	// for (final PolygonPoint pp : wallUpperPoints) {
	// Vector3 p = new Vector3(pp.getX(), pp.getY(), pp.getZ());
	// final double d = point.distance(p);
	// if (d < dis) {
	// dis = d;
	// p1 = p;
	// }
	// }
	// }

	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer());
	}

	@Override
	protected ReadOnlyVector3 getCenter() {
		// final ReadOnlyVector3 center = super.getCenter();
		// return new Vector3(center.getX(), center.getY(), container.getPoints().get(1).getZ());
		final Vector3 min = new Vector3(wallUpperPoints.get(0).getX(), wallUpperPoints.get(0).getY(), wallUpperPoints.get(0).getZ());
		final Vector3 max = new Vector3(wallUpperPoints.get(0).getX(), wallUpperPoints.get(0).getY(), wallUpperPoints.get(0).getZ());
		for (final PolygonPoint p : wallUpperPoints) {
			min.setX(Math.min(min.getX(), p.getX()));
			min.setY(Math.min(min.getY(), p.getY()));
			max.setX(Math.max(max.getX(), p.getX()));
			max.setY(Math.max(max.getY(), p.getY()));
		}
		return min.addLocal(max).multiplyLocal(new Vector3(0.5, 0.5, 0)).addLocal(0, 0, container.getPoints().get(1).getZ());
	}

	@Override
	public void setAnnotationsVisible(boolean visible) {
		super.setAnnotationsVisible(visible);
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		if (flattenedMeshesRoot != null)
			for (final Spatial roofPart : flattenedMeshesRoot.getChildren()) {
				((Node) roofPart).getChild(1).getSceneHints().setCullHint(cull);
				((Node) roofPart).getChild(2).getSceneHints().setCullHint(cull);
			}
	}

}
