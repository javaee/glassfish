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

package com.sun.enterprise.update.client.api;

import com.sun.enterprise.update.beans.Module;

/**
 * This Task class is used by GUI to show the progress of a module download.
 *
 * @author Satish Viswanatham
 */
public class DownloadTask {
    private int lengthOfTask;
    private int current = 0;
    private boolean done = false;
    private boolean canceled = false;
    private String statMessage = null;

    /**
     * Constructor.
     */
    public DownloadTask(int len) {
        lengthOfTask = len;
    }

    /**
     * Called from ProgressBar to find out how much work needs
     * to be done.
     */
    public int getLengthOfTask() {
        return lengthOfTask;
    }

    /**
     * Called from ProgressBar to find out how much has been done.
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Called from ProgressBar to stop the task
     */
    public void stop() {
        canceled = true;
        statMessage = null;
    }

    /**
     * Called from ProgressBarDemo to find out if the task has completed.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the most recent status message, or null
     * if there is no current status message.
     */
    public String getMessage() {
        return statMessage;
    }

}


