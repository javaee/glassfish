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


import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Superclass for common ActionReport implementation.
 *
 * @author Jerome Dochez
 */
public abstract class ActionReporter extends ActionReport {

    protected Throwable exception = null;
    protected String actionDescription = null;
    protected List<ActionReporter> subActions = new ArrayList<ActionReporter>();
    protected ExitCode exitCode = ExitCode.SUCCESS;
    protected MessagePart topMessage = new MessagePart();
    protected String contentType = "text/html";

    protected static final String EOL_MARKER = "%%%EOL%%%";

    /** Creates a new instance of HTMLActionReporter */
    public ActionReporter() {
    }

    public void setFailure() {
        setActionExitCode(ExitCode.FAILURE);
    }
    
    public boolean isFailure() {
        return getActionExitCode() == ExitCode.FAILURE;
    }
    
    public void setWarning() {
        setActionExitCode(ExitCode.WARNING);
    }

    public boolean isWarning() {
        return getActionExitCode() == ExitCode.WARNING;
    }
    
    public boolean isSuccess() {
        return getActionExitCode() == ExitCode.SUCCESS;
    }
    
    public void setSuccess() {
        setActionExitCode(ExitCode.SUCCESS);
    }
    
    @Override
    public void setActionDescription(String message) {
        this.actionDescription = message;
    }

    @Override
    public void setFailureCause(Throwable t) {
        this.exception = t;
    }
    @Override
    public Throwable getFailureCause() {
        return exception;
    }
        
    @Override
    public MessagePart getTopMessagePart() {
        return topMessage;
    }

    @Override
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

    @Override
    public void setActionExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public ExitCode getActionExitCode() {
        return exitCode;
    }

    @Override
    public void setMessage(String message) {
        topMessage.setMessage(message);
    }

    @Override
    public String getMessage() {
        return topMessage.getMessage();
    }
        
    
    @Override
    public void setMessage(InputStream in) {
        try {
            if(in == null)
                throw new NullPointerException("Internal Error - null InputStream");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(in, baos);
            setMessage(baos.toString());
        }
        catch (Exception ex) {
            setActionExitCode(ExitCode.FAILURE);
            setFailureCause(ex);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }

        out.close();
        in.close();
    }
    
    /**
     * Returns the content type to be used in sending the response back to 
     * the client/caller.
     * <p>
     * This is the default type.  Specific subclasses of ActionReporter might
     * override the method to return a different valid type.
     * @return content type to be used in formatting the command response to the client
     */
    @Override
    public String getContentType() {
        return contentType;
    }
    @Override
    public void setContentType(String s) {
        contentType = s;
    }

    /** Returns combined messages. Meant mainly for long running
     *  operations where some of the intermediate steps can go wrong, although
     *  overall operation succeeds. Does nothing if either of the arguments are null.
     *  The traversal visits the message of current reporter first.
     * <p>
     * Note: This method is a recursive implementation.
     * @param aReport a given (usually top-level) ActionReporter instance
     * @param sb StringBuilder instance that contains all the messages  
     */
    protected void getCombinedMessages(ActionReporter aReport, StringBuilder sb) {
        if (aReport == null || sb == null)
            return;
        String mainMsg = ""; //this is the message related to the topMessage
        String failMsg; //this is the message related to failure cause
        // Other code in the server may write something like report.setMessage(exception.getMessage())
        // and also set report.setFailureCause(exception). We need to avoid the duplicate message.
        if (aReport.getMessage() != null && aReport.getMessage().length() != 0) {
            mainMsg = aReport.getMessage();
            String format = "{0}";
            if (ActionReport.ExitCode.WARNING.equals(aReport.getActionExitCode())) {
                LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ActionReporter.class);
                format = localStrings.getLocalString("flag.message.as.warning", "Warning: {0}");
            }
            if (ActionReport.ExitCode.FAILURE.equals(aReport.getActionExitCode())) {
                LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ActionReporter.class);
                format = localStrings.getLocalString("flag.message.as.failure", "Failure: {0}");
            }
            sb.append(MessageFormat.format(format,mainMsg));
            sb.append(EOL_MARKER);
        }
        if (aReport.getFailureCause() != null && aReport.getFailureCause().getMessage() != null && aReport.getFailureCause().getMessage().length() != 0) {
            failMsg = aReport.getFailureCause().getMessage();
            if (!failMsg.equals(mainMsg))
                sb.append(failMsg);
        }
        for (ActionReporter sub : aReport.subActions) {
            getCombinedMessages(sub, sb);
        }
    }

    @Override
    public boolean hasSuccesses() {
        return has(this,ExitCode.SUCCESS);
    }

    @Override
    public boolean hasWarnings() {
        return has(this,ExitCode.WARNING);
    }

    @Override
    public boolean hasFailures() {
        return has(this,ExitCode.FAILURE);
    }

    private static boolean has(ActionReporter ar, ExitCode value) {
        if (null != ar.exitCode && ar.exitCode.equals(value)) {
            return true;
        }
        Queue<ActionReporter> q = new LinkedList<ActionReporter>();
        q.addAll(ar.subActions);
        while (!q.isEmpty()) {
            ActionReporter lar = q.remove();
            ExitCode ec = lar.getActionExitCode();
            if (null != ec && ec.equals(value)) {
                return true;
            } else {
                q.addAll(lar.subActions);
            }
        }
        return false;
    }
}
