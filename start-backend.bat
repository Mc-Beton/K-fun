@echo off
cd /d "%~dp0"
set SPRING_PROFILES_ACTIVE=h2
echo Starting KSeF Hub Backend with H2 database...
java -jar target\ksef-hub-1.0.0-SNAPSHOT.jar
pause
