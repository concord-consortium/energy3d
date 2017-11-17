package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public final class PvModulesData {

	private static final PvModulesData instance = new PvModulesData();
	private final Map<String, PvModuleSpecs> modules = new TreeMap<String, PvModuleSpecs>();

	public static PvModulesData getInstance() {
		return instance;
	}

	private PvModulesData() {

		final Map<String, String> data = new HashMap<String, String>();
		InputStream is = null;
		final int cut = 20;
		try {
			is = getClass().getResourceAsStream("data/pvmodules.txt");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				String modelName = line.substring(0, cut).trim();
				if (!modelName.equals("Model")) {
					modelName = modelName.substring(0, modelName.length() - 1);
					data.put(modelName, line.substring(cut).trim());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		for (final String s : data.keySet()) {
			final PvModuleSpecs module = new PvModuleSpecs(s);
			modules.put(s, module);
			final String[] t = data.get(s).split(",");
			module.setBrand(t[0].trim());
			module.setCellType(t[1].trim());
			module.setCellEfficiency(Double.parseDouble(t[2].trim()));
			final String length = t[3].trim();
			final String l1 = length.substring(0, length.indexOf("("));
			final String l2 = length.substring(length.indexOf("(") + 1, length.indexOf(")"));
			module.setLength(Double.parseDouble(l1));
			module.setNominalLength(Double.parseDouble(l2));
			final String width = t[4].trim();
			final String w1 = width.substring(0, width.indexOf("("));
			final String w2 = width.substring(width.indexOf("(") + 1, width.indexOf(")"));
			module.setWidth(Double.parseDouble(w1));
			module.setNominalWidth(Double.parseDouble(w2));
			module.setThickness(Double.parseDouble(t[5].trim()));
			module.setLayout(Integer.parseInt(t[7].trim()), Integer.parseInt(t[6].trim()));
			module.setPmax(Double.parseDouble(t[8].trim()));
			module.setVmpp(Double.parseDouble(t[9].trim()));
			module.setImpp(Double.parseDouble(t[10].trim()));
			module.setVoc(Double.parseDouble(t[11].trim()));
			module.setIsc(Double.parseDouble(t[12].trim()));
			module.setPmaxTc(Double.parseDouble(t[13].trim()));
			module.setNoct(Double.parseDouble(t[14].trim()));
			module.setWeight(Double.parseDouble(t[15].trim()));
			module.setColor(t[16].trim());
			module.setShadeTolerance(t[17].trim());
			module.setPrice(Double.parseDouble(t[18].trim()));
		}

	}

	public Map<String, PvModuleSpecs> getModules() {
		return modules;
	}

	public PvModuleSpecs getModuleSpecs(final String model) {
		if ("Custom".equals(model) || model == null) {
			return new PvModuleSpecs();
		}
		return modules.get(model);
	}

}
