@echo off
for /f "tokens=2 delims=:" %%i in ('find /i "key" _conf.txt') do ( set ddnskey=%%i )
for /f "tokens=2 delims=:" %%i in ('find /i "domain" _conf.txt') do ( set ddnsdomain=%%i )
for /f "tokens=2 delims=:" %%i in ('find /i "frequency" _conf.txt') do ( set ddnsfrequency=%%i )

java -jar DDNSjar.jar %ddnskey% %ddnsdomain% %ddnsfrequency%
pause