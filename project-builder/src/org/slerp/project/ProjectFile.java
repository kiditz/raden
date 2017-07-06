package org.slerp.project;

public class ProjectFile {
	public String resourceName;
	public String outputName;
	/** whether to replace values in this file **/
	public boolean isTemplate;
	public String resourceLoc = "/org/slerp/project/resources/";

	public ProjectFile(String name) {
		this.resourceName = name;
		this.outputName = name;
		this.isTemplate = true;
	}

	public ProjectFile(String name, boolean isTemplate) {
		this.resourceName = name;
		this.outputName = name;
		this.isTemplate = isTemplate;
	}

	public ProjectFile(String resourceName, String outputName, boolean isTemplate) {
		this.resourceName = resourceName;
		this.outputName = outputName;
		this.isTemplate = isTemplate;
	}
}