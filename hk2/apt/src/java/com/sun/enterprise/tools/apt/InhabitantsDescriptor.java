/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.tools.apt;

import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.KeyValuePairParser;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

/**
 * Inhabitants descriptor as a map from the class name to its line.
 *
 * @author Kohsuke Kawaguchi
 */
final class InhabitantsDescriptor extends HashMap<String,String> {
    private boolean dirty = false;
    public InhabitantsDescriptor() {
    }

    public InhabitantsDescriptor(File f) throws IOException {
        load(f);
    }

    /**
     * Loads an existing file.
     */
    public void load(File f) throws IOException {
        InhabitantsScanner scanner = new InhabitantsScanner(new FileInputStream(f),f.getPath());
        for (KeyValuePairParser kvpp : scanner)
            put(kvpp.find(InhabitantsFile.CLASS_KEY),kvpp.getLine());
    }


    public String put(String key, String value) {
        dirty = true;
        return super.put(key, value);
    }

    public String remove(Object key) {
        dirty = true;
        return super.remove(key);
    }

    /**
     * Writes the descriptor to a file.
     */
    public void write(File outputDir, AnnotationProcessorEnvironment env,String habitatName) {
        if(!dirty)  return; // no need to write.

        try {
            File out = new File(new File(outputDir,InhabitantsFile.PATH),habitatName);
            out.getParentFile().mkdirs();
            PrintWriter w = new PrintWriter(out,"UTF-8");

            w.println("# generated on "+new Date().toGMTString());
            for (String line : values()) {
                w.println(line);
            }
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
            env.getMessager().printError("Failed to write inhabitants file "+habitatName);
        }
    }
}
