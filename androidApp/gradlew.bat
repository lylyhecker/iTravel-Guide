@ECHO OFF

SET DIR=%~dp0
SET APP_HOME=%DIR:~0,-1%

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" GOTO findJavaFromJavaHome

SET JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
IF %ERRORLEVEL% EQU 0 GOTO execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. >&2
GOTO fail

:findJavaFromJavaHome
SET JAVA_HOME=%JAVA_HOME:"=%
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

IF EXIST "%JAVA_EXE%" GOTO execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
GOTO fail

:execute
"%JAVA_EXE%" -Xmx64m -Xms64m -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
GOTO end

:fail
EXIT /B 1

:end
