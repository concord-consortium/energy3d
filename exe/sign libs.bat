for %%f in (lib\jogl\*.jar) do jarsigner -keystore cc-keystore -storepass cc1234 %%f concord
pause