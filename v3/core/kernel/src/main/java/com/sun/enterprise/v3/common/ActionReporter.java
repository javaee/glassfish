/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.common;


import org.glassfish.api.ActionReport;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Superclass for common ActionReport implementation.
 *
 * @author Jerome Dochez
 */
public abstract class ActionReporter implements ActionReport {

    protected Throwable exception = null;
    protected String actionDescription = null;
    protected List<ActionReporter> subActions = new ArrayList<ActionReporter>();
    protected ExitCode exitCode = ExitCode.SUCCESS;
    protected MessagePart topMessage = new MessagePart();
    
    /** Creates a new instance of HTMLActionReporter */
    public ActionReporter() {
    }

    public void setActionDescription(String message) {
        this.actionDescription = message;
    }

    public void setFailureCause(Throwable t) {
        this.exception = t;
    }

    public MessagePart getTopMessagePart() {
        return topMessage;
    }

    public ActionReport addSubActionsReport() {
        ActionReporter subAction;
        try {
            subAction = this.getClass().newInstance();
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InstantiationException ex) {
            return null;
        }
        subActions.add(subAction);
        return subAction;
    }

    public void setActionExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public ExitCode getActionExitCode() {
        return exitCode;
    }

    public void setMessage(String message) {
        topMessage.setMessage(message);
    }

    public String getMessage() {
        return topMessage.getMessage();
    }
        
    public abstract void writeReport(OutputStream os) throws IOException;
    
}
