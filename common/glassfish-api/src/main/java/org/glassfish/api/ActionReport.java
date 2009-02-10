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

package org.glassfish.api;

import org.jvnet.hk2.annotations.Contract;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An action report is an interface allowing any type of server side action 
 * like a service execution, a command execution to report on its execution
 * to the originator of the action. 
 * 
 * Implementations of this interface should provide a good reporting 
 * experience based on the user's interface like a browser or a command line 
 * shell. 
 *
 * @author Jerome Dochez
 */
@Contract
public abstract class ActionReport {
    
    public enum ExitCode { SUCCESS, WARNING, FAILURE }
    
    public abstract void setActionDescription(String message);
    
    public abstract void setFailureCause(Throwable t);

    public abstract Throwable getFailureCause();

    public abstract void setMessage(String message);
    
    public abstract void writeReport(OutputStream os) throws IOException;

    public abstract void setMessage(InputStream in);

    public abstract String getMessage();
    
    public abstract MessagePart getTopMessagePart();
    
    public abstract ActionReport addSubActionsReport();
    
    public abstract void setActionExitCode(ExitCode exitCode);

    public abstract ExitCode getActionExitCode();
    
    public abstract String getContentType();

    public abstract void setContentType(String s);

    /**
     * Report a failure to the logger and {@link ActionReport}.
     *
     * This is more of a convenience to the caller.
     */
    public final void failure(Logger logger, String message, Throwable e) {
        logger.log(Level.SEVERE, message ,e);
        if (e!=null) {
            setMessage(message + " : "+ e.toString());
        } else {
            setMessage(message);
        }
        setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    /**
     * Short for {@code failure(logger,message,null)}
     */
    public final void failure(Logger logger, String message) {
        failure(logger,message,null);
    }

    public static class MessagePart {

        Properties props = new Properties();
        String message;
        String childrenType;

        List<MessagePart> children = new ArrayList<MessagePart>();

        public MessagePart addChild() {
            MessagePart newPart = new MessagePart();
            children.add(newPart);
            return newPart;
        }

        public void setChildrenType(String type) {
            this.childrenType = type;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void addProperty(String key, String value) {
            props.put(key, value);
        }

        public Properties getProps() {
            return props;
        }

        public String getMessage() {
            return message;
        }

        public String getChildrenType() {
            return childrenType;
        }

        public List<MessagePart> getChildren() {
            return children;
        }        
    }

    Properties extraProperties;

    public final Properties getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Properties properties) {
        extraProperties = properties;
    }
}
