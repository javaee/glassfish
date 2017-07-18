/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.net.URL;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;
        
/**
 * Command line client for the global monitoring web service
 *
 * @author Jerome Dochez
 */
public class CLClient {
    
    public static void main(String[] args) {
        CLClient client = new CLClient();
        client.doTest(args);
    }
    
    /** Creates a new instance of CLClient */
    public CLClient() {
    }
    
    private void doTest(String[] args) {
        try {
            System.out.println("Running in " + System.getProperty("java.version"));
            ServiceFactory factory = ServiceFactory.newInstance();
            GlobalMonitoring_Impl service = new GlobalMonitoring_Impl();
            System.out.println("Obtained Service " + service);
            WebServiceEngine intf = (WebServiceEngine) service.getPort(
                    new QName("urn:SunAppServerMonitoring","WebServiceEnginePort"),
                    WebServiceEngine.class);
            if (intf!=null) {
                System.out.println("There are " + intf.getEndpointsCount() + " endpoints available");
                int endpointsCount = intf.getEndpointsCount();
                for (int i=0;i<endpointsCount;i++) {
                    String selector = intf.getEndpointsSelector(i);
                    System.out.println("Web Service " + i + " URL is " + selector);
                }
            }
            int tracesCount = intf.getTraceCount();
            System.out.println("Traces = " + tracesCount);
            
                   
            for (int i=0;i<tracesCount;i++) {
                InvocationTrace trace = intf.getTrace(i);
                System.out.println("=====Trace " + i + " request " + trace.getRequest());
                System.out.println("=====Trace " + i + " response" + trace.getResponse());
                
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
