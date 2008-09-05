package com.sun.enterprise.container.common.impl.util;

import com.sun.enterprise.container.common.spi.util.JavaEEObjectStreamHandler;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Mahesh Kannan
 *         Date: Sep 3, 2008
 */
@Service
public class JavaEEObjectStreamHandlerForJNDIContext
        implements JavaEEObjectStreamHandler {

    @Inject
    GlassfishNamingManager gfNM;

    public Object replaceObject(Object obj)
            throws IOException {
        Object result = obj;
        if (obj instanceof Context) {
            Context ctx = (Context) obj;
            try {
                // Serialize state for a jndi context.  The spec only requires
                // support for serializing contexts pointing to java:comp/env
                // or one of its subcontexts.  We also support serializing the
                // references to the the default no-arg InitialContext, as well
                // as references to the the contexts java: and java:comp. All
                // other contexts will either not serialize correctly or will
                // throw an exception during deserialization.
                result = new SerializableJNDIContext(ctx.getNameInNamespace());
            } catch (NamingException ex) {
                IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            }
        }

        return result;
    }

    public Object resolveObject(Object obj)
        throws IOException {
        Object result = obj;
        if (obj instanceof SerializableJNDIContext) {
            SerializableJNDIContext sctx = (SerializableJNDIContext) obj;
            try {
                String name = sctx.getName();
                if ((name == null) || (name.length() == 0)) {
                    result = new InitialContext();
                } else {
                    result = gfNM.restoreJavaCompEnvContext(name);
                }
            } catch (NamingException namEx) {
                IOException ioe = new IOException();
                ioe.initCause(namEx);
                throw ioe;
            }
        }

        return result;
    }

    private static final class SerializableJNDIContext
        implements Serializable {

        private String name;

        SerializableJNDIContext(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
