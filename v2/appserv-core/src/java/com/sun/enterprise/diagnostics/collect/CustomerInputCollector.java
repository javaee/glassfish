/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.diagnostics.collect;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.util.FileUtils;
import com.sun.logging.LogDomains;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mu125243
 */
public class CustomerInputCollector implements Collector{

    String customerInputFile;
    String customerInput;
    String intermediateReportLocation;
    boolean local;

    protected static final Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /** Creates a new instance of CustomerInputCollector */
    public CustomerInputCollector(String customerInputFile , String customerInput,
            String intermediateReportLocation, boolean local) {
       this.intermediateReportLocation = intermediateReportLocation;
       this.customerInput = customerInput;
       this.customerInputFile = customerInputFile;
       this.local = local;
    }

    public Data capture() throws DiagnosticException {
        if(customerInputFile != null)
            return copyCustomerInputFile(customerInputFile);
        if(customerInput != null && customerInput.trim().length() > 0) {
            WritableDataImpl customerInfo = new WritableDataImpl(DataType.CUSTOMER_INFO);
            customerInfo.addValue(customerInput);
            return customerInfo;
        }
        return null;
    }

    /**
     * Copies customer input
     * @param inputFile name of file to be copied.
     */
    private Data copyCustomerInputFile(String inputFile) {
        if(inputFile != null) {
            try {
                String destFile = intermediateReportLocation +
                        Defaults.CUSTOMER_INPUT;
                FileUtils.copyFile(inputFile, destFile);
                return new FileData(destFile,
                        DataType.CUSTOMER_INFO);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "diagnostic-service.copy_failed",
                        new Object[]{inputFile, ioe.getMessage()});
            }
        }
        return null;
    }

}
