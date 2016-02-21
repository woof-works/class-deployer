package woofworks.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

/**
 * File utility class that contains generic helper functions to deal with file manipulation
 * 
 * @author timothy
 * 
 */
public class FileUtil {
	private static Logger logger = LogManager.getLogger(FileUtil.class);

	/**
	 * Creates a copy of the from file to the to file
	 * 
	 * @param original
	 *            is the original file
	 * @param copy
	 *            is the copy of the original file to create
	 * @return
	 */
	public static boolean copyFile(File original, File copy) {
		logger.debug("Attempting to copy file : " + original.getAbsolutePath());
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			/*
			 * Create the respective file streams
			 */
			from = new FileInputStream(original);
			to = new FileOutputStream(copy);
			byte[] buffer = new byte[4096];
			int bytesRead;
			/*
			 * Write the copy file based on the original file
			 */
			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead); // write
			}
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			return false;
		} finally {
			/*
			 * Close the streams..
			 */
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
		logger.debug(original.getName() + " copied");
		return true;
	}

	/**
	 * Creates a directory at the specified root directory
	 * 
	 * @param dir
	 *            is the directory to create
	 * @param root
	 *            is the root to create the directory at
	 */
	public static void createDir(String dir, String root) {
		File rootDir = new File(root);
		/*
		 * Ensure that the root directory exists
		 */
		if (rootDir.exists()) {
			logger.debug("Creating: " + root + dir);
			/*
			 * If the required directory doesn't exist, create it
			 */
			if (!new File(root + dir).exists()) {
				boolean done = new File(root + dir).mkdirs();
				/*
				 * If we weren't able to create the directory, consider it a fatal error
				 */
				if (!done) {
					logger.fatal("Problem creating: " + root + dir);
				}
			} else {
				// Do not try to re-create the directory if it exists..
				logger.debug("Directory exists; Not doing anything");
			}
		} else {
			// The root must exist!
			logger.error("Doesn't exist " + rootDir.getAbsolutePath());
		}
	}

	public static List<String> findFiles(String directory, FilenameFilter filter)
			throws FileNotFoundException {
		checkNotNull(filter);
		File dir = getDirectory(directory);

		List<String> fileNames = Lists.newArrayList(dir.list(filter));
		Collections.sort(fileNames);

		return fileNames;
	}

	/**
	 * Returns a File containing the link created via the specified directory
	 * 
	 * @param directory
	 *            is the directory path to create
	 * @return
	 * @throws FileNotFoundException
	 */
	public static File getDirectory(String directory) throws FileNotFoundException {
		checkNotNull(directory);

		File dir = new File(directory);
		// Ensure that the directory exists
		if (!dir.exists()) {
			throw new FileNotFoundException("The directory " + dir.getAbsolutePath() + " does not exist.");
		}
		return dir;
	}

	public static List<File> findFiles(String dir, String file) {
		logger.debug("=========== Finding Specified Files ========");
		List<String> specifiedFiles = new ArrayList<String>();
		List<File> files = new ArrayList<File>();
		/*
		 * Multiple files using ; as a delimiter
		 */
		if (file.contains(";")) {
			specifiedFiles.addAll(Arrays.asList(file.split(";")));
		} else {
			specifiedFiles.add(file);
		}
		// This collection tracks the files that we were not able to find
		List<String> notFound = new ArrayList<String>(specifiedFiles);
		logger.debug("Specified " + specifiedFiles.size() + " files");
		/*
		 * Get all the files within the specified directory
		 */
		List<File> contents = getContents(new File(dir));
		logger.debug("Locating files within " + dir);
		for (String specifiedName : specifiedFiles) {
			for (File f : contents) {
				String fileName = f.getName();
				// Represents of the file names match
				boolean match = fileName.equals(specifiedName);

				// If the file names do not match and the file is a class file
				if (fileName.endsWith(".class") && !match) {
					/*
					 * This checks for class files that have the names BaseJob$1.class
					 * If our specified files has the file BaseJob.class, corresponding class files such as
					 * BaseJob$1.class must be deployed as well
					 */
					String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf("."));
					String specifiedNameNoExt = specifiedName.substring(0, specifiedName.lastIndexOf("."));

					match = fileNameNoExt.startsWith(specifiedNameNoExt + "$");
				}

				// Wild card match
				if (specifiedName.startsWith("*") && !match) {
					String ext = specifiedName.substring(1);
					match = fileName.endsWith(ext);
				}
				// If the file names are a match
				if (match) {
					/*
					 * If the file found is the file that we required, add in to the result collection and
					 * remove it from the notFound collection
					 * 
					 * This creates a tracker of what files we have / have not found
					 */
					logger.debug("Found " + fileName);
					files.add(f);
					notFound.remove(specifiedName);
				}
			}
		}
		/*
		 * If we missed out some files..
		 */
		if (!notFound.isEmpty()) {
			logger.fatal("Cound not find " + notFound.size() + " file(s)");
			for (String s : notFound) {
				logger.fatal(s);
			}
			throw new RuntimeException();
		}
		logger.debug("===============================================");
		return files;
	}

	public static List<File> getContents(File dir) {
		List<File> contents = new ArrayList<File>();
		if (dir.exists() && dir.listFiles().length > 0) {
			// For all files / directories
			for (File f : Arrays.asList(dir.listFiles())) {
				// If it is a file
				if (f.isFile()) {
					contents.add(f);
				} else if (f.isDirectory()) {
					// Recurse and get contents
					contents.addAll(getContents(f));
				}
			}
		}
		return contents;
	}
}
