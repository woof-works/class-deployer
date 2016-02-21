package woofworks.versioning.java;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassVersion {
	/*
	 * major minor Java platform version
	 * 45 3 1.0
	 * 45 3 1.1
	 * 46 0 1.2
	 * 47 0 1.3
	 * 48 0 1.4
	 * 49 0 1.5
	 * 50 0 1.6
	 */

	/**
	 * Mapping of major version values to the java platform version
	 */
	@SuppressWarnings("serial")
	public static Map<Integer, String> verMap = new HashMap<Integer, String>() {
		{
			put(45, "1.0");
			put(46, "1.2");
			put(47, "1.3");
			put(48, "1.4");
			put(49, "1.5");
			put(50, "1.6");
			put(51, "1.7");
			put(52, "1.8");
		}
	};

	private static Logger logger = LogManager.getLogger(ClassVersion.class);

	/**
	 * Recursively checks a directory structure for class files and returns the
	 * major minor versions
	 * 
	 * @param directory
	 *            is the path of the directory
	 * @return map containing the class files and directory structure
	 */
	public static Map<File, String> recursiveCheck(String directory) {
		Map<File, String> ver = new HashMap<File, String>();
		File dir = new File(directory);
		// If dir exists
		if (dir.exists() && dir.listFiles().length > 0) {
			// For all files / directories
			for (File f : Arrays.asList(dir.listFiles())) {
				// If it is a file
				if (f.isFile()) {
					// Get extension of the file
					String ext = f.getName().substring(f.getName().lastIndexOf('.') + 1);
					// If it is a class file
					if (ext.equalsIgnoreCase("class")) {
						ver.put(f, getVersion(f.getAbsolutePath()));
					}
				} else if (f.isDirectory()) {
					// Recurse
					Map<File, String> nodeMap = recursiveCheck(f.getAbsolutePath());
					// Save recursed map details
					ver.putAll(nodeMap);
				}
			}
		}
		return ver;
	}

	/**
	 * Reads the major and minor versions from a file and returns the values
	 * 
	 * @param filename
	 *            is the filename of the class file
	 * @return null if not a valid class file, major:minor versions if valid
	 */
	public static String getVersion(String filename) {
		int major = -1;
		int minor = -1;
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(filename));

			int magic = in.readInt();
			// Checks if the file is a valid class file
			if (magic != 0xcafebabe) {
				logger.error(filename + " is not a valid class!");
				in.close();
				return null;
			}
			minor = in.readUnsignedShort();
			major = in.readUnsignedShort();
			// System.out.println(filename + ": " + major + " . " + minor);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return major + ":" + minor;
	}

	public static void runClassCheck(String path) {
		logger.debug("===============================================");
		logger.debug("Running Java Class Check in " + path);
		Map<File, String> t = ClassVersion.recursiveCheck(path);
		logger.debug("Total class files found: " + t.size());
		Map<String, Integer> summary = new HashMap<String, Integer>();
		for (File f : t.keySet()) {
			// Print out the version of each class file
			String version = verMap.get(Integer.valueOf(t.get(f).split(":")[0]));
			logger.debug(f.getName() + " - " + version);
			// Save summary for printing later
			if (summary.containsKey(version)) {
				summary.put(version, summary.get(version) + 1);
			} else {
				summary.put(version, 1);
			}
		}
		logger.info("===============================================");
		logger.info("[ Summary of class files ]");
		for (String v : summary.keySet()) {
			logger.info("Version " + v + " - " + summary.get(v) + " files");
		}
	}
}
