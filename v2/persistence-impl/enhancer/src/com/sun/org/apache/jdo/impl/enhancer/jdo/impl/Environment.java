/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.jdo.impl;

import java.io.PrintWriter;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;




/**
 * Serves as a repository for the options for the enhancer.
 */
public final class Environment
    extends Support
{
    /**
     * Writer for regular program output and warnings.
     */
    private PrintWriter out = new PrintWriter(System.out, true);

    /**
     * Writer for error output.
     */
    private PrintWriter err = new PrintWriter(System.err, true);

    /**
     * If true, provide timing statistics.
     */
    private boolean timingOption = false;

    /**
     * If true, dump class.
     */
    private boolean dumpClassOption = false;

    /**
     * If true, don't apply augmentation to PC classes.
     */
    private boolean noAugmentOption = false;

    /**
     * If true, don't apply annotation to PC classes.
     */
    private boolean noAnnotateOption = false;

    /**
     * If true, provide verbose output.
     */
    private boolean verboseOption = false;

    /**
     * If true, squash warnings.
     */
    private boolean quietOption = false;

    /**
     * The number of errors encountered thus far.
     */
    private int errorsEncountered = 0;

    /**
     * The instance providing the JDO meta data.
     */
    private EnhancerMetaData jdoMetaData;

    /**
     * Last error message.
     */
    private String lastErrorMessage = null;

    /**
     * The constructor
     */
    public Environment()
    {}

    public void error(String error)
    {
        errorsEncountered++;
        err.println(lastErrorMessage = getI18N("enhancer.enumerated_error",
                                               errorsEncountered,
                                               error));
    }

    public void warning(String warn)
    {
        if (!quietOption) {
            out.println(getI18N("enhancer.warning", warn));
        }
    }

    public void verbose(String msg)
    {
        if (verboseOption) {
            out.println(msg);
        }
    }

    public void message(String msg)
    {
        if (verboseOption) {
            out.println(getI18N("enhancer.message", msg));
        }
    }

    public void messageNL(String msg)
    {
        if (verboseOption) {
            out.println();
            out.println(getI18N("enhancer.message", msg));
        }
    }

    public int errorCount()
    {
        return errorsEncountered;
    }

    public final String getLastErrorMessage()
    {
        return this.lastErrorMessage;
    }

    public void setDoTimingStatistics(boolean opt)
    {
        timingOption = opt;
    }

    public boolean doTimingStatistics()
    {
        return timingOption;
    }

    public void setDumpClass(boolean opt)
    {
        dumpClassOption = opt;
    }

    public boolean dumpClass()
    {
        return dumpClassOption;
    }

    public void setNoAugment(boolean opt)
    {
        noAugmentOption = opt;
    }

    public boolean noAugment()
    {
        return noAugmentOption;
    }

    public void setNoAnnotate(boolean opt)
    {
        noAnnotateOption = opt;
    }

    public boolean noAnnotate()
    {
        return noAnnotateOption;
    }

    public EnhancerMetaData getEnhancerMetaData()
    {
        return jdoMetaData;
    }

    public void setEnhancerMetaData(EnhancerMetaData jdoMetaData)
    {
        this.jdoMetaData = jdoMetaData;
    }

    public void setOutputWriter(PrintWriter out)
    {
        this.out = out;
    }

    public PrintWriter getOutputWriter()
    {
        return out;
    }

    public void setErrorWriter(PrintWriter err)
    {
        this.err = err;
    }

    public PrintWriter getErrorWriter()
    {
        return err;
    }

    public void setVerbose(boolean beVerbose)
    {
        verboseOption = beVerbose;
    }

    public boolean isVerbose()
    {
        return verboseOption;
    }

    public void setQuiet(boolean beQuiet)
    {
        quietOption = beQuiet;
    }

    public boolean isQuiet()
    {
        return quietOption;
    }

    /**
     * Reset the environment.
     */
    public void reset()
    {
        //^olsen: ?
/*
        jdoMetaData = null;

        verboseOption = false;
        quietOption = false;
*/
        errorsEncountered = 0;
    }
}
