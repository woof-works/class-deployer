package woofworks.model;

import java.util.Set;

public class DeployPackage {
	/**
	 * Base directory to deploy the files to
	 * <p/>
	 * E.g. "D:/Development/workspace/.../.../deployments/
	 */
	public String baseDirectory;

	/**
	 * Deployment directory to deploy the files to
	 * <p/>
	 * E.g. /20121010-1
	 */
	public String deploymentDirectory;

	/**
	 * Directory to deploy the package explicitly to
	 * <p/>
	 * E.g. /ROOT
	 */
	public String packageDirectory;

	/**
	 * Names of the files that need to be deployed
	 */
	public Set<String> fileNames;

	/**
	 * Directory in which to get the files from.<br/>
	 * This directory can be the root directory or the specific directory.
	 */
	public String sourceDirectory;
}
