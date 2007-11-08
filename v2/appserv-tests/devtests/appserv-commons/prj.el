(jde-project-file-version "1.0")
;; For SOlaris
;  '(jde-jdk-registry (quote (("1.4.2" . "/net/koori.sfbay/onestop/jdk/1.4.2_05/promoted/latest/binaries/linux-i586"))))
(jde-set-variables
 '(jde-jdk-registry (quote (("1.4.2_04" . "c:/j2sdk1.4.2_04"))))
 '(jde-global-classpath (quote ("./build" "../../../publish/JDK1.4_DBG.OBJ/junit/junit.jar" "../../../publish/JDK1.4_DBG.OBJ/admin-core/config-api/lib/config-api.jar" "../../../publish/JDK1.4_DBG.OBJ/appserv-commons/lib/appserv-commons.jar" "../../../publish/JDK1.4_DBG.OBJ/netbeans/modules/schema2beans.jar" "../../../publish/JDK1.4_DBG.OBJ/jmx/lib/jmxri.jar")))
 '(jde-compiler (quote ("jikes" "")))
 '(jde-compile-option-directory "./build")
 '(jde-compile-option-source (quote ("1.4")))
 '(jde-ant-program "../../../publish/JDK1.4_DBG.OBJ/ant/bin/ant")
 '(jde-sourcepath (quote ("./com/sun/enterprise/config" "../../../appserv-commons/src/java/com/sun/enterprise/config/" "../../../appserv-commons/src/java/com/sun/enterprise/config/impl")))
 '(jde-compile-option-debug (quote ("all" (t nil nil)))))
