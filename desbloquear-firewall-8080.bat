@echo off
echo Desbloqueando puerto 8080 y Java para CodeForge...
netsh advfirewall firewall add rule name="CodeForge Backend 8080" dir=in action=allow protocol=TCP localport=8080
netsh advfirewall firewall set rule name="TCP Query User{8AEE724D-BB44-4AFF-96A4-4718E50611DF}C:\program files\java\jdk-22\bin\java.exe" new enable=no
echo Listo! Puerto 8080 habilitado en el Firewall de Windows.
pause
