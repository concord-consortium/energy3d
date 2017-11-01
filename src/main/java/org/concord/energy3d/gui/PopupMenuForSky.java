package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.AnnualGraph;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.undo.ChangeAtmosphericDustLossCommand;
import org.concord.energy3d.undo.ChangeThemeCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

class PopupMenuForSky extends PopupMenuFactory {

	private static JPopupMenu popupMenuForSky;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForSky == null) {

			final JMenuItem miInfo = new JMenuItem("Sky");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			final JCheckBoxMenuItem miHeliodon = new JCheckBoxMenuItem("Heliodon");
			miHeliodon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					MainPanel.getInstance().getHeliodonButton().doClick();
				}
			});

			final JMenu weatherMenu = new JMenu("Weather");
			JMenuItem mi = new JMenuItem("Monthly Sunshine Hours...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new MonthlySunshineHours().showDialog();
				}
			});
			weatherMenu.add(mi);

			mi = new JMenuItem("Annual Environmental Temperature...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new AnnualEnvironmentalTemperature().showDialog();
				}
			});
			weatherMenu.add(mi);

			mi = new JMenuItem("Daily Environmental Temperature...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new DailyEnvironmentalTemperature().showDialog();
				}
			});
			weatherMenu.add(mi);

			final JMenu themeMenu = new JMenu("Theme");
			final ButtonGroup themeButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miCloudySky = new JRadioButtonMenuItem("Cloudy Sky");
			miCloudySky.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.CLOUDY_SKY_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miCloudySky);
			themeMenu.add(miCloudySky);

			final JRadioButtonMenuItem miDesert = new JRadioButtonMenuItem("Desert");
			miDesert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.DESERT_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miDesert);
			themeMenu.add(miDesert);

			final JRadioButtonMenuItem miGrassland = new JRadioButtonMenuItem("Grassland");
			miGrassland.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.GRASSLAND_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miGrassland);
			themeMenu.add(miGrassland);

			final JRadioButtonMenuItem miForest = new JRadioButtonMenuItem("Forest");
			miForest.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.FOREST_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miForest);
			themeMenu.add(miForest);

			themeMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					Util.selectSilently(miCloudySky, Scene.getInstance().getTheme() == Scene.CLOUDY_SKY_THEME);
					Util.selectSilently(miDesert, Scene.getInstance().getTheme() == Scene.DESERT_THEME);
					Util.selectSilently(miGrassland, Scene.getInstance().getTheme() == Scene.GRASSLAND_THEME);
					Util.selectSilently(miForest, Scene.getInstance().getTheme() == Scene.FOREST_THEME);
				}
			});

			final JMenuItem miDustLoss = new JMenuItem("Dust & Pollen...");
			miDustLoss.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final JPanel gui = new JPanel(new BorderLayout());
					final String title = "<html><b>Soiling loss factor:</b><br>Loss of productivity due to atmospheric dust and pollen<br>(a dimensionless parameter within [0, 1])</html>";
					gui.add(new JLabel(title), BorderLayout.NORTH);
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					final JTextField[] fields = new JTextField[12];
					for (int i = 0; i < 12; i++) {
						final JLabel l = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i] + ": ", JLabel.LEFT);
						inputPanel.add(l);
						fields[i] = new JTextField(threeDecimalsFormat.format(Scene.getInstance().getAtmosphere().getDustLoss(i)), 5);
						l.setLabelFor(fields[i]);
						inputPanel.add(fields[i]);
					}
					SpringUtilities.makeCompactGrid(inputPanel, 12, 2, 6, 6, 6, 6);
					while (true) {
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Dust and pollen loss", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
							break;
						}
						boolean pass = true;
						final double[] val = new double[12];
						for (int i = 0; i < 12; i++) {
							try {
								val[i] = Double.parseDouble(fields[i].getText());
								if (val[i] < 0 || val[i] > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dust and pollen loss value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
									pass = false;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), fields[i].getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								pass = false;
							}
						}
						if (pass) {
							final ChangeAtmosphericDustLossCommand c = new ChangeAtmosphericDustLossCommand();
							for (int i = 0; i < 12; i++) {
								Scene.getInstance().getAtmosphere().setDustLoss(val[i], i);
							}
							updateAfterEdit();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							break;
						}
					}
				}
			});

			popupMenuForSky = new JPopupMenu();
			popupMenuForSky.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForSky.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					Util.selectSilently(miHeliodon, MainPanel.getInstance().getHeliodonButton().isSelected());
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForSky.add(miInfo);
			popupMenuForSky.add(miDustLoss);
			popupMenuForSky.add(miHeliodon);
			popupMenuForSky.addSeparator();
			popupMenuForSky.add(weatherMenu);
			popupMenuForSky.add(themeMenu);

		}

		return popupMenuForSky;

	}

}
