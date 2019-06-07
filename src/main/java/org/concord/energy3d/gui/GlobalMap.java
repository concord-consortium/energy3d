package org.concord.energy3d.gui;

import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.util.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;

class GlobalMap extends JDialog {

    private static final long serialVersionUID = 1L;

    private final MapImageView mapImageView;

    GlobalMap(final JFrame owner) {

        super(owner);
        setTitle("Total Supported Regions: " + (LocationData.getInstance().getCities().length - 1));
        setResizable(false);
        mapImageView = new MapImageViewWithLocations();
        final int m = 1;
        mapImageView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(m, m, m, m), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        mapImageView.setAlignmentX(0.5f);
        mapImageView.setText("Loading...");
        mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mapImageView, BorderLayout.CENTER);
        final JPanel bottomPanel = new JPanel();
        final JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> GlobalMap.this.dispose());
        bottomPanel.add(closeButton);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        final Map<String, ArrayList<String>> countries = new TreeMap<>();
        for (final String s : LocationData.getInstance().getCities()) {
            if (!s.equals("")) {
                final String[] t = s.split(",");
                t[0] = t[0].trim();
                if (t.length == 1) {
                    final ArrayList<String> list = new ArrayList<>();
                    list.add(t[0]);
                    countries.put(t[0], list);
                } else {
                    t[1] = t[1].trim();
                    if (t[1].length() == 2) {
                        if (!countries.keySet().contains("United States")) {
                            countries.put("United States", new ArrayList<>());
                        }
                        final ArrayList<String> list = countries.get("United States");
                        list.add(s);
                    } else {
                        if (!countries.keySet().contains((t[1]))) {
                            countries.put(t[1], new ArrayList<>());
                        }
                        final ArrayList<String> list = countries.get(t[1]);
                        list.add(t[0]);
                    }
                }
            }
        }

        final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(topPanel, BorderLayout.NORTH);

        final JComboBox<String> regionsComboBox = new JComboBox<>();
        final JComboBox<String> countriesComboBox = new JComboBox<>();
        final JLabel regionsLabel = new JLabel("Regions:");
        if (Config.isMac()) {
            regionsLabel.setFont(new Font(regionsLabel.getFont().getName(), Font.PLAIN, regionsLabel.getFont().getSize() - 2));
            regionsComboBox.setFont(new Font(regionsComboBox.getFont().getName(), Font.PLAIN, regionsComboBox.getFont().getSize() - 3));
            countriesComboBox.setFont(regionsComboBox.getFont());
        }
        final ItemListener listener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (regionsComboBox.getSelectedItem() != null) {
                    if ("United States".equals(countriesComboBox.getSelectedItem())) {
                        EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem());
                    } else {
                        if (regionsComboBox.getSelectedItem().equals(countriesComboBox.getSelectedItem())) {
                            EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem());
                        } else {
                            EnergyPanel.getInstance().getCityComboBox().setSelectedItem(regionsComboBox.getSelectedItem() + ", " + countriesComboBox.getSelectedItem());
                        }
                    }
                }
                mapImageView.repaint();
            }
        };

        final JLabel countriesLabel = new JLabel("<html><font color=blue size=2><u>" + countries.size() + " Countries</u>:</html>");
        countriesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        countriesLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                StringBuilder s = new StringBuilder("<html><body><table>");
                int i = 0;
                for (final String country : countries.keySet()) {
                    switch (i % 3) {
                        case 0:
                            s.append("<tr>");
                            s.append("<td bgcolor=87CEEB><font size=3>").append(country).append("</td><td width=40 bgcolor=98FB98><font size=3>").append(countries.get(country).size()).append("</td>");
                            break;
                        case 1:
                            s.append("<td bgcolor=87CEEB><font size=3>").append(country).append("</td><td width=40 bgcolor=98FB98><font size=3>").append(countries.get(country).size()).append("</td>");
                            break;
                        case 2:
                            s.append("<td bgcolor=87CEEB><font size=3>").append(country).append("</td><td width=40 bgcolor=98FB98><font size=3>").append(countries.get(country).size()).append("</td>");
                            s.append("</tr>");
                            break;
                    }
                    i++;
                }
                s.append("</table></body></html>");
                final JTextPane textPane = new JTextPane();
                textPane.setContentType("text/html");
                textPane.setText(s.toString());
                final JScrollPane scroller = new JScrollPane(textPane);
                scroller.setPreferredSize(new Dimension(400, 400));
                JOptionPane.showMessageDialog(GlobalMap.this, scroller, "Numbers of Supported Regions in Each Country", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        topPanel.add(countriesLabel);
        for (final String s : countries.keySet()) {
            countriesComboBox.addItem(s);
        }
        countriesComboBox.addActionListener(e -> {
            regionsComboBox.removeAllItems();
            final ArrayList<String> locationsInCountry = countries.get(countriesComboBox.getSelectedItem());
            for (final String s : locationsInCountry) {
                regionsComboBox.addItem(s);
            }
            regionsLabel.setText("<html><font size=2>Regions (" + locationsInCountry.size() + "):</html>");
        });
        topPanel.add(countriesComboBox);

        topPanel.add(regionsLabel);
        topPanel.add(regionsComboBox);

        final String current = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
        if (current != null) {
            if ("".equals(current)) {
                final ArrayList<String> regionsInCountries = countries.get(countriesComboBox.getSelectedItem());
                for (final String s : regionsInCountries) {
                    regionsComboBox.addItem(s);
                }
            } else {
                final String[] t = current.split(",");
                t[0] = t[0].trim();
                if (t.length == 1) {
                    countriesComboBox.setSelectedItem(t[0]);
                    regionsComboBox.setSelectedItem(t[0]);
                } else {
                    t[1] = t[1].trim();
                    if (t[1].length() == 2) {
                        countriesComboBox.setSelectedItem("United States");
                        regionsComboBox.setSelectedItem(current);
                    } else {
                        countriesComboBox.setSelectedItem(t[1]);
                        regionsComboBox.setSelectedItem(t[0]);
                    }
                }
            }
        }

        pack();
        setLocationRelativeTo(owner);

        regionsComboBox.addItemListener(listener);

        new SwingWorker<BufferedImage, Void>() {

            @Override
            protected BufferedImage doInBackground() throws Exception {
                return ImageIO.read(new URL(MapImageView.getGoogleMapUrl("roadmap", 0, 0, 1, mapImageView.getPreferredSize().width, mapImageView.getPreferredSize().height)));
            }

            @Override
            protected void done() {
                try {
                    final BufferedImage mapImage = get();
                    final int w = mapImageView.getPreferredSize().width;
                    mapImageView.setImage(mapImage.getScaledInstance(w, w, Image.SCALE_DEFAULT));
                    mapImageView.repaint();
                } catch (final Exception e) {
                    displayError(e);
                } finally {
                    mapImageView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    mapImageView.setText(null);
                }
            }

            void displayError(final Exception e) {
                if (e instanceof CancellationException) {
                    return;
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(GlobalMap.this, "Couldn't download map from Google!\nPlease check your internet connection and try again.", getTitle(), JOptionPane.WARNING_MESSAGE);
            }

        }.execute();

    }

}