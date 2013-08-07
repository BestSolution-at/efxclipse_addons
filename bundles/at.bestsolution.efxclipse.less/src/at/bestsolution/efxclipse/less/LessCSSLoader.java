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
package at.bestsolution.efxclipse.less;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class LessCSSLoader {
	private ScriptEngine e;
	
	public LessCSSLoader() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		e = mgr.getEngineByExtension("js");
		
		try {
			e.eval(createParserScript());
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static String createParserScript() {
		StringBuilder b = new StringBuilder();
		readFileContent(LessCSSLoader.class.getResource("env.js"), b);
		readFileContent(LessCSSLoader.class.getResource("less.js"), b);
		readFileContent(LessCSSLoader.class.getResource("parser.js"), b);
		
		return b.toString();
	}
	
	private static void readFileContent(URL url, StringBuilder b) {
		byte[] buf = new byte[1024];
		int l;
		try(InputStream in = url.openStream() ) {
			while( (l = in.read(buf)) != -1 ) {
				b.append(new String(buf,0,l));
			}
			in.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public URL loadLess(URL lessFile) {
		try {
			StringBuilder lessContent = new StringBuilder();
			readFileContent(lessFile, lessContent);
			
			Object rv = ((Invocable)e).invokeFunction("parseString", lessContent.toString());
			File f = File.createTempFile("less_", ".css");
			f.deleteOnExit();
			
			try(FileOutputStream out = new FileOutputStream(f) ) {
				out.write(rv.toString().getBytes());
				out.close();
			}
			return f.toURI().toURL();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}
