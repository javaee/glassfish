/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package sahoo.osgihttp.test1;

import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private ServiceTracker httpServiceTracker;
	
	public void start(BundleContext context) throws Exception {
		httpServiceTracker = new HttpServiceTracker(context);
		httpServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		httpServiceTracker.close();
		httpServiceTracker = null;
	}

	private class HttpServiceTracker extends ServiceTracker {

		public HttpServiceTracker(BundleContext context) throws Exception {
			super(context, context.createFilter("(&(objectClass=" + 
                                                                org.osgi.service.http.HttpService.class.getName() + 
                                                                ")(VirtualServer=server))"), null);
		}

		public Object addingService(ServiceReference reference) {
			HttpService httpService = (HttpService) context.getService(reference);
			try {			
				httpService.registerServlet("/aa/bb", new HelloWorldServlet1(), null, null);
				httpService.registerServlet("/aa", new HelloWorldServlet2(), null, null);
                                System.out.println("Registered servlet1 with mapping /aa/bb and servlet2 with mapping /aa");
                                test();
                                httpService.unregister("/aa/bb");
                                System.out.println("Unregistered servlet1");
                                test();
                                
			} catch (Exception e) {
				e.printStackTrace();
			}
			return httpService;
		}		
	}

        void test() {
            try {
                final String urlstr = "http://localhost:8080/osgi/aa/bb";
                URL source = null;
                String inputLine = new String();
                StringBuffer resbuf = new StringBuffer();
                BufferedReader in = null;
                source = new URL(urlstr);
                in = new BufferedReader(new InputStreamReader(source.openStream()));
                while ((inputLine = in.readLine()) != null) {
                        resbuf.append(inputLine);
                }
                in.close();
                System.out.println(resbuf.toString());
            } catch(Exception e) {
                System.out.println(e);
            }
        }
}
