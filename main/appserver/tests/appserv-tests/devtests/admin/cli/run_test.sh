test_run(){
	export HUDSON=true
	export ROOT=`pwd`

	if [ -x "/usr/bin/cygpath" ]
	then
	  ROOT=`cygpath -d $ROOT`
	  echo "Windows ROOT: $ROOT"
	  export CYGWIN=nontsec
	fi
	ant clean
	time ant -Dnum_tests=45 $TARGET | tee $TEST_RUN_LOG
	egrep 'FAILED *0' "$APS_HOME/count.txt" >/dev/null
}

run_test_id(){
	source `dirname $0`/../../../../common_test.sh
	kill_process
	delete_gf
	download_test_resources glassfish.zip version-info.txt
	unzip_test_resources $WORKSPACE/bundles/glassfish.zip
	cd `dirname $0`
	test_init
	get_test_target $1
	test_run
	check_successful_run
    generate_junit_report $1
    change_junit_report_class_names
    copy_test_artifects
    upload_test_results
    delete_bundle
    cd -
}

get_test_target(){
	case $1 in
		admin_cli_all )
			TARGET=all
			export TARGET;;
	esac

}

list_test_ids(){
	echo admin_cli_all
}

OPT=$1
TEST_ID=$2

case $OPT in
	list_test_ids )
		list_test_ids;;
	run_test_id )
		run_test_id $TEST_ID ;;
esac
