/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * JSON provider for OptionsResult.
 *
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class OptionsResultJsonProvider extends ProviderUtil implements MessageBodyWriter<OptionsResult> {

     @Context
     protected UriInfo uriInfo;

     public long getSize(final OptionsResult proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
          return -1;
     }


     public boolean isWriteable(final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
         return type.equals(OptionsResult.class);
     }


     public void writeTo(final OptionsResult proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType,
               final MultivaluedMap<String, Object> httpHeaders,
               final OutputStream entityStream) throws IOException, WebApplicationException {
         entityStream.write(getJson(proxy).getBytes());
     }


     //get json representation for the given OptionsResult object
     private String getJson(OptionsResult proxy) {
        String result;
        String indent = Constants.INDENT;
        result ="{" + quote(proxy.getName()) + ":";
        result = result + "\n" + indent + "{";
        result = result + getRespresenationForMethodMetaData(proxy,
            indent + Constants.INDENT);
        result = result + "\n" + indent + "}";
        result = result + "\n}";
        return result;
    }


    String getRespresenationForMethodMetaData(OptionsResult proxy, String indent) {
        String result = "";
        Set<String> methods = proxy.methods();
        Iterator<String> iterator = methods.iterator();
        String method;
        boolean first = true;
        while (iterator.hasNext()) {
           method = iterator.next();
           if (!first) {
               result = result + ",";
           }

           MethodMetaData methodMetaData = proxy.getMethodMetaData(method);

           //get method representation
           result = result + getMethod(method, methodMetaData, indent);

           first = false;
        }
        return result;
    }


    //get json representation for the given method name
    private String getMethod(String method, MethodMetaData methodMetaData, String indent) {
        String result = "\n" + indent;
        result = result + quote("Method") + ":{";
        result = result + "\n" + indent + Constants.INDENT +
            quote("Name") + ":" + quote(method);

        //query params
        result = result + getQueryParams(methodMetaData, indent + Constants.INDENT);

        //parameters (message parameters)
        result = result + getMessageParams(methodMetaData, indent + Constants.INDENT);

        result = result + "\n" + indent + "}";
        return result;
    }


    //get json representation for the method query parameters
    private String getQueryParams(MethodMetaData methodMetaData, String indent) {
        String result = "";
        if (methodMetaData.sizeQueryParamMetaData() > 0) {
            result = result + "," + "\n" + indent;
            result = result + quote("Query Parameters") + ":{";

            Set<String> queryParams = methodMetaData.queryParams();
            Iterator<String> iterator = queryParams.iterator();
            String queryParam;
            boolean first = true;
            while (iterator.hasNext()) {
               if (!first) {
                   result = result + ",";
               }

                queryParam = iterator.next();
                ParameterMetaData parameterMetaData =
                    methodMetaData.getQureyParamMetaData(queryParam);
                result = result + getParameter(queryParam, parameterMetaData,
                    indent + Constants.INDENT);
                first = false;
            }
            result = result + "\n" + indent + "}";
        }
        return result;
    }


    //get json representation for the method message parameters
    private String getMessageParams(MethodMetaData methodMetaData, String indent) {
        String result = "";
        if (methodMetaData.sizeParameterMetaData() > 0) {
            result = result + "," + "\n" + indent;
            result = result + quote("Message Parameters") + ":{";

            Set<String> parameters = methodMetaData.parameters();
            Iterator<String> iterator = parameters.iterator();
            String parameter;
            boolean first = true;
            while (iterator.hasNext()) {
               if (!first) {
                   result = result + ",";
               }
               parameter = iterator.next();
               ParameterMetaData parameterMetaData =
                   methodMetaData.getParameterMetaData(parameter);
               result = result + getParameter(parameter, parameterMetaData,
                   indent + Constants.INDENT);
               first = false;
            }
            result = result + "\n" + indent + "}";
        }
        return result;
    }


    //get json representation for the given parameter
    private String getParameter(String parameter,
        ParameterMetaData parameterMetaData, String indent) {
        String result = "\n" + indent;

        result = result + quote(parameter) + ":{";

        Set<String> attributes = parameterMetaData.attributes();
        Iterator<String> iterator = attributes.iterator();
        String attributeName;
        boolean first = true;
        while (iterator.hasNext()) {
           if (!first) {
               result = result + ", ";
           }
           attributeName = iterator.next();
           String attributeValue =
               parameterMetaData.getAttributeValue(attributeName);
           result = result + getAttribute(attributeName, attributeValue);
           first = false;
        }
        result = result + "}";
        return result;
    }


    //get json representation for a give attribute of parameter
    private String getAttribute(String name, String value) {
        String result = "";
        result = result + quote(name) + ":" + quote(value);
        return result;
    }

}
