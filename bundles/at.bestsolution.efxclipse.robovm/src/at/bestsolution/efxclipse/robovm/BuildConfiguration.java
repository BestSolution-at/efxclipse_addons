package at.bestsolution.efxclipse.robovm;

import java.io.File;
import java.util.Set;

public class BuildConfiguration {
		public Set<SetupDirectory> origProjectRefSourceDirs;
		public Set<String> projectRefSourceDirs;
		public Set<SetupDirectory> origProjectSourceDirs;
		public Set<String> projectSourceDirs;
		public Set<File> origExternalLibs;
		public Set<String> externalLibs;
		public String targetCompliance;
		public String projectEncoding;
		public String sourceCompliance;
		public Object keyStore;
		public String projectName;
		public Object builderName;
		public String buildDirectory;
	}