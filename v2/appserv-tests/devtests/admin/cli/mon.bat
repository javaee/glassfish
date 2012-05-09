call ant clean
call ant -Dteststorun=monitoring | tee out
start d:/gf/trunk/v2/appserv-tests/test_results.html
