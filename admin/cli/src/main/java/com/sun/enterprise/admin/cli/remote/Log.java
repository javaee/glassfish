
package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *
 * @author bnevins
 */
class Log{
    static final void severe(String s, Object... objs) {
        logger.printError(strings.get(s, objs));
    }
    static final void warning(String s, Object... objs) {
        logger.printWarning(strings.get(s, objs));
    }
    static final void info(String s, Object... objs) {
        logger.printMessage(strings.get(s, objs));
    }
    static final void fine(String s, Object... objs) {
        logger.printDetailMessage(strings.get(s, objs));
    }
    static final void finer(String s, Object... objs) {
        logger.printDebugMessage(strings.get(s, objs));
    }
    static final void finest(String s, Object... objs) {
        logger.printTraceMessage(strings.get(s, objs));
    }

    private static final CLILogger logger = CLILogger.getInstance();
    private static LocalStringsImpl strings = new LocalStringsImpl(Log.class);
}
