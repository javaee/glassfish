package com.sun.enterprise.naming.util;

import java.util.logging.Logger;

public class LogFacade {

    public static Logger _logger = Logger.getLogger("com.sun.enterprise.naming");

    public static Logger getLogger() {
        return _logger;
    }
}
