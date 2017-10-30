package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class PvModulesData {

	private static final PvModulesData instance = new PvModulesData();
	private final Map<String, PvModuleSpecs> modules = new HashMap<String, PvModuleSpecs>();

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
				final String modelName = line.substring(0, cut).trim();
				if (!modelName.equals("Model")) {
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
			final String[] t = data.get(s).split("\\s+");
			module.setBrand(t[0].trim());
			module.setCellType(t[1].trim());
			module.setCellEfficiency(Float.parseFloat(t[2].trim()));
			module.setLength(Integer.parseInt(t[3].trim()));
			module.setWidth(Integer.parseInt(t[4].trim()));
			module.setThickness(Integer.parseInt(t[5].trim()));
			module.setLayout(Integer.parseInt(t[6].trim()), Integer.parseInt(t[7].trim()));
			module.setPmax(Float.parseFloat(t[8].trim()));
			module.setVmpp(Float.parseFloat(t[9].trim()));
			module.setImpp(Float.parseFloat(t[10].trim()));
			module.setVoc(Float.parseFloat(t[11].trim()));
			module.setIsc(Float.parseFloat(t[12].trim()));
			module.setNoct(Float.parseFloat(t[13].trim()));
			module.setWeight(Float.parseFloat(t[14].trim()));
		}

	}

	public PvModuleSpecs getModuleSpecs(final String model) {
		return modules.get(model);
	}

}
