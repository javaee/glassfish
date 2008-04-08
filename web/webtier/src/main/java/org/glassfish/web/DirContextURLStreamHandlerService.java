package org.glassfish.web;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.naming.resources.DirContextURLStreamHandler;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is responsible for adding {@see DirContextURLStreamHandler}
 * to OSGi service registry.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DirContextURLStreamHandlerService
        extends AbstractURLStreamHandlerService
        implements URLStreamHandlerService, BundleActivator {

    // We have to extend DirContextURLStreamHandler so that we
    // can make openConnection and toExternalForm available as
    // public methods.
    private static class DelegatingDirContextURLStreamHandler
            extends DirContextURLStreamHandler{
        @Override
        public URLConnection openConnection(URL u) throws IOException {
            return super.openConnection(u);
        }

        @Override
        public String toExternalForm(URL u) {
            return super.toExternalForm(u);
        }
    }

    public URLConnection openConnection(URL u) throws IOException {
        return new DelegatingDirContextURLStreamHandler().openConnection(u);
    }

    @Override
    public String toExternalForm(URL u) {
        return new DelegatingDirContextURLStreamHandler().toExternalForm(u);
    }

    public void start(BundleContext context) throws Exception {
        Properties p = new Properties();
        p.put(URLConstants.URL_HANDLER_PROTOCOL,
                new String[]{"jndi"});
        context.registerService(
                URLStreamHandlerService.class.getName(),
                this,
                p);
    }

    public void stop(BundleContext context) throws Exception {
    }
}
