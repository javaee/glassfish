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

package org.glassfish.resources.custom.factory;

import javax.naming.spi.ObjectFactory;
import javax.naming.*;
import java.io.*;
import java.util.*;


public class PropertiesFactory implements Serializable, ObjectFactory {
    public static final String filePropertyName = "org.glassfish.resources.custom.factory.PropertiesFactory.fileName";
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        Reference ref = (Reference)obj;
        Enumeration<RefAddr> refAddrs = ref.getAll();

        String fileName = null;
        Properties fileProperties = new Properties();
        Properties properties = new Properties();

        while(refAddrs.hasMoreElements()){
            RefAddr addr = refAddrs.nextElement();
            String type = addr.getType();
            String value = (String)addr.getContent();

            if(type.equalsIgnoreCase(filePropertyName)){
                fileName = value;
            }else{
                properties.put(type, value);
            }
        }

            if(fileName != null){
                File file = new File(fileName);
                if(!file.isAbsolute()){
                    file = new File(System.getProperty("com.sun.aas.installRoot")+File.separator+fileName);
                }
                try{
                    if(file.exists()){
                        try{

                            FileInputStream fis = new FileInputStream(file);
                            if(fileName.toUpperCase().endsWith("XML")){
                                fileProperties.loadFromXML(fis);
                            }else{
                                fileProperties.load(fis);
                            }

                        }catch(IOException ioe){
                            throw new IOException("IO Exception during properties load : " + file.getAbsolutePath());
                        }
                    } else {
                        throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
                    }
                }catch(FileNotFoundException fnfe){
                    throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
                }
            }
            fileProperties.putAll(properties);

            return fileProperties;
    }
}
