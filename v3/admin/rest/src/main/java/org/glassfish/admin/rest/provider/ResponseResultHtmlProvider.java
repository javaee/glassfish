/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.admin.rest.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.glassfish.admin.rest.Util.*;

/**
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class ResponseResultHtmlProvider extends ProviderUtil
        implements MessageBodyWriter<String> {

    @Context
    protected UriInfo uriInfo;

    @Override
    public long getSize(final String proxy, final Class<?> type, final Type genericType,
                final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }


    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        try {
            if (Class.forName("java.lang.String").equals(genericType)) {
                return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
            }
        } catch (java.lang.ClassNotFoundException e) {
            return false;
        }
        return false;
    }


    @Override
    public void writeTo(final String proxy, final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(getHtml(proxy).getBytes());
    }


    private String getHtml(String proxy) {
        String result = getHtmlHeader();
        String uri = uriInfo.getAbsolutePath().toString();
        String name = upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));
        String parentName =
            upperCaseFirstLetter(eleminateHypen(getParentName(uri)));

        result = result + "<h1>" + name + "</h1>";
        result = result + proxy + "<br><br>";
        result = result + "<a href=\"" + uri + "\">Back</a><br>";

        result = "<div>" + result + "</div>" + "<br>";
        result = result + "</body></html>";
        return result;
    }
}
