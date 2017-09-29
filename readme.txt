In order to run Energy3D project in eclipse add the following JVM run parameter:

Windows:

-Djava.library.path=./exe/lib/jogl/native/windows-32 -DrunInEclipse=true

Mac:

-Djava.library.path=./exe/lib/jogl/native/mac-universal -DrunInEclipse=true

Linux:

-Djava.library.path=./exe/lib/jogl/native/linux-64 -DrunInEclipse=true

-------------------------------------------------------------------------------------
Update JOGL and Ardor3D jar files:

Both JOGL and Ardor3D jar files can found in this url:
http://jogamp.org/deployment/

The JOGL files are in version folders. For example the folder "v2.3.2" contains the jar files for JOGL v2.3.2:
http://jogamp.org/deployment/v2.3.2/jar/  

The Ardor3D latest jar files are always here:
http://jogamp.org/deployment/ardor3d/

To update JOGL jar and dll files follow these steps:
	1) Go to: http://jogamp.org/deployment/
	2) Go to folder for latest version of jogl e.g. "v2.3.2"
	3) Go to "jar" folder e.g. http://jogamp.org/deployment/v2.3.2/jar/
	4) Download the following jar files (sometimes it downloads .gz instead of .jar, if so, simply unzip them with 7-zip):
		- jogl-all.jar
		- gluegen-rt.jar
	5) Copy the above jar files to exe/lib/jogl/
	6) Download the following native files:
		- jogl-all-natives-solaris-i586.jar
		- jogl-all-natives-solaris-amd64.jar
		- jogl-all-natives-macosx-universal.jar
		- gluegen-rt-natives-windows-i586.jar
		- gluegen-rt-natives-windows-amd64.jar
		- gluegen-rt-natives-macosx-universal.jar
	7) Unzip the above jar files and copy the .dll and/or .jnilib files to the following folders:
		- exe/lib/jogl/native/windows-32
		- exe/lib/jogl/native/windows-64
		- exe/lib/jogl/native/mac-universal

To update Ardor3D jar files follow these steps:
	1) Go to: http://jogamp.org/deployment/ardor3d/
	2) Download the following jar files (sometimes it downloads .gz instead of .jar, if so, simply unzip them with 7-zip):
		- ardor3d-animation-sources.jar
		- ardor3d-animation.jar
		- ardor3d-awt-sources.jar
		- ardor3d-awt.jar
		- ardor3d-collada-sources.jar
		- ardor3d-collada.jar
		- ardor3d-core-sources.jar
		- ardor3d-core.jar
		- ardor3d-effects-sources.jar
		- ardor3d-effects.jar
		- ardor3d-extras-sources.jar
		- ardor3d-extras.jar
		- ardor3d-jogl-awt-sources.jar
		- ardor3d-jogl-awt.jar
		- ardor3d-jogl-sources.jar
		- ardor3d-jogl.jar
		- ardor3d-math-sources.jar
		- ardor3d-math.jar
		- ardor3d-savable-sources.jar
		- ardor3d-savable.jar		
	3) Copy the above jar files to exe/lib/ardor3d/
-------------------------------------------------------------------------------------
Ardor3D Forum
http://forum.jogamp.org/JogAmp-s-Ardor3D-Continuation-f4037779.html

I posted in the above forum using the nickname "runiter". You can search that to find my previous posts.