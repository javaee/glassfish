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

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import com.sun.org.apache.xml.internal.serializer.ToXMLStream;
import java.io.OutputStream;
import org.xml.sax.EntityResolver;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.SAXException;



public class Flatten
{
    public static void main(String [] args) throws Exception {

        String input = null;
        OutputStream output = System.out;
        EntityResolver entityResolver = null;
        
        if ( args.length < 1 || 5 < args.length){
            System.err.println("Flatten [-out output-file] [-dtd dtd] input ");
        }

        for (int i = 0; i < args.length; i++){
            if (args[i].equals("-out")){
                output = new FileOutputStream(new File(args[++i]));
                break;
            }
            if (args[i].equals("-dtd")){
                entityResolver = new MyEntityResolver(args[++i]);
                break;
            }
            input = args[i];
        }

    
        

        VariableResolver vr = new VariableResolver();
        if (null != entityResolver){
            vr.setEntityResolver(entityResolver);
        }
        
//         vr.setParent(getXmlReader());
        vr.setContentHandler(getContentHandler(output));
        vr.parse(new InputSource(input));
    }

    private static XMLReader getXmlReader() throws Exception {
        return XMLReaderFactory.newInstance(System.err);
    }

    private static ContentHandler getContentHandler(OutputStream o) throws Exception {
        ToXMLStream xml = new ToXMLStream();
        xml.setOutputStream(o);
        xml.setOmitXMLDeclaration(true);
        return xml.asContentHandler();
    }

    private static class MyEntityResolver implements EntityResolver
    {
        private String myDtd = "";

        MyEntityResolver(){}
        
        MyEntityResolver(String dtd){
            myDtd = dtd;
        }
        
        public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)
            throws IOException, SAXException{
            if (null != publicId && publicId.equals("-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN")) {
                final InputStream is = getClass().getResourceAsStream(myDtd);
                return (null != is ) ? new InputSource(is) : null;
            } else {
                return null;
            }
        }
    }

}
