for %%f in (lib\*.jar) do jarsigner -keystore cc-keystore -storepass cc1234 %%f concord
pause