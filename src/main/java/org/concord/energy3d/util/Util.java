package org.concord.energy3d.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.geometry.primitives.Point;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.bounding.BoundingVolume.Type;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoFade;
import com.ardor3d.ui.text.BMText.AutoScale;

public class Util {

	/**
	 * platform-independent check for Windows' equivalent of right click of mouse button. This can be used as an alternative as MouseEvent.isPopupTrigger(), which requires checking within both mousePressed() and mouseReleased() methods.
	 */
	public static boolean isRightClick(final MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
			return true;
		}
		if (System.getProperty("os.name").startsWith("Mac") && e.isControlDown()) {
			return true;
		}
		return false;
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
		return String.format("#%02x%02x%02x", Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
	}

	public static double findBoundLength(final BoundingVolume bounds) {
		return 2 * bounds.asType(Type.Sphere).getRadius();
	}

	/* require that a and b are normalized */
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
		final Path2D path = makePath2D(polygon);

		return path.contains(new Point2D.Double(p.getX(), p.getY()));

		// int counter = 0;
		// double xinters;
		// Point p1, p2;
		//
		// final int n = polygon.size();
		// p1 = polygon.get(0);
		// for (int i = 1; i <= n; i++) {
		// p2 = polygon.get(i % n);
		// if (p.getY() > Math.min(p1.getY(), p2.getY())) {
		// if (p.getY() <= Math.max(p1.getY(), p2.getY())) {
		// if (p.getX() <= Math.max(p1.getX(), p2.getX())) {
		// if (p1.getY() != p2.getY()) {
		// xinters = (p.getY() - p1.getY()) * (p2.getX() - p1.getX()) /
		// (p2.getY() - p1.getY()) + p1.getX();
		// if (p1.getX() == p2.getX() || p.getX() <= xinters)
		// counter++;
		// }
		// }
		// }
		// }
		// p1 = p2;
		// }
		//
		// if (counter % 2 == 0)
		// return false;
		// else
		// return true;
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

	private static double area(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3) {
		return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
	}

	/*
	 * A function to check whether point P(x, y) lies inside the triangle formed by A(x1, y1), B(x2, y2) and C(x3, y3)
	 */
	public static boolean isPointInsideTriangle(final ReadOnlyVector2 p, final ReadOnlyVector2 p1, final ReadOnlyVector2 p2, final ReadOnlyVector2 p3) {
		final double A = area(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
		final double A1 = area(p.getX(), p.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
		final double A2 = area(p1.getX(), p1.getY(), p.getX(), p.getY(), p3.getX(), p3.getY());
		final double A3 = area(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p.getX(), p.getY());
		return isEqual(A1 + A2 + A3, A);
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 p21, final ReadOnlyVector3 p3, final ReadOnlyVector3 p43) {
		final double EPS = 0.0001;
		Vector3 p13;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p13 = p1.subtract(p3, null);
		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS) {
			return null;
		}
		if (Math.abs(p21.length()) < EPS) {
			return null;
		}

		d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
		d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
		d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
		d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
		d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();

		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS) {
			return null;
		}
		numer = d1343 * d4321 - d1321 * d4343;

		final double mua = numer / denom;
		final Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());

		return pa;
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

	/*
	 * This is automatically evaluated as either 2D projection on xy plane or 1D projection on z axis
	 */
	public static Vector3 projectPointOnLine(final ReadOnlyVector3 point, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final boolean limitToLineSegment) {
		// return projectPointOnLine(new Vector2(point.getX(), point.getY()),
		// new Vector2(p1.getX(), p1.getY()), new Vector2(p2.getX(), p2.getY()),
		// false);
		final double t = projectPointOnLineScale(point, p1, p2);
		if (limitToLineSegment && t < 0.0) {
			return p1.clone();
		} else if (limitToLineSegment && t > 1.0) {
			return p2.clone();
		} else {
			return p2.subtract(p1, null).multiplyLocal(t).addLocal(p1); // v + t * (w - v);
		}
	}

	/*
	 * This is automatically evaluated as either 2D projection on xy plane or 1D projection on z axis
	 */
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

	public static double round(final double x) {
		return Math.round(x * 100.0) / 100.0;
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

	public static boolean isEqual(final double a, final double b) {
		return isZero(a - b);
	}

	public static boolean isZero(final double x) {
		return Math.abs(x) < MathUtils.ZERO_TOLERANCE;
	}

	// This is called by DesignReplay to suppress the error dialog when we replay a design process
	public static boolean suppressReportError = false;

	public static void reportError(final Throwable e) {
		if (suppressReportError) {
			return;
		}
		e.printStackTrace();
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		final String msg = sw.toString();
		final JPanel panel = new JPanel(new BorderLayout(10, 10));
		final JScrollPane scrollPane = new JScrollPane(new JTextArea(msg));
		scrollPane.setPreferredSize(new Dimension(400, 400));
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(new JLabel("<html><b>Report the above error message to the developers?</b></html>"), BorderLayout.SOUTH);
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION) {
			return;
		}

		try {
			String s = "{\"ip_address\":\"" + InetAddress.getLocalHost().getHostAddress() + "\"";
			s += ",";
			s += "\"os_name\":\"" + URLEncoder.encode(System.getProperty("os.name"), "UTF-8") + "\"";
			s += ",";
			s += "\"os_version\":\"" + System.getProperty("os.version") + "\"";
			s += ",";
			s += "\"energy3d_version\":\"" + MainApplication.VERSION + "\"";
			s += ",";
			s += "\"error_message\":\"" + URLEncoder.encode(msg, "UTF-8") + "\"}";
			final URL url = new URL("https://staff.concord.org/~emcelroy/error/error.php?error=" + s);
			System.out.println(url);
			final URLConnection urlConnection = url.openConnection();
			urlConnection.setDoOutput(true);
			final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String receipt = "";
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line != null) {
					receipt += line;
				}
			}
			in.close();
			if ("success".equalsIgnoreCase(receipt)) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error message received. Thank you!", "Notice", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			// backup solution
			final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(new StringSelection(msg), null);
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html><h1>Error message copied</h1>Please paste it in your email and send it to qxie@concord.org.<br>Thanks for your help for this open-source project!</html>", "Noficiation", JOptionPane.INFORMATION_MESSAGE);
		}

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
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		return p3.subtract(p1, null).crossLocal(p3.subtract(p2, null)).length() * annotationScale * annotationScale / 2.0;
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

}
