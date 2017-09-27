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

To update the jar files follow these steps:
	1) Find out which jar files are needed by Energy3D by looking at these 3 folders:
		- exe/lib/ardor3d/
		- exe/lib/jogl/
	2) Download the corresponding jar files from above urls.
	3) Remove the version info from the jar file names. For example rename ardor3d-animation-1.0-SNAPSHOT.jar to ardor3d-animation.jar
	4) Copy the jar files to exe folder to replace the old jar files.

To update the dll and jnilib files:
	1) Find out which jar files are needed by Energy3D by looking at these 3 folders:
		- exe/lib/jogl/native/windows-32
		- exe/lib/jogl/native/windows-64
		- exe/lib/jogl/native/mac-universal
	2) Download the corresponding native jar files from above urls. For example download gluegen-rt-natives-windows-i586.jar
	3) Extract the native jar files and copy the dll and jnilib files to exe/lib/jogl/native/... folders to replace the old files
-------------------------------------------------------------------------------------
Ardor3D Forum
http://forum.jogamp.org/JogAmp-s-Ardor3D-Continuation-f4037779.html

I posted in the above forum using the nickname "runiter". You can search that to find my previous posts.