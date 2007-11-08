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

package com.sun.enterprise.config.serverbeans.validation;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import org.xml.sax.InputSource;
import java.io.FileReader;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;



public class SimpleTransform
{
    public static void main(String [] args) throws Exception {
        String in = null;
        String xsl = null;
        String out = null;
        
        if (args.length != 6){
            System.err.println("Insufficient args: SimpleTransform -in file -xsl file -out file");
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++){
            if (args[i].equals("-in")){
                in = args[++i];
            } else if (args[i].equals("-xsl")){
                xsl = args[++i];
            } else if (args[i].equals("-out")){
                out = args[++i];
            } else {
                System.err.println("Unrecognized arg: "+args[i]);
                System.exit(1);
            }
        }
        

        final TransformerFactory f = TransformerFactory.newInstance();
        final Transformer t = f.newTransformer(new StreamSource(new File(xsl)));
//         final Source src = new SAXSource(XMLReaderFactory.newInstance(System.err),
//                                    new InputSource(in));
        final Source src = new SAXSource(new VariableResolver(),
                                   new InputSource(in));
        final Result res = new StreamResult(out);
        
        t.transform(src, res);
    }
    

}
