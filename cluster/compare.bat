@echo off
call setfiles.bat
echo ******  %a%
diff %br%\%a% %tr%\%a%
echo ******  %b%
diff %br%\%b% %tr%\%b%
echo ******  %c%
diff %br%\%c% %tr%\%c%
