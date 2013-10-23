package at.bestsolution.efxclipse.robovm

import java.io.File

class RobovmBuild {
	def generateBootstrapFile(String name) '''
	import javafx.application.Application;

	import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
	import org.robovm.cocoatouch.foundation.NSDictionary;
	import org.robovm.cocoatouch.uikit.UIApplication;
	import org.robovm.cocoatouch.uikit.UIApplicationDelegate;
	
	public class «name» extends UIApplicationDelegate.Adapter {
	
	    @Override
	    public boolean didFinishLaunching(UIApplication application,
	            NSDictionary launchOptions) {
	
	        Thread launchThread = new Thread() {
	            @Override
	            public void run() {
	                Application.launch(«name»Main.class);
	            }
	        };
	        launchThread.setDaemon(true);
	        launchThread.start();
	
	        return true;
	    }
	    
	    public static void main(String[] args) throws Exception {
	        System.setProperty("glass.platform", "ios");
	        NSAutoreleasePool pool = new NSAutoreleasePool();
	        UIApplication.main(args, null, «name».class);
	        pool.drain();
	    }
	}
	'''
	
	def generateFXMainFile(String name, String mobileMain) '''
	import org.eclipse.fx.ui.mobile.Deck;

	import javafx.application.Application;
	import javafx.geometry.Insets;
	import javafx.scene.Scene;
	import javafx.stage.Stage;
	import javafx.scene.layout.Region;
	
	public class «name»Main extends Application {
		@Override
		public void start(Stage arg0) throws Exception {
			«mobileMain» m = new «mobileMain»();
			Region root = (Region)m.createUI();
			root.setPadding(new Insets(30,0,0,0));
			Scene s = new Scene(root,100,100,true);
			s.getStylesheets().addAll(m.getInitialStylesheets());
			arg0.setScene(s);
			arg0.show();
		}
	
	}
	'''
	
	def generateBuildProperties(String applicationName) '''
app.version=1.0
app.id=«applicationName»
app.mainclass=«applicationName»
app.executable=«applicationName»
app.build=1
app.name=«applicationName»
	'''
	
	def generateConfigXML(BuildConfiguration buildConfig) '''
<config>
  <executableName>${app.executable}</executableName>
  <mainClass>${app.mainclass}</mainClass>
  <os>ios</os>
  <arch>thumbv7</arch>
  <resources>
    <resource>
      <directory>resources</directory>
    </resource>
  </resources>
  <target>ios</target>
  <iosInfoPList>Info.plist.xml</iosInfoPList>
  <roots>
    <root>com.sun.javafx.tk.quantum.QuantumToolkit</root>
    <root>com.sun.prism.es2.ES2Pipeline</root>
    <root>com.sun.prism.es2.IOSGLFactory</root>
    <root>com.sun.glass.ui.ios.**.*</root>
    <root>javafx.scene.CssStyleHelper</root>
    <root>com.sun.prism.shader.**.*</root>
    <root>com.sun.prism.**</root>
    <root>com.sun.javafx.font.**</root>
    <root>com.sun.scenario.effect.impl.prism.ps.**</root>
    <root>com.sun.scenario.effect.impl.es2.ES2ShaderSource</root>
    <root>application.**</root>
  </roots>
  <classpath>
  	<classpathentry>jfx78/jfxrt.jar</classpathentry>
  	<classpathentry>jfx78/openjfx-78-backport-compat.jar</classpathentry>
  	«FOR l : buildConfig.origExternalLibs»
  		<classpathentry>build/libs/«l.name»</classpathentry>
  	«ENDFOR»
  	<classpathentry>build/classes</classpathentry>
  </classpath>
  <libs>
  	<lib>jfx78/libjavafx_font.a</lib>
    <lib>jfx78/libprism_common.a</lib>
    <lib>jfx78/libprism_es2.a</lib>
    <lib>jfx78/libglass.a</lib>
    <lib>jfx78/libjavafx_iio.a</lib> 
  </libs>
  <frameworks>
    <framework>UIKit</framework>
    <framework>OpenGLES</framework>
    <framework>QuartzCore</framework>
    <framework>CoreGraphics</framework>
    <framework>CoreText</framework>
    <framework>ImageIO</framework>
    <framework>MobileCoreServices</framework>
  </frameworks>
</config>
	'''
	
		def generatePlistContent() '''
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>en</string>
    <key>CFBundleDisplayName</key>
    <string>${app.name}</string>
    <key>CFBundleExecutable</key>
    <string>${app.executable}</string>
    <key>CFBundleIdentifier</key>
    <string>${app.id}</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>${app.name}</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>${app.version}</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>CFBundleVersion</key>
    <string>${app.build}</string>
    <key>LSRequiresIPhoneOS</key>
    <true/>
    <key>UIDeviceFamily</key>
    <array>
        <integer>1</integer>
        <integer>2</integer>
    </array>
    <key>UIRequiredDeviceCapabilities</key>
    <array>
        <string>armv7</string>
    </array>
    <key>UISupportedInterfaceOrientations</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
    <key>UISupportedInterfaceOrientations~ipad</key>
    <array>
        <string>UIInterfaceOrientationPortrait</string>
        <string>UIInterfaceOrientationPortraitUpsideDown</string>
        <string>UIInterfaceOrientationLandscapeLeft</string>
        <string>UIInterfaceOrientationLandscapeRight</string>
    </array>
</dict>
</plist>
	'''
	
	def generate(BuildConfiguration config) '''
	<?xml version="1.0" encoding="UTF-8"?>
	<project name="robobuild" default="do-run-simulator-ipad" basedir=".">
		<path id="fxcompile">
			<filelist>
				<file name="build/classes"/>
				<file name="org.eclipse.fx.fxml.compiler.jar"/>
			</filelist>
			<fileset dir="build/libs">
				<include name="*.jar"/>
			</fileset>
		</path>
		
		«createLocalSetup(config)»
		«compileTarget(config)»
		«createDoRunSimulatorIPad(config)»
		«createDoRunSimulatorIPhone(config)»
		«createDoRunDevice(config)»
	</project>
	'''
	
	def createLocalSetup(BuildConfiguration config) '''
	<target name="setup-staging-area">
		<delete dir="externalLibs" />
		<delete dir="project" />
		<delete dir="projectRefs" />
		
		<mkdir dir="externalLibs" />
		
		«FOR File l : config.origExternalLibs»
		<copy todir="externalLibs">
			<fileset dir="«l.parent»">
				<filename name="«l.name»"/>	
			</fileset>
		</copy>
		«ENDFOR»
		
		<mkdir dir="project" />
		«FOR SetupDirectory d : config.origProjectSourceDirs»
		<copy todir="project">
			<fileset dir="«d.originalPath.absolutePath»">
				<include name="«d.relativePath.path»/**" />
			</fileset>
		</copy>
		«ENDFOR»
		
		<mkdir dir="projectRefs" />
		«FOR SetupDirectory d : config.origProjectRefSourceDirs»
		<copy todir="projectRefs">
			<fileset dir="«d.originalPath.absolutePath»">
				<include name="«d.relativePath.path»/**" />
			</fileset>
		</copy>
		«ENDFOR»
		
		<copy toDir="project/src">
			<fileset dir="robovm-template">
				<include name="*.java" />
			</fileset>
		</copy>
	</target>
	'''
	
	def compileTarget(BuildConfiguration config) '''
	<target name='do-compile'>
		<delete dir="build" />
		<mkdir dir="build/src" />
		<mkdir dir="build/libs" />
		<mkdir dir="build/classes" />
	
		<!-- Copy project-libs references -->
		<copy todir="build/libs">
			<fileset dir="externalLibs">
				«FOR String s : config.externalLibs»
				<include name="«s»"/>
				«ENDFOR»
			</fileset>
		</copy>
	
		<!-- Copy project references -->
		«FOR String s : config.projectRefSourceDirs»
		<copy todir="build/src">
			<fileset dir="projectRefs/«s»">
				<include name="**/*"/>
			</fileset>
		</copy>
		«ENDFOR»
	
		<!-- Copy project sources itself -->
		«FOR String s : config.projectSourceDirs»
		<copy todir="build/src">
			<fileset dir="project/«s»">
				<include name="**/*"/>
			</fileset>
		</copy>
		«ENDFOR»
	
		<javac includeantruntime="false" source="«config.sourceCompliance»" target="«config.targetCompliance»" srcdir="build/src" destdir="build/classes"«IF config.projectEncoding != null» encoding="«config.projectEncoding»"«ENDIF»>
			<classpath>
				<fileset dir="build/libs">
					<include name="*"/>
				</fileset>
				<fileset dir="robovm/lib">
					<include name="*.jar" />
				</fileset>
			</classpath>
		</javac>
		
		<!-- Transform FXML-Files -->
		<taskdef name="fxml-compiler" classpathref="fxcompile" classname="org.eclipse.fx.ide.fxml.compiler.ant.FXMLCompilerTask" />

		<fxml-compiler sourcedir="build/src" destdir="build/gen-src"/>
		<javac srcdir="build/gen-src" destdir="build/classes">
			<classpath>
				<filelist>
					<file name="build/classes"/>
				</filelist>
				<fileset dir="build/libs">
					<include name="*"/>
				</fileset>
				<fileset dir="robovm/lib">
					<include name="*.jar" />
				</fileset>
			</classpath>
		</javac>

		
		<!-- Copy over none Java-Files -->
		<copy todir="build/classes">
		«FOR String s : config.projectSourceDirs»
			<fileset dir="project/«s»">
				<exclude name="**/*.java"/>
			</fileset>
		«ENDFOR»
		</copy>
	
		«FOR String s : config.projectRefSourceDirs»
		<copy todir="build/classes">
			<fileset dir="projectRefs/«s»">
				<exclude name="**/*.java"/>
			</fileset>
		</copy>
		«ENDFOR»

	</target>
	'''
	
	def createDoRunSimulatorIPhone(BuildConfiguration config) '''
	<target name="do-run-simulator-iphone" depends="setup-staging-area, do-compile">
		<exec executable="sh">
			<arg value="robovm-run.sh"/>
			<arg value="sim-iphone"/>
			<arg value="«config.projectName»"/>
		</exec>
	</target>
	'''
	
	def createDoRunSimulatorIPad(BuildConfiguration config) '''
	<target name="do-run-simulator-ipad" depends="setup-staging-area, do-compile">
		<exec executable="sh">
			<arg value="robovm-run.sh"/>
			<arg value="sim-ipad"/>
			<arg value="«config.projectName»"/>
		</exec>
	</target>
	'''
	
	def createDoRunDevice(BuildConfiguration config) '''
	<target name="do-run-device" depends="setup-staging-area, do-compile">
		<exec executable="sh">
			<arg value="robovm-run.sh"/>
			<arg value="device"/>
			<arg value="«config.projectName»"/>
		</exec>
	</target>
	'''
	
	
}