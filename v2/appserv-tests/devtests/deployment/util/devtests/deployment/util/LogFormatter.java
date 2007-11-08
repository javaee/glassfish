/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * LogFormatter.java
 *
 * Created on January 28, 2005, 1:58 PM
 */

package devtests.deployment.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * I just hate the jdk formatters
 *
 * @author dochez
 */
public class LogFormatter extends Formatter {
    
    /** Creates a new instance of LogFormatter */
    public LogFormatter() {
    }
   
    public String format(LogRecord record) {
        return record.getMessage()+"\n";
    }
}
