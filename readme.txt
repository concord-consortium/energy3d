In order to run Energy3D project in eclipse add the following JVM run parameter:

Windows:

-Djava.library.path=./exe/lib/jogl/native/windows-32 -DrunInEclipse=true

Mac:

-Djava.library.path=./exe/lib/jogl/native/mac-universal -DrunInEclipse=true

Linux:

-Djava.library.path=./exe/lib/jogl/native/linux-64 -DrunInEclipse=true