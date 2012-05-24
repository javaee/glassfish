/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.annotation.processing;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedOptions("resourceBundlePackage")
@SupportedAnnotationTypes("org.glassfish.logging.LogMessageInfo")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class LogMessageInfoAnnotationProcessor extends AbstractProcessor {

    public static final String RBFILE = "LogMessages.properties";
    HashMap<String, String> pkgMap = null;
    protected boolean debugging = false;

    @Override
    public boolean process (Set<? extends TypeElement> annotations, 
            RoundEnvironment env) {
        
        error("The usage of LogMessageInfoAnnotationProcessor is obsoleted. " + 
                "Please use the LogMessagesResourceBundleGenerator processor instead along with the new annotations from logging-annotation-processor artifact.");
        
        return false; // Claim the annotations
    }    

    protected void debug(String msg) {
        if (debugging)
            System.out.println(msg);
    }

    protected void debug(String msg, Throwable t) {
        if (debugging) {
            System.out.println(msg + "Exception: " + t.getMessage());
            t.printStackTrace();
        }
    }

    protected void info(String msg) {
        debug(msg);
        processingEnv.getMessager().printMessage(Kind.NOTE, 
            "LogMessageInfoAnnotationProcessor: " + msg);
    }

    protected void warn(String msg) {
        processingEnv.getMessager().printMessage(Kind.WARNING, 
            "LogMessageInfoAnnotationProcessor: " + msg);
    }
    protected void warn(String msg, Throwable t) {
        String errMsg = msg + ": " + t.getMessage();

        processingEnv.getMessager().printMessage(Kind.WARNING, 
            "LogMessageInfoAnnotationProcessor: " + errMsg);
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR, 
            "LogMessageInfoAnnotationProcessor: " + msg);
    }
    protected void error(String msg, Throwable t) {
        String errMsg = msg + ": " + t.getMessage();

        processingEnv.getMessager().printMessage(Kind.ERROR, 
            "LogMessageInfoAnnotationProcessor: " + errMsg);
    }
}
