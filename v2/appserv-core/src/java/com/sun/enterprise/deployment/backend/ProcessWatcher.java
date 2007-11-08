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

package com.sun.enterprise.deployment.backend;

import java.util.logging.Level;
import java.io.InputStream;
import java.io.IOException;


/**
 * Workaround for now for the lack of APIs to access wsgen and wsimport utilities
 * @author Jerome Dochez
 */
public class ProcessWatcher {
    
    Process toWatch = null;
    
    /** Creates a new instance of ProcessWatcher */
    public ProcessWatcher(Process p) {
        toWatch = p;
    }
    
    public int watch() {
        InputStream input = toWatch.getInputStream();
        InputStream error = toWatch.getErrorStream();
        boolean notDone = true;
        do {
            try {
                while (input.available()>0) {
                    dump(input);
                }
                while (error.available()>0) {
                    dump(error);
                }
            } catch(IOException e) {
                
            }
            try {
                toWatch.exitValue();
                notDone = false;
            } catch(IllegalThreadStateException e) {
            }            
        } while (notDone);
        return toWatch.exitValue();
    }
    
    public void dump(InputStream is) throws IOException {
        if (is.available()>0) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, bytes.length);
            System.out.println(new String(bytes));
        }
    }
}
