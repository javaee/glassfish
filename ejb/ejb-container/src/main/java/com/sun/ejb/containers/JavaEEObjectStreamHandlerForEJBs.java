package com.sun.ejb.containers;

import com.sun.enterprise.container.common.spi.util.JavaEEObjectStreamHandler;
import com.sun.ejb.spi.io.IndirectlySerializable;
import com.sun.ejb.spi.io.SerializableObjectFactory;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.Serializable;

/**
 * A small JavaEEObjectStreamHandler that wraps the old IndirectlySerializable
 *  AND SerializableObjectFactory
 * 
 * @author Mahesh Kannan
 *         Date: Sep 3, 2008
 */
@Service
public class JavaEEObjectStreamHandlerForEJBs
        implements JavaEEObjectStreamHandler {

    public Object replaceObject(Object obj)
            throws IOException {
        Object result = obj;
        if (obj instanceof IndirectlySerializable) {
            result = ((IndirectlySerializable) obj).getSerializableObjectFactory();
        }
        return result;
    }

    public Object resolveObject(Object obj)
        throws IOException {
        Object result = obj;
        if (obj instanceof SerializableObjectFactory) {
            result = ((SerializableObjectFactory) obj).createObject();
        }
        return result;
    }

}
