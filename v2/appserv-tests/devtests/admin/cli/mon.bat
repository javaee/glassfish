call ant clean
call ant -Dteststorun=monitoring | tee out
start d:\bg\all\v2\appserv-tests\test_results.html
