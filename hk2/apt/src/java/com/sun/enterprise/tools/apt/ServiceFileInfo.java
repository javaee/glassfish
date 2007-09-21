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

package com.sun.enterprise.tools.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * State file for annotaton processor, 1 per service type, maintains
 * the list of implementors of that service type.
 * 
 */
final class ServiceFileInfo {

    private final String serviceName;
    private Set<String> implementors;
    private PrintWriter writer=null;


    public ServiceFileInfo(String serviceName, Set<String> initialImplementors) {
        this.serviceName = serviceName;
        this.implementors = initialImplementors;
    }

    public boolean isDirty() {
        return writer!=null;
    }

    public void createFile(AnnotationProcessorEnvironment env) throws IOException {
        // create the file at this time.
        if (writer==null) {
            File out = new File(new File("META-INF/services"),serviceName);
            writer = env.getFiler().createTextFile(Filer.Location.SOURCE_TREE, "", out, "UTF-8");
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public Set<String> getImplementors() {
        return implementors;
    }

    public PrintWriter getWriter() {
        return writer;
    }
}
