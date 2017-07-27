/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.w3c.dom.Node;

import java.net.URL;
import java.net.URI;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Map;

public class Client {

    private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
    private static final QName serviceQName = new QName("http://duke.org", "AddNumbersService");
    private static final QName portQName = new QName("http://duke.org", "AddNumbersPort");

    private static String endpointAddress =
        "http://HTTP_HOST:HTTP_PORT/restful-2/webservice/AddNumbersService";
    private static String queryString = "num1=30&num2=20";

    public static void main (String[] args) throws Exception {
	stat.addDescription("webservices-simple-restful-svc");
        Client client = new Client();
        Service service = client.createService();
        URI endpointURI = new URI(endpointAddress);
        String path = null;
        String query = null;
        if (endpointURI != null){
            path = endpointURI.getPath();
            query = endpointURI.getQuery();
        }
        service.addPort(portQName, HTTPBinding.HTTP_BINDING, endpointAddress);
        Dispatch<Source> d = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, new String("GET"));
        requestContext.put(MessageContext.QUERY_STRING, queryString);
        requestContext.put(MessageContext.PATH_INFO, path);
        System.out.println ("Invoking Restful GET Request with query string " + queryString);
        Source result = d.invoke(null);
        printSource(result);
	stat.printSummary("webservices-simple-restful-svc");
    }

    private Service createService() {
        Service service = Service.create(serviceQName);
        return service;
    }

    private static void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            String resp = bos.toString();
            System.out.println("**** Response ******"+resp);
            bos.close();
            if(resp.indexOf("<ns:return>50</ns:return>") != -1)
                stat.addStatus("restful-svc-2", stat.PASS);
            else
                stat.addStatus("restful-svc-2", stat.FAIL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

