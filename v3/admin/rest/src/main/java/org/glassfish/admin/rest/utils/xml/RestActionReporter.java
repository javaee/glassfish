/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.utils.xml;

import com.sun.enterprise.v3.common.ActionReporter;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jasonlee
 */
public class RestActionReporter extends ActionReporter {
    public RestActionReporter() {
        super();
    }
    
    @Override
    public void writeReport(OutputStream os) throws IOException {
        // no-op
    }
}
