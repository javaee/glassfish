rem @echo off
call setfiles.bat

svn diff %br%\%a% %tr%\%a%
pause
svn commit %br%\%a% %tr%\%a%
