package com.sun.ejb.base.io;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import org.jvnet.hk2.annotations.Service;

import java.io.*;

/**
 * Simple wrapper to IOUtils
 * 
 */
@Service
public class JavaEEIOUtilsImpl
    implements JavaEEIOUtils {

    @Override
    public ObjectInputStream createObjectInputStream(InputStream is, boolean resolveObject, ClassLoader loader) throws Exception {
        return IOUtils.createObjectInputStream(is, resolveObject, loader);
    }

    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream os, boolean replaceObject) throws IOException {
        return IOUtils.createObjectOutputStream(os, replaceObject);
    }

    @Override
    public byte[] serializeObject(Object obj, boolean replaceObject) throws IOException {
        return IOUtils.serializeObject(obj, replaceObject);
    }

    @Override
    public Object deserializeObject(byte[] data, boolean resolveObject, ClassLoader appClassLoader) throws Exception {
        return IOUtils.deserializeObject(data, resolveObject, appClassLoader);
    }

}
