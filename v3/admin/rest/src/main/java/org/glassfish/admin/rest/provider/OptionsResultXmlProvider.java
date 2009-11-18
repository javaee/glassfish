/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.glassfish.admin.rest.Constants;

/**
 * XML provider for OptionsResult.
 *
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class OptionsResultXmlProvider extends ProviderUtil implements MessageBodyWriter<OptionsResult> {

     @Context
     protected UriInfo uriInfo;

     @Override
     public long getSize(final OptionsResult proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
          return -1;
     }


     @Override
     public boolean isWriteable(final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
         return type.equals(OptionsResult.class);
     }


     @Override
     public void writeTo(final OptionsResult proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType,
               final MultivaluedMap<String, Object> httpHeaders,
               final OutputStream entityStream) throws IOException, WebApplicationException {
         entityStream.write(getXml(proxy).getBytes());
     }


     //get xml representation for the given OptionsResult object
     private String getXml(OptionsResult proxy) {
        String result;
        String indent = Constants.INDENT;
        result = "<" + proxy.getName() + ">" ;

        result = result + getRespresenationForMethodMetaData(proxy, indent);

        result = result + "\n" + "</" + proxy.getName() + ">" ;
        return result;
    }


    String getRespresenationForMethodMetaData(OptionsResult proxy, String indent) {
        String result = "";
        Set<String> methods = proxy.methods();
        Iterator<String> iterator = methods.iterator();
        String method;

        while (iterator.hasNext()) {
           method = iterator.next();

           //method
           result = result + getMethod(method, indent);

           MethodMetaData methodMetaData = proxy.getMethodMetaData(method);

           //query params`
           result = result + getQueryParams(methodMetaData,
               indent + Constants.INDENT);

           //parameters (message parameters)
           result = result + getMessageParams(methodMetaData,
               indent + Constants.INDENT);

           result = result + "\n" + indent;
           result = result + getEndXmlElement("Method");
        }
        return result;
    }


    //get xml representation for the given method name
    private String getMethod(String method, String indent) {
        String result = "\n" + indent + "<";
        result = result + "Method name=";
        result = result + quote(method);
        result = result + ">";
        return result;
    }


    //get xml representation for the method query parameters
    private String getQueryParams(MethodMetaData methodMetaData,
            String indent) {
        String result = "";
        if (methodMetaData.sizeQueryParamMetaData() > 0) {
            result = result + "\n" + indent;
            result = result + "<Query-Parameters>";

            Set<String> queryParams = methodMetaData.queryParams();
            Iterator<String> iterator = queryParams.iterator();
            String queryParam;
            while (iterator.hasNext()) {
                queryParam = iterator.next();
                ParameterMetaData parameterMetaData =
                    methodMetaData.getQureyParamMetaData(queryParam);
                result = result + getParameter(queryParam, parameterMetaData,
                    indent + Constants.INDENT);
            }
            result = result + "\n" + indent;
            result = result + "</Query-Parameters>";
        }
        return result;
    }


    //get xml representation for the method message parameters
    private String getMessageParams(MethodMetaData methodMetaData,
            String indent) {
        String result = "";
        if (methodMetaData.sizeParameterMetaData() > 0) {
            result = result + "\n" + indent;
            result = result + "<Message-Parameters>";

            Set<String> parameters = methodMetaData.parameters();
            Iterator<String> iterator = parameters.iterator();
            String parameter;
            while (iterator.hasNext()) {
               parameter = iterator.next();
               ParameterMetaData parameterMetaData =
                   methodMetaData.getParameterMetaData(parameter);
               result = result + getParameter(parameter, parameterMetaData,
                   indent + Constants.INDENT);
            }
            result = result + "\n" + indent;
            result = result + "</Message-Parameters>";
        }
        return result;
    }


    //get xml representation for the given parameter
    private String getParameter(String parameter,
        ParameterMetaData parameterMetaData, String indent) {
        String result = "\n" + indent;

        result = result + "<" + parameter;

        Set<String> attributes = parameterMetaData.attributes();
        Iterator<String> iterator = attributes.iterator();
        String attributeName;
        while (iterator.hasNext()) {
           attributeName = iterator.next();
           String attributeValue =
               parameterMetaData.getAttributeValue(attributeName);
           result = result + getAttribute(attributeName, attributeValue);
        }
        result = result + "/>";
        return result;
    }


    //get xml representation for a give attribute of parameter
    private String getAttribute(String name, String value) {
        String result = " ";
        name = name.replace(' ', '-');
        result = result + name + "=" + quote(value);
        return result;
    }

}
