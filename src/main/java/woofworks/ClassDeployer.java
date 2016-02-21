package woofworks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import woofworks.util.FileUtil;
import woofworks.versioning.java.ClassVersion;

public class ClassDeployer {

	private Logger logger = LogManager.getLogger(getClass());

	public static void main(String[] args) throws Exception {
		// Set up a simple configuration that logs on the console.
		new ClassDeployer();
	}

	public ClassDeployer() throws IOException {
		String workspace = "D:/Development/workspace";

		// TODO Change this to what is required
		String deployDir = "";
		// SMOU deployment directory
		deployDir = workspace + "/project-name/deployment/release1";

		logger.debug("Checking " + deployDir);

		/*
		 * Files to be deployed
		 * These files should be the files to deploy.
		 * E.g if deploying java files, these should be classes
		 */
		genDeploys("Test1.class;" + "Test2.class;", deployDir);
		ClassVersion.runClassCheck(deployDir);
	}

	private void genDeploys(String files, String deployDir) throws IOException {
		File dir = FileUtil.getDirectory(deployDir);
		if (!dir.exists()) {
			throw new IOException("Directory " + deployDir + " doesn't exist");
		}

		List<String> summary = Lists.newArrayList();
		// TODO Change this to what is required
		/*
		 * This is the root directory where the files to deploy are stored
		 */
		String root = "";
		/*
		 * This is the directory suffix to put the files to deploy in
		 */
		String suffix = "";

		/*
		 * For SMOU deployment
		 */
		root = "D:/Development/workspace/project-name/target/ROOT";
		suffix = "/ROOT";

		logger.debug("Locating required files..");
		List<File> deployFiles = FileUtil.findFiles(root, files);
		logger.debug("Total files to deploy: " + deployFiles.size());
		logger.debug("Creating deployment files and directories..");
		int totalCopied = 0;
		for (File f : deployFiles) {
			String fileDir = f.getAbsolutePath();
			// Replace the \ with / if not it won't work for some reason
			fileDir = fileDir.replace("\\", "/");
			/*
			 * Get the "actual" file path of the file within the project
			 * E.g. D:/...com/hibernate/repo...
			 * This gives finds the com/hibernate/repo... without the actual
			 * file name
			 */
			int first = fileDir.indexOf(root);
			first += root.length();
			int last = fileDir.lastIndexOf("/");
			// Save for summary printing later
			summary.add(
					suffix + fileDir.substring(first, last) + "/" + fileDir.substring(last + 1));
			// Get the full deployment directory
			String deployFileDir = deployDir + suffix + fileDir.substring(first, last);
			logger.debug("==========================================");
			logger.debug("Deployment: " + fileDir.substring(first, last));
			// Create the directory as required
			FileUtil.createDir(suffix + fileDir.substring(first, last), deployDir);
			logger.debug("Copying file: " + fileDir.substring(last + 1));
			logger.debug("To: " + deployFileDir);
			File to = new File(deployFileDir + "/" + fileDir.substring(last + 1));
			if (FileUtil.copyFile(f, to)) {
				totalCopied++;
			}
		}
		logger.debug("Total Files Copied: " + totalCopied);
		if (totalCopied != deployFiles.size()) {
			logger.error("Missing deployment files (Specified  " + deployFiles.size()
					+ " but only copied " + totalCopied + " files");
			throw new RuntimeException();
		}
		logger.info("==========================================");
		logger.info("[ Summary of all files copied ]");
		for (String s : summary) {
			// System.out.println(s.replace(suffix, ""));
			System.out.println(s);
		}
	}

}
