package org.concord.energy3d.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;

import org.concord.energy3d.model.UserData;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.geometry.primitives.Point;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.bounding.BoundingVolume.Type;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoFade;
import com.ardor3d.ui.text.BMText.AutoScale;
import com.ardor3d.util.geom.BufferUtils;

public class Util {

	// This is called by DesignReplay to suppress the error dialog when we replay a design process
	public static boolean suppressReportError = false;

	public static boolean sameDateOfYear(final Date d1, final Date d2) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		final int mon1 = cal.get(Calendar.MONTH);
		final int day1 = cal.get(Calendar.DAY_OF_MONTH);
		cal.setTime(d2);
		final int mon2 = cal.get(Calendar.MONTH);
		final int day2 = cal.get(Calendar.DAY_OF_MONTH);
		return mon1 == mon2 && day1 == day2;
	}

	public static boolean sameTimeOfDay(final Date d1, final Date d2) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		final int h1 = cal.get(Calendar.HOUR);
		final int m1 = cal.get(Calendar.MINUTE);
		cal.setTime(d2);
		final int h2 = cal.get(Calendar.HOUR);
		final int m2 = cal.get(Calendar.MINUTE);
		return h1 == h2 && m1 == m2;
	}

	/** platform-independent check for Windows' equivalent of right click of mouse button. This can be used as an alternative as MouseEvent.isPopupTrigger(), which requires checking within both mousePressed() and mouseReleased() methods. */
	public static boolean isRightClick(final MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			return true;
		}
		if (Config.isMac() && e.isControlDown()) {
			return true;
		}
		return false;
	}

	public static double sum(final double[] array) {
		double s = 0;
		for (final double x : array) {
			s += x;
		}
		return s;
	}

	public static int countMatch(final Matcher m) {
		int count = 0;
		while (m.find()) {
			count++;
		}
		m.reset();
		return count;
	}

	public static String firstMatch(final String regex, final String string) {
		final Matcher matcher = Pattern.compile(regex).matcher(string);
		while (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String lastMatch(final String regex, final String string) {
		final Matcher matcher = Pattern.compile(regex).matcher(string);
		String lastMatch = null;
		while (matcher.find()) {
			lastMatch = matcher.group();
		}
		return lastMatch;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map, final boolean ascending) {
		final List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(final Map.Entry<K, V> o1, final Map.Entry<K, V> o2) {
				return (ascending ? 1 : -1) * o1.getValue().compareTo(o2.getValue());
			}
		});

		final Map<K, V> result = new LinkedHashMap<K, V>();
		for (final Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static BufferedImage resizeImage(final BufferedImage bi, final int newW, final int newH) {
		final Image tmp = bi.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		final BufferedImage bi2 = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = bi2.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return bi2;
	}

	public static boolean isRGBEqual(final ReadOnlyColorRGBA c1, final ReadOnlyColorRGBA c2) {
		return Util.isEqual(c1.getRed(), c2.getRed()) && Util.isEqual(c1.getGreen(), c2.getGreen()) && Util.isEqual(c1.getBlue(), c2.getBlue());
	}

	public static ColorRGBA getColorRGB(final int r, final int g, final int b) {
		return new ColorRGBA(r / 255f, g / 255f, b / 255f, 1f);
	}

	public static ColorRGBA getColorRGBA(final int r, final int g, final int b, final int a) {
		return new ColorRGBA(r / 255f, g / 255f, b / 255f, a / 255f);
	}

	public static double[][] cloneArray(final double[][] src) {
		final int length = src.length;
		final double[][] target = new double[length][src[0].length];
		for (int i = 0; i < length; i++) {
			System.arraycopy(src[i], 0, target[i], 0, src[i].length);
		}
		return target;
	}

	public static int roundToPowerOfTwo(final int n) {
		if (isPowerOfTwo(n)) {
			return n;
		}
		return (int) Math.pow(2.0, Math.ceil(Math.log(n) / Math.log(2)));
	}

	public static boolean isPowerOfTwo(final int n) {
		return (n & (n - 1)) == 0;
	}

	public static double toUsUValue(final double siUValue) {
		return siUValue / 5.67826;
	}

	public static double toSiRValue(final double usRValue) {
		return usRValue / 5.67826;
	}

	public static double toUsRValue(final double siUValue) {
		return 5.67826 / siUValue;
	}

	public static String toString(final ReadOnlyColorRGBA c) {
		if (c == null) {
			return "#000000";
		}
		return String.format("#%02x%02x%02x", Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
	}

	public static double findBoundLength(final BoundingVolume bounds) {
		return 2 * bounds.asType(Type.Sphere).getRadius();
	}

	/** require that a and b are normalized */
	public static double angleBetween(final ReadOnlyVector3 a, final ReadOnlyVector3 b, final ReadOnlyVector3 n) {
		return Math.atan2(b.dot(n.cross(a, null)), b.dot(a));
	}

	public static String toString(final ReadOnlyVector3 v) {
		final double C = 1000.0;
		return "(" + Math.round(v.getX() * C) / C + ", " + Math.round(v.getY() * C) / C + ", " + Math.round(v.getZ() * C) / C + ")";
	}

	public static String toString2D(final ReadOnlyVector3 v) {
		final double C = 1000.0;
		return "(" + Math.round(v.getX() * C) / C + ", " + Math.round(v.getY() * C) / C + ")";
	}

	public static void disablePickShadowLight(final Spatial spatial) {
		spatial.getSceneHints().setAllPickingHints(false);
		spatial.getSceneHints().setCastsShadows(false);
		spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);
	}

	public static void disablePickShadowLight(final Spatial spatial, final boolean disablePick) {
		spatial.getSceneHints().setPickingHint(PickingHint.Pickable, !disablePick);
		spatial.getSceneHints().setCastsShadows(false);
		spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);
	}

	public static boolean insidePolygon(final ReadOnlyVector3 p, final List<? extends ReadOnlyVector3> polygon) {
		int counter = 0;
		int i;
		double xinters;
		ReadOnlyVector3 p1, p2;

		final int n = polygon.size();
		p1 = polygon.get(0);
		for (i = 1; i <= n; i++) {
			p2 = polygon.get(i % n);
			if (p.getY() > Math.min(p1.getY(), p2.getY())) {
				if (p.getY() <= Math.max(p1.getY(), p2.getY())) {
					if (p.getX() <= Math.max(p1.getX(), p2.getX())) {
						if (p1.getY() != p2.getY()) {
							xinters = (p.getY() - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY()) + p1.getX();
							if (p1.getX() == p2.getX() || p.getX() <= xinters) {
								counter++;
							}
						}
					}
				}
			}
			p1 = p2;
		}

		if (counter % 2 == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean insidePolygon(final Point p, final List<? extends Point> polygon) {
		return makePath2D(polygon).contains(new Point2D.Double(p.getX(), p.getY()));
	}

	public static Path2D makePath2D(final List<? extends Point> polygon) {
		final Path2D path = new Path2D.Double();
		path.moveTo(polygon.get(0).getX(), polygon.get(0).getY());
		for (int i = 1; i < polygon.size(); i++) {
			final Point point = polygon.get(i);
			path.lineTo(point.getX(), point.getY());
		}
		path.closePath();
		return path;
	}

	/** A function to check whether point P(x, y) lies inside the triangle formed by A(x1, y1), B(x2, y2) and C(x3, y3) */
	public static boolean isPointInsideTriangle(final ReadOnlyVector2 p, final ReadOnlyVector2 p1, final ReadOnlyVector2 p2, final ReadOnlyVector2 p3) {
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(p1.getX(), p1.getY());
		path.lineTo(p2.getX(), p2.getY());
		path.lineTo(p3.getX(), p3.getY());
		path.closePath();
		return path.contains(p.getX(), p.getY());
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		return closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final ReadOnlyVector3 p2, final ReadOnlyVector3 v2) {
		final double EPS = 0.0001;
		Vector3 p12;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p12 = p1.subtract(p2, null);
		if (Math.abs(v1.length()) < EPS || Math.abs(v2.length()) < EPS) {
			return null;
		}

		d1343 = p12.getX() * v2.getX() + p12.getY() * v2.getY() + p12.getZ() * v2.getZ();
		d4321 = v2.getX() * v1.getX() + v2.getY() * v1.getY() + v2.getZ() * v1.getZ();
		d1321 = p12.getX() * v1.getX() + p12.getY() * v1.getY() + p12.getZ() * v1.getZ();
		d4343 = v2.getX() * v2.getX() + v2.getY() * v2.getY() + v2.getZ() * v2.getZ();
		d2121 = v1.getX() * v1.getX() + v1.getY() * v1.getY() + v1.getZ() * v1.getZ();

		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS) {
			return null;
		}
		numer = d1343 * d4321 - d1321 * d4343;

		final double mua = numer / denom;
		return new Vector3(p1.getX() + mua * v1.getX(), p1.getY() + mua * v1.getY(), p1.getZ() + mua * v1.getZ());
	}

	public static Vector2 projectPointOnLine(final ReadOnlyVector2 point, final ReadOnlyVector2 p1, final ReadOnlyVector2 p2, final boolean limitToLineSegment) {
		final double t = projectPointOnLineScale(point, p1, p2);
		if (limitToLineSegment && t < 0.0) {
			return p1.clone();
		} else if (limitToLineSegment && t > 1.0) {
			return p2.clone();
		} else {
			return p2.subtract(p1, null).multiplyLocal(t).addLocal(p1); // v + t * (w - v);
		}
	}

	public static double projectPointOnLineScale(final ReadOnlyVector2 point, final ReadOnlyVector2 p1, final ReadOnlyVector2 p2) {
		final double l2 = p1.distanceSquared(p2);
		if (l2 == 0.0) {
			return 0.0;
		}
		final double t = point.subtract(p1, null).dot(p2.subtract(p1, null)) / l2; // dot(p - v, w - v) / l2;
		return t;
	}

	/** This is automatically evaluated as either 2D projection on xy plane or 1D projection on z axis */
	public static Vector3 projectPointOnLine(final ReadOnlyVector3 point, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final boolean limitToLineSegment) {
		// return projectPointOnLine(new Vector2(point.getX(), point.getY()), new Vector2(p1.getX(), p1.getY()), new Vector2(p2.getX(), p2.getY()), false);
		final double t = projectPointOnLineScale(point, p1, p2);
		if (limitToLineSegment && t < 0.0) {
			return p1.clone();
		} else if (limitToLineSegment && t > 1.0) {
			return p2.clone();
		} else {
			return p2.subtract(p1, null).multiplyLocal(t).addLocal(p1); // v + t * (w - v);
		}
	}

	/** This is automatically evaluated as either 2D projection on xy plane or 1D projection on z axis */
	public static double projectPointOnLineScale(final ReadOnlyVector3 point, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2) {
		final boolean isHorizontal = Util.isZero(p2.subtract(p1, null).normalizeLocal().getZ());
		if (isHorizontal) {
			return Util.projectPointOnLineScale(new Vector2(point.getX(), point.getY()), new Vector2(p1.getX(), p1.getY()), new Vector2(p2.getX(), p2.getY()));
		} else {
			return Util.projectPointOnLineScale(new Vector2(0, point.getZ()), new Vector2(0, p1.getZ()), new Vector2(0, p2.getZ()));
		}
	}

	public static Vector2 snapToPolygon(final ReadOnlyVector3 point, final List<ReadOnlyVector3> polygon, final List<ReadOnlyVector3> wallNormals) {
		final Vector2 p = new Vector2(point.getX(), point.getY());
		final Vector2 l1 = new Vector2();
		final Vector2 l2 = new Vector2();
		double shortestDistance = Double.MAX_VALUE;
		Vector2 closestPoint = null;
		ReadOnlyVector3 closestNormal = null;
		final int n = polygon.size();
		for (int i = 0; i < n; i++) {
			final ReadOnlyVector3 pp1 = polygon.get(i);
			l1.set(pp1.getX(), pp1.getY());
			final ReadOnlyVector3 pp2 = polygon.get((i + 1) % n);
			l2.set(pp2.getX(), pp2.getY());
			if (l1.distanceSquared(l2) > MathUtils.ZERO_TOLERANCE) {
				final Vector2 pointOnLine = projectPointOnLine(p, l1, l2, true);
				final double distance = pointOnLine.distanceSquared(p);
				if (distance < shortestDistance) {
					shortestDistance = distance;
					closestPoint = pointOnLine;
					if (wallNormals != null) {
						if (l1.distanceSquared(closestPoint) <= l2.distanceSquared(pointOnLine)) {
							closestNormal = wallNormals.get(i);
						} else {
							closestNormal = wallNormals.get((i + 1) % n);
						}
					}
				}
			}
		}

		if (wallNormals != null) {
			closestPoint.addLocal(-closestNormal.getX() / 100.0, -closestNormal.getY() / 100.0);
		}

		return closestPoint;
	}

	public static Vector3 intersectLineSegments(final ReadOnlyVector3 a1, final ReadOnlyVector3 a2, final ReadOnlyVector3 b1, final ReadOnlyVector3 b2) {
		final double ua_t = (b2.getX() - b1.getX()) * (a1.getY() - b1.getY()) - (b2.getY() - b1.getY()) * (a1.getX() - b1.getX());
		final double ub_t = (a2.getX() - a1.getX()) * (a1.getY() - b1.getY()) - (a2.getY() - a1.getY()) * (a1.getX() - b1.getX());
		final double u_b = (b2.getY() - b1.getY()) * (a2.getX() - a1.getX()) - (b2.getX() - b1.getX()) * (a2.getY() - a1.getY());

		if (u_b != 0) {
			final double ua = ua_t / u_b;
			final double ub = ub_t / u_b;

			if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
				return new Vector3(a1.getX() + ua * (a2.getX() - a1.getX()), a1.getY() + ua * (a2.getY() - a1.getY()), a1.getZ());
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static void initHousePartLabel(final BMText label) {
		label.setFontScale(0.6);
		label.setAutoScale(AutoScale.FixedScreenSize);
		label.setAutoFade(AutoFade.Off);
		Util.disablePickShadowLight(label);
	}

	/** @return true if all the elements are zero */
	public static boolean isZero(final FloatBuffer buf) {
		final boolean b = true;
		buf.rewind();
		while (buf.hasRemaining()) {
			if (!Util.isZero(buf.get())) {
				return false;
			}
		}
		return b;
	}

	public static double round(final double x) {
		return Math.round(x * 100.0) / 100.0;
	}

	public static boolean isVertexBufferEqual(final Mesh m1, final Mesh m2) {
		return isEqual(m1.getMeshData().getVertexBuffer(), m2.getMeshData().getVertexBuffer());
	}

	public static boolean isEqual(final FloatBuffer b1, final FloatBuffer b2) {
		if (b1 == b2) {
			return true;
		}
		final int n = b1.limit();
		if (n != b2.limit()) {
			return false;
		}
		for (int i = 0; i < n; i++) {
			if (!Util.isZero(b1.get(i) - b2.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEqual(final OrientedBoundingBox b1, final OrientedBoundingBox b2, final double tolerance) {
		if (b1 == b2) {
			return true;
		}
		// only need to check the x and y axes. once these two are identical, the z axes must be identical as well
		return isEqualFaster(b1.getCenter(), b2.getCenter(), tolerance) && isEqualFaster(b1.getExtent(), b2.getExtent(), tolerance) && isEqualFaster(b1.getXAxis(), b2.getXAxis(), tolerance) && isEqualFaster(b1.getYAxis(), b2.getYAxis(), tolerance);
	}

	// this method does not use square root and should be faster
	public static boolean isEqualFaster(final ReadOnlyVector3 a, final ReadOnlyVector3 b) {
		return isZero(a.getX() - b.getX()) && isZero(a.getY() - b.getY()) && isZero(a.getZ() - b.getZ());
	}

	// this method does not use square root and should be faster
	public static boolean isEqualFaster(final ReadOnlyVector3 a, final ReadOnlyVector3 b, final double tolerance) {
		return Math.abs(a.getX() - b.getX()) < tolerance && Math.abs(a.getY() - b.getY()) < tolerance && Math.abs(a.getZ() - b.getZ()) < tolerance;
	}

	public static boolean isEqual(final ReadOnlyVector3 a, final ReadOnlyVector3 b) {
		return a.distance(b) < MathUtils.ZERO_TOLERANCE;
	}

	public static boolean isEqual(final ReadOnlyVector3 a, final ReadOnlyVector3 b, final double tolerance) {
		return a.distance(b) < tolerance;
	}

	public static boolean isEqual(final ReadOnlyVector2 a, final ReadOnlyVector2 b) {
		return a.distance(b) < MathUtils.ZERO_TOLERANCE;
	}

	public static boolean isEqual(final ReadOnlyVector2 a, final ReadOnlyVector2 b, final double tolerance) {
		return a.distance(b) < tolerance;
	}

	public static boolean isEqual(final double a, final double b) {
		return isZero(a - b);
	}

	public static boolean isEqual(final double a, final double b, final double tolerance) {
		return Math.abs(a - b) < tolerance;
	}

	public static boolean isZero(final double x) {
		return Math.abs(x) < MathUtils.ZERO_TOLERANCE;
	}

	public final static void openBrowser(final URL url) {
		openBrowser(url.toString());
	}

	public final static void openBrowser(final String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			final String os = System.getProperty("os.name");
			try {
				if (os.startsWith("Windows")) {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				} else if (os.startsWith("Mac OS")) {
					Runtime.getRuntime().exec(new String[] { "open", url });
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** return the file name of this path */
	public static String getFileName(final String path) {
		if (path == null) {
			return null;
		}
		int i = path.lastIndexOf("/");
		if (i == -1) {
			i = path.lastIndexOf("\\");
		}
		if (i == -1) {
			i = path.lastIndexOf(System.getProperty("file.separator"));
		}
		if (i == -1) {
			return path;
		}
		return path.substring(i + 1, path.length());
	}

	/**
	 * This method sets the selection state of a button visually without invoking its ItemListeners and ActionListeners
	 */
	public static void selectSilently(final AbstractButton button, final boolean selected) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ItemListener[] itemListeners = button.getItemListeners();
				final ActionListener[] actionListeners = button.getActionListeners();
				for (final ItemListener x : itemListeners) {
					button.removeItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					button.removeActionListener(x);
				}
				button.setSelected(selected);
				for (final ItemListener x : itemListeners) {
					button.addItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					button.addActionListener(x);
				}
			}
		});
	}

	/**
	 * This method sets the selection state of a combo box visually without invoking its ItemListeners and ActionListeners
	 */
	public static void selectSilently(final JComboBox<?> comboBox, final int selectedIndex) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ItemListener[] itemListeners = comboBox.getItemListeners();
				final ActionListener[] actionListeners = comboBox.getActionListeners();
				for (final ItemListener x : itemListeners) {
					comboBox.removeItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					comboBox.removeActionListener(x);
				}
				comboBox.setSelectedIndex(selectedIndex);
				for (final ItemListener x : itemListeners) {
					comboBox.addItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					comboBox.addActionListener(x);
				}
			}
		});
	}

	/**
	 * This method sets the selection state of a combo box visually without invoking its ItemListeners and ActionListeners
	 */
	public static void selectSilently(final JComboBox<?> comboBox, final Object item) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ItemListener[] itemListeners = comboBox.getItemListeners();
				final ActionListener[] actionListeners = comboBox.getActionListeners();
				for (final ItemListener x : itemListeners) {
					comboBox.removeItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					comboBox.removeActionListener(x);
				}
				comboBox.setSelectedItem(item);
				for (final ItemListener x : itemListeners) {
					comboBox.addItemListener(x);
				}
				for (final ActionListener x : actionListeners) {
					comboBox.addActionListener(x);
				}
			}
		});
	}

	/**
	 * This method sets the value of a spinner visually without invoking its ChangeListeners
	 */
	public static void setSilently(final JSpinner spinner, final Object value) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ChangeListener[] changeListeners = spinner.getChangeListeners();
				for (final ChangeListener x : changeListeners) {
					spinner.removeChangeListener(x);
				}
				spinner.setValue(value);
				final JComponent editor = spinner.getEditor();
				if (editor instanceof JSpinner.DateEditor) {
					final JSpinner.DateEditor dateEditor = (JSpinner.DateEditor) editor;
					dateEditor.getTextField().setText(dateEditor.getFormat().format(value));
				} else if (editor instanceof JSpinner.NumberEditor) {
					final JSpinner.NumberEditor numberEditor = (JSpinner.NumberEditor) editor;
					numberEditor.getTextField().setText(numberEditor.getFormat().format(value));
				} else {
					((DefaultEditor) spinner.getEditor()).getTextField().setText("" + value);
				}
				for (final ChangeListener x : changeListeners) {
					spinner.addChangeListener(x);
				}
			}
		});
	}

	/**
	 * This method sets the value of a slider visually without invoking its ChangeListeners
	 */
	public static void setSilently(final JSlider slider, final int value) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ChangeListener[] changeListeners = slider.getChangeListeners();
				for (final ChangeListener x : changeListeners) {
					slider.removeChangeListener(x);
				}
				slider.setValue(value);
				for (final ChangeListener x : changeListeners) {
					slider.addChangeListener(x);
				}
			}
		});
	}

	/**
	 * This method selects a tab from a tabbed pane without invoking its ChangeListeners
	 */
	public static void setSilently(final JTabbedPane tabbedPane, final Component value) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ChangeListener[] changeListeners = tabbedPane.getChangeListeners();
				for (final ChangeListener x : changeListeners) {
					tabbedPane.removeChangeListener(x);
				}
				tabbedPane.setSelectedComponent(value);
				for (final ChangeListener x : changeListeners) {
					tabbedPane.addChangeListener(x);
				}
				tabbedPane.repaint();
			}
		});
	}

	/**
	 * This method sets the text of a text component without invoking its DocumentListeners
	 */
	public static void setSilently(final JTextComponent tc, final String text) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final AbstractDocument doc = (AbstractDocument) tc.getDocument();
				final DocumentListener[] documentListeners = doc.getDocumentListeners();
				for (final DocumentListener x : documentListeners) {
					doc.removeDocumentListener(x);
				}
				tc.setText(text);
				for (final DocumentListener x : documentListeners) {
					doc.addDocumentListener(x);
				}
			}
		});
	}

	public static JButton getButtonSubComponent(final Container container) {
		if (container instanceof JButton) {
			return (JButton) container;
		} else {
			final Component[] components = container.getComponents();
			for (final Component component : components) {
				if (component instanceof Container) {
					return getButtonSubComponent((Container) component);
				}
			}
		}
		return null;
	}

	// area3D_Polygon(): compute the area of a 3D planar polygon
	// Input: int n = the number of vertices in the polygon
	// Point* points = an array of n+1 points in a 2D plane with V[n]=V[0]
	// Point normal = a normal vector of the polygon's plane
	// Return: the (float) area of the polygon
	public static double area3D_Polygon(final List<ReadOnlyVector3> points, final ReadOnlyVector3 normal) {
		final int n = points.size() - 1;
		double area = 0;
		double an, ax, ay, az; // abs value of normal and its coords
		int coord; // coord to ignore: 1=x, 2=y, 3=z
		int i, j, k; // loop indices

		if (n < 3) {
			return 0; // a degenerate polygon
		}

		// select largest abs coordinate to ignore for projection
		ax = (normal.getX() > 0 ? normal.getX() : -normal.getX()); // abs x-coord
		ay = (normal.getY() > 0 ? normal.getY() : -normal.getY()); // abs y-coord
		az = (normal.getZ() > 0 ? normal.getZ() : -normal.getZ()); // abs z-coord

		coord = 3; // ignore z-coord
		if (ax > ay) {
			if (ax > az) {
				coord = 1; // ignore x-coord
			}
		} else if (ay > az) {
			coord = 2; // ignore y-coord
		}

		// compute area of the 2D projection
		switch (coord) {
		case 1:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (points.get(i).getY() * (points.get(j).getZ() - points.get(k).getZ()));
			}
			break;
		case 2:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (points.get(i).getZ() * (points.get(j).getX() - points.get(k).getX()));
			}
			break;
		case 3:
			for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
				area += (points.get(i).getX() * (points.get(j).getY() - points.get(k).getY()));
			}
			break;
		}
		switch (coord) { // wrap-around term
		case 1:
			area += (points.get(n).getY() * (points.get(1).getZ() - points.get(n - 1).getZ()));
			break;
		case 2:
			area += (points.get(n).getZ() * (points.get(1).getX() - points.get(n - 1).getX()));
			break;
		case 3:
			area += (points.get(n).getX() * (points.get(1).getY() - points.get(n - 1).getY()));
			break;
		}

		// scale to get area before projection
		an = Math.sqrt(ax * ax + ay * ay + az * az); // length of normal vector
		switch (coord) {
		case 1:
			area *= (an / (2 * normal.getX()));
			break;
		case 2:
			area *= (an / (2 * normal.getY()));
			break;
		case 3:
			area *= (an / (2 * normal.getZ()));
		}
		return Math.abs(area);
	}

	public static double computeArea(final Mesh mesh) {
		double area = 0.0;
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		while (buf.hasRemaining()) {
			final Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
			final Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
			final Vector3 p3 = new Vector3(buf.get(), buf.get(), buf.get());
			final double trigArea = computeTriangleArea(p1, p2, p3);
			area += trigArea;
		}
		return area;
	}

	public static double getAreaOf2DPolygon(final double[] x, final double[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("x and y must have the same length");
		}
		float area = 0;
		final int n = x.length;
		for (int i = 0; i < n - 1; i++) {
			area += x[i] * y[i + 1] - x[i + 1] * y[i];
		}
		area += x[n - 1] * y[0] - x[0] * y[n - 1];
		return Math.abs(area) * 0.5f;
	}

	/**
	 * Approximately calculate the center of a mesh using the averange of all the triangle vertices
	 */
	public static Vector3 computeCenter(final Mesh mesh) {
		double x = 0, y = 0, z = 0;
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		int count = 0;
		while (buf.hasRemaining()) {
			final Vector3 p1 = new Vector3(buf.get(), buf.get(), buf.get());
			final Vector3 p2 = new Vector3(buf.get(), buf.get(), buf.get());
			final Vector3 p3 = new Vector3(buf.get(), buf.get(), buf.get());
			x += (p1.getX() + p2.getX() + p3.getX()) / 3;
			y += (p1.getY() + p2.getY() + p3.getY()) / 3;
			z += (p1.getZ() + p2.getZ() + p3.getZ()) / 3;
			count++;
		}
		return new Vector3(x / count, y / count, z / count);
	}

	private static double computeTriangleArea(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		final double s = Scene.getInstance().getScale();
		return p3.subtract(p1, null).crossLocal(p3.subtract(p2, null)).length() * s * s / 2.0;
	}

	public static int getHashCode(final ReadOnlyVector3 p, final ReadOnlyVector3 direction) {
		return Arrays.hashCode(new double[] { 5 + Math.signum(p.getX()), 5 + Math.signum(p.getY()), p.getX(), p.getY(), p.getZ(), direction.getX(), direction.getY(), direction.getZ() });
	}

	public static void addPointToQuad(final ReadOnlyVector3 normal, final ReadOnlyVector3 v1, final ReadOnlyVector3 v2, final Vector3 dir, final FloatBuffer vertexBuffer, final FloatBuffer normalBuffer) {
		final ReadOnlyVector3 p1 = new Vector3(v1).addLocal(dir);
		final ReadOnlyVector3 p3 = new Vector3(v2).addLocal(dir);
		dir.negateLocal();
		final ReadOnlyVector3 p2 = new Vector3(v1).addLocal(dir);
		final ReadOnlyVector3 p4 = new Vector3(v2).addLocal(dir);
		vertexBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		vertexBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		vertexBuffer.put(p4.getXf()).put(p4.getYf()).put(p4.getZf());
		vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		for (int i = 0; i < 4; i++) {
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		}
	}

	public static Point2D.Double segmentIntersects(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final double x4, final double y4) {
		final double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // lines are parallel
			return null;
		}
		final double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
		final double v = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
		if (u >= 0 && u <= 1 && v >= 0 && v <= 1) { // get the intersection point
			return new Point2D.Double(x1 + u * (x2 - x1), y1 + u * (y2 - y1));
		}
		return null;
	}

	/** traverse the node to obtain all the meshes */
	public static void getMeshes(final Node node, final List<Mesh> meshes) {
		final List<Spatial> children = node.getChildren();
		if (!children.isEmpty()) {
			for (final Spatial c : children) {
				if (c instanceof Node) {
					getMeshes((Node) c, meshes);
				} else {
					if (c instanceof Mesh) {
						meshes.add((Mesh) c);
					}
				}
			}
		}
	}

	public static OrientedBoundingBox getOrientedBoundingBox(final Mesh mesh) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		final FloatBuffer newbuf = BufferUtils.createFloatBuffer(buf.limit());
		while (buf.hasRemaining()) {
			final Vector3 v = new Vector3(buf.get(), buf.get(), buf.get());
			mesh.getWorldTransform().applyForward(v);
			newbuf.put(v.getXf()).put(v.getYf()).put(v.getZf());
		}
		final OrientedBoundingBox boundingBox = new OrientedBoundingBox();
		boundingBox.computeFromPoints(newbuf);
		boundingBox.transform(mesh.getWorldTransform().invert(null), mesh.getModelBound());
		mesh.updateWorldBound(true);
		return boundingBox;
	}

	public static OrientedBoundingBox getOrientedBoundingBox(final Node node) {
		int count = 0;
		for (final Spatial s : node.getChildren()) {
			if (s instanceof Mesh) {
				final Mesh m = (Mesh) s;
				count += m.getMeshData().getVertexBuffer().limit();
			}
		}
		final FloatBuffer newbuf = BufferUtils.createFloatBuffer(count);
		for (final Spatial s : node.getChildren()) {
			if (s instanceof Mesh) {
				final Mesh m = (Mesh) s;
				final FloatBuffer buf = m.getMeshData().getVertexBuffer();
				buf.rewind();
				while (buf.hasRemaining()) {
					final Vector3 v = new Vector3(buf.get(), buf.get(), buf.get());
					m.getWorldTransform().applyForward(v);
					newbuf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				}
			}
		}
		final OrientedBoundingBox boundingBox = new OrientedBoundingBox();
		boundingBox.computeFromPoints(newbuf);
		boundingBox.transform(node.getWorldTransform().invert(null), node.getWorldBound());
		// node.updateWorldBound(true);
		return boundingBox;
	}

	public static Point2D.Double[] get2DPoints(final OrientedBoundingBox box) {
		final Point2D.Double[] points = new Point2D.Double[4];
		final ReadOnlyVector3 center = box.getCenter();
		final ReadOnlyVector3 extent = box.getExtent();
		final ReadOnlyVector3 vx = box.getXAxis().multiply(extent.getX(), null);
		final ReadOnlyVector3 vy = box.getYAxis().multiply(extent.getY(), null);
		// (1, 1)
		double x = center.getX() + vx.getX();
		double y = center.getY() + vy.getY();
		points[0] = new Point2D.Double(x, y);
		// (-1, 1)
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		points[1] = new Point2D.Double(x, y);
		// (-1, -1)
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		points[2] = new Point2D.Double(x, y);
		// (1, -1)
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		points[3] = new Point2D.Double(x, y);
		return points;
	}

	public static void drawBoundingBox(final Spatial spatial, final Line boundingBox) {
		OrientedBoundingBox box = null;
		if (spatial instanceof Mesh) {
			box = getOrientedBoundingBox((Mesh) spatial);
		} else if (spatial instanceof Node) {
			box = getOrientedBoundingBox((Node) spatial);
		} else {
			return;
		}
		FloatBuffer buf = boundingBox.getMeshData().getVertexBuffer();
		if (buf == null || buf.capacity() != 24) {
			buf = BufferUtils.createVector3Buffer(24);
			boundingBox.getMeshData().setVertexBuffer(buf);
		} else {
			buf.rewind();
			buf.limit(buf.capacity());
		}
		final ReadOnlyVector3 center = box.getCenter();
		final ReadOnlyVector3 extent = box.getExtent();
		final ReadOnlyVector3 vx = box.getXAxis().multiply(extent.getX(), null);
		final ReadOnlyVector3 vy = box.getYAxis().multiply(extent.getY(), null);
		final ReadOnlyVector3 vz = box.getZAxis().multiply(extent.getZ(), null);
		double x, y, z;

		// #1: (1, 1, 1) to (-1, 1, 1)
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #2: (1, 1, 1) to (1, -1, 1)
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #3: (1, 1, 1) to (1, 1, -1)
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #4: (-1, -1, -1) to (1, -1, -1)
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #5: (-1, -1, -1) to (-1, 1, -1)
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #6: (-1, -1, -1) to (-1, -1, 1)
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #7: (-1, 1, 1) to (-1, -1, 1)
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #8: (-1, 1, 1) to (-1, 1, -1)
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #9: (1, -1, 1) to (-1, -1, 1)
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #10: (1, -1, 1) to (1, -1, -1)
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() + vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #11: (1, 1, -1) to (-1, 1, -1)
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() - vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		// #12: (1, 1, -1) to (1, -1, -1)
		x = center.getX() + vx.getX();
		y = center.getY() + vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);
		x = center.getX() + vx.getX();
		y = center.getY() - vy.getY();
		z = center.getZ() - vz.getZ();
		buf.put((float) x).put((float) y).put((float) z);

		boundingBox.updateModelBound();
		boundingBox.setVisible(true);
	}

	/** debug method */
	public static Vector3 computeFirstNormal(final Mesh mesh) {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		if (vertexBuffer.limit() < 9) {
			return null;
		}
		final Vector3 p1 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
		final Vector3 p2 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5));
		final Vector3 p3 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8));
		final Vector3 v1 = p2.subtract(p1, null);
		final Vector3 v2 = p3.subtract(p1, null);
		final Vector3 normal = v1.cross(v2, null);
		mesh.getWorldTransform().applyForwardVector(normal); // as the vertex buffer can be relative to the node, apply the world transform to get the absolute normal
		return normal.normalizeLocal();
	}

	/** debug method */
	public static Vector3 getFirstNormalFromBuffer(final Mesh mesh) {
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		if (normalBuffer.limit() < 9) {
			return null;
		}
		return new Vector3(normalBuffer.get(0), normalBuffer.get(1), normalBuffer.get(2));
	}

	public static void reverseFace(final Mesh m) {
		if (m == null) {
			return;
		}
		final UserData u = (UserData) m.getUserData();
		u.setNormal(u.getNormal().negate(null));
		if (u.getRotatedNormal() != null) {
			u.setRotatedNormal(u.getRotatedNormal().negate(null));
		}
	}

	/** return the mesh by its mesh index, which is NOT the index of the children array of the node, but the original index of the mesh when the node is created */
	public static Mesh getMesh(final Node n, final int meshIndex) {
		for (final Spatial s : n.getChildren()) {
			final Mesh m = (Mesh) s;
			final UserData u = (UserData) m.getUserData();
			if (u.getMeshIndex() == meshIndex) {
				return m;
			}
		}
		return null;
	}

}
