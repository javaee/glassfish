/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.io;

import java.io.*;
import java.util.*;

/**
 * A class for sanitizing Files.
 * Note that the main reason for this class is that on non-Windows, 
 * getCanonicalXXX and getAbsoluteXXX might point at different files.  
 * If the file is a soft link then the Canonical will be the file that is linked to.
 * The Absolute will be the link file itself.
 * This method will give you the benefits of Canonical -- but will always point
 * at the link file itself. 
 * <p>
 * I.e. It is just like getAbsoluteXXX -- but it removes all relative path 
 * elements from the path.
 * @author bnevins
 */

public class SmartFile {
    /**
     * Sanitize a File object -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * @param f The file to sanitize
     * @return THe sanitized File
     */
    public static File sanitize(File f)
    {
        SmartFile sf = new SmartFile(f);
        return new File(sf.path);
    }

    /**
     * Sanitize a path -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * Note that the main reason for this class is that on non-Windows, 
     * getCanonicalXXX and getAbsoluteXXX might point at different files.  
     * If the file is a soft link then the Canonical will be the file that is linked to.
     * The Absolute will be the link file itself.
     * This method will give you the benefits of Canonical -- but will always point
     * at the link file path itself. 
     * @param filename The path to sanitize
     * @return The sanitized path
     */
    public static String sanitize(String filename) {
        SmartFile sf = new SmartFile(filename);
        return sf.path;
    }
    
    private SmartFile(File f) {
        if(f == null)
            throw new NullPointerException();
        
        convert(f.getAbsolutePath());
    }
    private SmartFile(String s) {
        if(s == null)
            throw new NullPointerException();

        // note that "" is a valid filename
        convert(new File(s).getAbsolutePath());
    }

    private void convert(String oldPath) {
        // guarantee -- the beginning will not have "." or ".."
        oldPath = oldPath.replace('\\', '/');
        String[] elemsArray = oldPath.split(SLASH);
        List<String> elems = new ArrayList<String>();
        
        for(String s : elemsArray) {
            elems.add(s);
        }
            
        // 4 possibilities
        // 1. (Windows) //x
        // 2. (Windows) X:/
        // 3. / (UNIX)
        // 4. /x (UNIX)
        
        // Windows -->  \\computer\x\y\z
        if(oldPath.startsWith("//")) { // 1.
            path = "//";
        }
        else if(!oldPath.startsWith(SLASH)) { // 2.
            path = elems.get(0) + SLASH;
            elems.remove(0);
        }
        else if(oldPath.startsWith(SLASH)) { //3,4
            path = SLASH;
        }
        
        // remove empty elems
            for(Iterator<String> it = elems.iterator(); it.hasNext(); ) {
                String elem = it.next();
                if(elem.length() <= 0) {
                    it.remove();
            }
        }
        
        // replace dots
        
        while(hasDots(elems)) {
            for(int i = 0; i < elems.size(); i++) {
                String elem = elems.get(i);

                if(elem.equals(".")) {
                    elems.remove(i);
                    break;
                }
                else if(elem.equals("..")) {
                    elems.remove(i);
                    
                    // special case -- path is something like "/foo/../../.."
                    // just return the convertial path...
                    if(i <= 0)
                        return;
                    
                    elems.remove(i-1);
                    break;
                }
            }
        }

        // now all the dots are gone.
        for(String s : elems) {
            path += s + SLASH;
        }
        // get rid of trailing slash
        if(path.length() > 1 && !path.equals("//"))
            path = path.substring(0, path.length() - 1);

        // Finally -- make it OS-friendly
        if(File.separatorChar != '/') {
            doWindows();
        }            
    }

    private void doWindows() {
        path = path.replace('/', File.separatorChar);
        
        // make the drive letter uppercase
        String drive = path.substring(0, 2);
        if(drive.endsWith(":")) {
            drive = drive.toUpperCase();
            path = drive + path.substring(2);
        }
    }

    private boolean hasDots(List<String> elems) {
        return elems.contains(".") || elems.contains("..");
    }


    private String path;
    private static final String SLASH = "/";
}
