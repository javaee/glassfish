package org.glassfish.weld;

import javax.enterprise.inject.spi.Extension;

public class WeldUtils {

    static final char SEPARATOR_CHAR = '/';
    static final String WEB_INF = "WEB-INF";
    static final String WEB_INF_CLASSES = WEB_INF + SEPARATOR_CHAR
            + "classes";
    static final String WEB_INF_LIB = WEB_INF + SEPARATOR_CHAR + "lib";
    static final String WEB_INF_BEANS_XML = "WEB-INF" + SEPARATOR_CHAR
            + "beans.xml";

    static final String META_INF_BEANS_XML = "META-INF" + SEPARATOR_CHAR
            + "beans.xml";

    private static final String SERVICES_DIR = "services";
    private static final String SERVICES_CLASSNAME = Extension.class.getCanonicalName();
    static final String META_INF_SERVICES_EXTENSION = "META-INF"
            + SEPARATOR_CHAR + SERVICES_DIR + SEPARATOR_CHAR
            + SERVICES_CLASSNAME;
    
    static final String CLASS_SUFFIX = ".class";
    static final String JAR_SUFFIX = ".jar";
    static final String RAR_SUFFIX = ".rar";
    static final String EXPANDED_RAR_SUFFIX = "_rar";
    static final String EXPANDED_JAR_SUFFIX = "_jar";

    static enum BDAType { WAR, JAR, UNKNOWN };

}