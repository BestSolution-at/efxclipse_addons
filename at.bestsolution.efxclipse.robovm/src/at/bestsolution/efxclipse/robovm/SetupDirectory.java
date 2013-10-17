package at.bestsolution.efxclipse.robovm;

import java.io.File;

public class SetupDirectory {
	public File originalPath;
	public File relativePath;
	
	public SetupDirectory(File originalPath, File relativePath) {
		this.originalPath = originalPath;
		this.relativePath = relativePath;
	}
}
