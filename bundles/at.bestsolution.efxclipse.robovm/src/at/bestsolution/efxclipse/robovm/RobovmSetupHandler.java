/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.efxclipse.robovm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class RobovmSetupHandler extends AbstractHandler {
	private static final String ROBOVM_VERSION = "0.0.5";
	private static final String OPENJFX_BACKPORT = "1.8.0.2-SNAPSHOT";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		ICompilationUnit unit = (ICompilationUnit) selection.getFirstElement();
		
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Set<IPath> sourcePaths = new HashSet<>();
		Set<IPath> refProjectSourcePaths = new HashSet<>();
		Set<IPath> libsPaths = new HashSet<>();
		
		resolveDataProject(unit.getJavaProject(), sourcePaths, refProjectSourcePaths, libsPaths);
		
		BuildConfiguration config = new BuildConfiguration();
		config.buildDirectory = "robovm-build";
		
		
		{
			Set<String> set = new HashSet<String>();
			Set<File> set2 = new HashSet<File>();
			for ( IPath p : libsPaths ) {
				set.add( p.lastSegment() );
				IFile file = root.getFile( p );
				if ( file != null && file.exists() ) {
					p = file.getLocation();
				}
				set2.add( p.toFile() );
			}
			config.externalLibs = set;
			config.origExternalLibs = set2;
		}
		
		{
			Set<String> set = new HashSet<String>();
			Set<SetupDirectory> set2 = new HashSet<SetupDirectory>();
			for ( IPath p : sourcePaths ) {
				IFolder t = root.getFolder( p );
				set.add( t.getProjectRelativePath().toString() );
				
				if( t.isLinked() ) {
					set2.add( new SetupDirectory( t.getLocation().toFile().getParentFile(), new File( t.getProjectRelativePath().toString() ) ) );
				} else {
					set2.add( new SetupDirectory( t.getProject().getLocation().toFile(), new File( t.getProjectRelativePath().toString() ) ) );  
				}				
			}
			config.projectSourceDirs = set;
			config.origProjectSourceDirs = set2;
		}
		
		{
			Set<String> set = new HashSet<String>();
			Set<SetupDirectory> set2 = new HashSet<SetupDirectory>();
			for ( IPath p : refProjectSourcePaths ) {
				IFolder t = root.getFolder( p );
				set.add( t.getProject().getName() + "/" + t.getProjectRelativePath() );
				set2.add( new SetupDirectory( t.getProject().getLocation().toFile().getParentFile(), new File( t.getProject().getName() + "/"
						+ t.getProjectRelativePath().toString() ) ) );

			}
			config.projectRefSourceDirs = set;
			config.origProjectRefSourceDirs = set2;
		}
		
		IProject p = unit.getJavaProject().getProject();
		config.projectName = getApplicationName(p);

		try {
			config.projectEncoding = p.getDefaultCharset();
			config.sourceCompliance = "1.7";//unit.getJavaProject().getOption( JavaCore.COMPILER_SOURCE, true );
			config.targetCompliance = "1.7";//unit.getJavaProject().getOption( JavaCore.COMPILER_COMPLIANCE, true );
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		IFolder buildFolder =  p.getFolder(new Path(config.buildDirectory));
		if( ! buildFolder.exists() ) {
			try {
				buildFolder.create(true, true, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		IFolder templateFolder = buildFolder.getFolder(new Path("robovm-template"));
		if( ! templateFolder.exists() ) {
			try {
				templateFolder.create(true, true, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if( ! buildFolder.getFolder(new Path("robovm")).exists() ) {
			try {
				File dir = buildFolder.getLocation().toFile();
				export("binaries/robovm-"+ROBOVM_VERSION+".tar.gz", dir.getAbsolutePath(), "robovm-"+ROBOVM_VERSION+".tar.gz");
				Process exec = Runtime.getRuntime().exec(new String[]{"tar","xzvf","robovm-"+ROBOVM_VERSION+".tar.gz"}, new String[0], dir );
				exec.waitFor();
				Files.move(new File(dir,"robovm-"+ROBOVM_VERSION).toPath(), new File(dir,"robovm").toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if( ! buildFolder.getFolder(new Path("jfx78")).exists() ) {
			try {
				File dir = new File(buildFolder.getLocation().toFile(),"jfx78");
				export("binaries/jfxrt.jar", dir.getAbsolutePath(), "jfxrt.jar");
				export("binaries/libdecora_sse_armv7.a", dir.getAbsolutePath(), "libdecora_sse_armv7.a");
				export("binaries/libglass.a", dir.getAbsolutePath(), "libglass.a");
				export("binaries/libjavafx_font.a", dir.getAbsolutePath(), "libjavafx_font.a");
				export("binaries/libjavafx_iio.a", dir.getAbsolutePath(), "libjavafx_iio.a");
				export("binaries/libprism_common.a", dir.getAbsolutePath(), "libprism_common.a");
				export("binaries/libprism_es2.a", dir.getAbsolutePath(), "libprism_es2.a");
				export("binaries/openjfx-78-backport-compat-"+OPENJFX_BACKPORT+".jar",dir.getAbsolutePath(),"openjfx-78-backport-compat.jar");
			} catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			File dir = buildFolder.getLocation().toFile();
			export("binaries/robovm-run.sh",dir.getAbsolutePath(),"robovm-run.sh");
			export("binaries/org.eclipse.fx.fxml.compiler_0.9.0-SNAPSHOT.jar", dir.getAbsolutePath(), "org.eclipse.fx.fxml.compiler.jar");
			new File(dir,"robovm-run.sh").setExecutable(true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		RobovmBuild b = new RobovmBuild();
		
		{
			IFile f = buildFolder.getFile("robovm.properties");
			fillFile(b.generateBuildProperties(config.projectName), f);
		}
		
		{
			IFile f = buildFolder.getFile("robovm.xml");
			fillFile(b.generateConfigXML(config), f);
		}
		
		{
			IFile f = buildFolder.getFile("Info.plist.xml");
			fillFile(b.generatePlistContent(), f);
		}
		
		{
			IFile f = templateFolder.getFile(config.projectName+".java");
			fillFile(b.generateBootstrapFile(config.projectName), f);
		}
		
		{
			IFile f = templateFolder.getFile(config.projectName+"Main.java");
			try {
				fillFile(b.generateFXMainFile(config.projectName, unit.getTypes()[0].getFullyQualifiedName()), f);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		{
			IFile f = buildFolder.getFile("robovm-ant.xml");
			fillFile(b.generate(config), f);
		}
		
		
//		System.err.println(sourcePaths);
//		System.err.println(refProjectSourcePaths);
//		System.err.println(libsPaths);
		
		// TODO Auto-generated method stub
		return null;
	}
	
	private void fillFile(CharSequence content, IFile f) {
		try ( ByteArrayInputStream in = new ByteArrayInputStream(content.toString().getBytes()) ) {
			if( ! f.exists() ) {
				f.create(in, IFile.KEEP_HISTORY, null);
			} else {
				f.setContents(in, IFile.KEEP_HISTORY, null);	
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private String getApplicationName(IProject p) {
		//FIXME We need to make sure the name is a valid Java-Classname
		return p.getName();
	}
	
	private void resolveDataProject( IJavaProject project, Set<IPath> listProjectSourceDirs, Set<IPath> listRefProjectSourceDirs, Set<IPath> listRefLibraries ) {
		try {
			IClasspathEntry[] entries = project.getRawClasspath();
			for ( IClasspathEntry e : entries ) {
				if ( e.getEntryKind() == IClasspathEntry.CPE_PROJECT ) {
					IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject( e.getPath().lastSegment() );
					if ( p.exists() ) {
						resolveDataProject( JavaCore.create( p ), listRefProjectSourceDirs, listRefProjectSourceDirs, listRefLibraries );
					}
				}
				else if ( e.getEntryKind() == IClasspathEntry.CPE_LIBRARY ) {
					listRefLibraries.add( e.getPath() );
				}
				else if ( e.getEntryKind() == IClasspathEntry.CPE_SOURCE ) {
					listProjectSourceDirs.add( e.getPath() );
				}
				else if ( e.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) {
					String start = e.getPath().segment( 0 );
					// TODO remove hard coded strings
					if ( !"org.eclipse.jdt.launching.JRE_CONTAINER".equals( start )
							&& !"org.eclipse.fx.ide.jdt.core.JAVAFX_CONTAINER".equals( start ) ) {
						IClasspathContainer cpe = JavaCore.getClasspathContainer( e.getPath(), project );
						IClasspathEntry[] cpEntries = cpe.getClasspathEntries();
						for ( IClasspathEntry tmp : cpEntries ) {
							if ( tmp.getEntryKind() == IClasspathEntry.CPE_LIBRARY ) {
								listRefLibraries.add( tmp.getPath() );
							}
						}
					}
				}
			}
		}
		catch ( JavaModelException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void export(String filename, String dir, String name) throws Exception {
		new File(dir).mkdirs();
		File f = new File(dir,name);
		FileOutputStream fos = new FileOutputStream(f);
		byte[] buf = new byte[1024];
		int l = 0;
		InputStream stream = RobovmSetupHandler.class.getClassLoader().getResourceAsStream(filename); 
		while( (l = stream.read(buf)) != -1 ) {
			fos.write(buf,0,l);
		}
		stream.close();
		fos.close();
	}
}
