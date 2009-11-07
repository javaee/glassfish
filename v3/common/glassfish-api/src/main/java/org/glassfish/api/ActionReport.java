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

    /**
     * return true if the action report or a subaction report has ExitCode.SUCCESS.
     */
    public abstract boolean hasSuccesses();

    /**
     * return true if the action report or a subaction report has ExitCode.WARNING.
     */
    public abstract boolean hasWarnings();

    /**
     * return true if the action report or a subaction report has ExitCode.FAILURE.
     */
    public abstract boolean hasFailures();

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
