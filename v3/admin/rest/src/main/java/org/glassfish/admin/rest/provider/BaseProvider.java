/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author jasonlee
 */
@Provider
public abstract class BaseProvider<T> implements MessageBodyWriter<T> {
    @Context
    protected UriInfo uriInfo;

    protected String desiredType;
    protected MediaType supportedMediaType;

    public BaseProvider(String desiredType, MediaType mediaType) {
        this.desiredType = desiredType;
        this.supportedMediaType = mediaType;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] antns, MediaType mt) {
        try {
            if (Class.forName(desiredType).equals(genericType)) {
                return mt.isCompatible(supportedMediaType);
            }
        } catch (java.lang.ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T proxy, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(getContent(proxy).getBytes());
    }

    protected abstract String getContent(T proxy);
}
