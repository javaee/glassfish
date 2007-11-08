package util;

import com.sun.enterprise.util.net.NetUtils;


/**
 *
 *
 *
 *
 */
public class Util {

	private static final String DAS_PORT;
	private static final String DAS_HOST;
	private static final String ADMIN_USER;
	private static final String ADMIN_PASSWD;
	private static final String INSTALL_TYPE;
	private static boolean IS_EE;

	static {
		DAS_HOST = System.getProperty("ADMIN_HOST", "localhost");
		DAS_PORT = System.getProperty("ADMIN_PORT", "4849");
		ADMIN_USER = System.getProperty("ADMIN_USER", "admin");
		ADMIN_PASSWD = System.getProperty("ADMIN_PASSWORD", "adminadmin");
		try {
			IS_EE = NetUtils.isSecurePort(DAS_HOST, Integer.parseInt(DAS_PORT));
		} catch(Exception ex) {
			//default make it PE. Squelch it, and let the connection handle this
			IS_EE=false;
		}

		if (IS_EE)
		    INSTALL_TYPE = "ee";
		else
		    INSTALL_TYPE = "pe";

	}

    public static String getAdminHost() {
        return DAS_HOST;
    }

    public static String getAdminPort() {
        return DAS_PORT;
    }

    public static String getAdminUser() {
        return  ADMIN_USER;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWD;
    }

    public static String getInstallType() {
        return INSTALL_TYPE;
    }


}
