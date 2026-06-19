@echo off
set TMP=C:\tmp
set TEMP=C:\tmp
set GRADLE_LAUNCHER=%USERPROFILE%\.gradle\wrapper\dists\gradle-7.4.2-bin\48ivgl02cpt2ed3fh9dbalvx8\gradle-7.4.2\lib\gradle-launcher-7.4.2.jar
set JAVA_EXE=C:\tools\jdk-17.0.11+9\bin\java.exe

echo [planbridge-api] Starting Spring Boot on port 8080...
"%JAVA_EXE%" -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -cp "%GRADLE_LAUNCHER%" org.gradle.launcher.GradleMain bootRun --no-daemon --project-dir "%~dp0"
