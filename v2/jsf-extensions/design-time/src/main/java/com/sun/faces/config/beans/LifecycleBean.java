/*
 * $Id: LifecycleBean.java,v 1.1 2005/09/20 21:11:26 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.beans;


import java.util.ArrayList;
import java.util.List;


/**
 * <p>Configuration bean for <code>&lt;lifecycle&gt; element.</p>
 */

public class LifecycleBean {


    // -------------------------------------------------------------- Properties


    // --------------------------------------------- PhaseListenerHolder Methods


    private List<String> phaseListeners = new ArrayList<String>();


    public void addPhaseListener(String phaseListener) {
        if (!phaseListeners.contains(phaseListener)) {
            phaseListeners.add(phaseListener);
        }
    }


    public String[] getPhaseListeners() {
        String results[] = new String[phaseListeners.size()];
        return (phaseListeners.toArray(results));
    }


    public void removePhaseListener(String phaseListener) {
        phaseListeners.remove(phaseListener);
    }


    // ----------------------------------------------------------------- Methods




}
