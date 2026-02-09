@echo off
echo Uruchamianie KSeF Hub Backend z bazą H2...
cd /d "%~dp0"
set SPRING_PROFILES_ACTIVE=h2
mvn spring-boot:run
