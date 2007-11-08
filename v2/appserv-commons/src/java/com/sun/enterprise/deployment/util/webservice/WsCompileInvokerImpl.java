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

package com.sun.enterprise.deployment.util.webservice;

import java.io.*;
import java.util.*;

//JAX-RPC SPI
import com.sun.xml.rpc.spi.JaxRpcObjectFactory;
import com.sun.xml.rpc.spi.tools.CompileTool;
import org.jvnet.hk2.annotations.Service;

/**
 * This is an implementation of the WsCompileInvoker
 */
@Service
public class WsCompileInvokerImpl extends WsCompileInvoker {

    private OutputStream output = null;
    
    public WsCompileInvokerImpl(OutputStream out) {
        output = out;
    }

    private void invokeTool(File configFile, String operation) throws WsCompileInvokerException {

        String key, value;
        int wsCompileOptionCount = 0;
        
        Object[] keySet = wsCompileOptions.keySet().toArray();
        
        for(int j=0; j<keySet.length; j++) {
            key = (String) keySet[j];
            value = (String) wsCompileOptions.get(keySet[j]);
            if(value == null)
                wsCompileOptionCount++;
            else
                wsCompileOptionCount+=2;
        }
                
        String[] wscompileArgs = new String[wsCompileOptionCount+3];
        int argsIndex = 0 ;

        for(int i=0; i<keySet.length; i++) {
            key = (String) keySet[i];
            value = (String) wsCompileOptions.get(keySet[i]);
            wscompileArgs[argsIndex++] = key;
            if(value != null)
                wscompileArgs[argsIndex++] = value;
        }
        wscompileArgs[argsIndex++] = "-" + operation;
        wscompileArgs[argsIndex++] = "-keep";        
        wscompileArgs[argsIndex++] = configFile.getPath();

        CompileTool tool = JaxRpcObjectFactory.newInstance().createCompileTool(output, "wscompile");
        if(!tool.run(wscompileArgs))
            throw new WsCompileInvokerException("wscompile invocation failed");
    }
    
/**
 * This is used to generate WSDL and mapping files given information on SEI config
*/
    public void generateWSDL(SEIConfig config) throws WsCompileInvokerException, IOException {
        File configFile = File.createTempFile("wscompileinvoker_seiinfo",
                                                  "config");
        configFile.deleteOnExit();
        FileWriter writer = new FileWriter(configFile);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        writer.write("<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">\n");
        writer.write("<service name=\"" + config.getWebServiceName() + "\" ");
        writer.write("targetNamespace=\"urn:" + config.getNameSpace() + "\" ");
        writer.write("typeNamespace=\"urn:" + config.getNameSpace() + "\" ");
        writer.write("packageName=\"" + config.getPackageName() + "\">\n");
        writer.write("<interface name=\"" + config.getInterface() + "\" ");
        writer.write("servantName=\"" + config.getServant() + "\"/>\n");
        writer.write("<typeMappingRegistry />\n");
        writer.write("</service>\n");
        writer.write("</configuration>\n");
        writer.close();
        invokeTool(configFile, "define");
    }

/**
 * This is used to generate SEI and mapping files given information on WSDL file location, require package name
*/

    public void generateSEI(WSDLConfig config) throws WsCompileInvokerException, IOException {
        File configFile = File.createTempFile("wscompileinvoker_wsdlinfo",
                                                  "config");
        configFile.deleteOnExit();
        FileWriter writer = new FileWriter(configFile);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        writer.write("<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">\n");
        writer.write("<wsdl location=\"" + config.getWsdlLocation() + 
                            "\" packageName=\"" + config.getPackageName() +"\">\n");
        writer.write("<typeMappingRegistry />\n");
        writer.write("</wsdl>\n");
        writer.write("</configuration>\n");
        writer.close();
        invokeTool(configFile, "import");
    }

/**
 * This is used to generate non-portable client stubs given information on WSDL file location, require package name
*/

    public void generateClientStubs(WSDLConfig config) throws WsCompileInvokerException, IOException {
        File configFile = File.createTempFile("wscompileinvoker_stubsinfo",
                                                  "config");
        configFile.deleteOnExit();
        FileWriter writer = new FileWriter(configFile);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        writer.write("<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">\n");
        writer.write("<wsdl name=\"" + config.getWebServiceName() + "\" location=\"" + config.getWsdlLocation() + 
                            "\" packageName=\"" + config.getPackageName() +"\">\n");
        writer.write("<typeMappingRegistry />\n");
        writer.write("</wsdl>\n");
        writer.write("</configuration>\n");
        writer.close();
        invokeTool(configFile, "gen:client");
    }

}
