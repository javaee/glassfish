/*
 * $Id: AbstractGeneratorTask.java,v 1.1 2005/09/20 21:11:22 edburns Exp $
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

package com.sun.faces.ant;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.BuildException;

/**
 * <p>Base task for generators.</p>
 */
public abstract class AbstractGeneratorTask extends Java {

    /**
     * <p>The fully qualified path to the properties file to drive the
     * Generator.</p>
     */
    protected String generatorConfig;

    /**
     * <p>The fully qualified <code>Generator</code class.</p>
     */
    private String generatorClass;

    /**
     * <p>The fully qualified path to the faces-config.xml to serve
     * as the model for the <code>Generator</code>.</p>
     */
    private String facesConfig;


    // ---------------------------------------------------------- Public Methods


    public void setGeneratorConfig(String generatorConfig) {

        this.generatorConfig = generatorConfig;

    } // END setGeneratorConfig


    public void setFacesConfig(String facesConfig) {

        this.facesConfig = facesConfig;

    } // END setFacesConfig


    public void setGeneratorClass(String generatorClass) {

        this.generatorClass = generatorClass;

    } // END setGeneratorClass


    public void execute() throws BuildException {

        super.createArg().setValue(generatorConfig);
        super.createArg().setValue(facesConfig);

        super.setClassname(generatorClass);

        super.execute();

    } // END execute


}
