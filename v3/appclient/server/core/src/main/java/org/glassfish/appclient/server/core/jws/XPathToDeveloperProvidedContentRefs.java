package org.glassfish.appclient.server.core.jws;

import com.sun.logging.LogDomains;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.glassfish.appclient.server.core.jws.servedcontent.Content;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.SimpleDynamicContentImpl;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;

/**
 * Abstracts the XPath information for developer-provided references to
 * other resources, whether static content (such as JARs or native libraries)
 * or dynamic content (such as other JNLP documents).  (Note that these
 * are dynamic content because the server adjusts them - even the
 * developer-provided ones - at HTTP request time with, for example, the
 * code base.)
 *
 * @param <T> either StaticContent or DynamicContent
 */
abstract class XPathToDeveloperProvidedContentRefs<T extends Content> {

    private static final String STATIC_REFS_PROPERTY_NAME = "static.refs";
    private static final String DYNAMIC_REFS_PROPERTY_NAME = "dynamic.refs";

    private final static XPathFactory xPathFactory = XPathFactory.newInstance();

    private final static XPath xPath = xPathFactory.newXPath();

    private static final Logger logger = LogDomains.getLogger(
            XPathToDeveloperProvidedContentRefs.class, LogDomains.ACC_LOGGER);
    

    private enum Type {

        STATIC(STATIC_REFS_PROPERTY_NAME), DYNAMIC(DYNAMIC_REFS_PROPERTY_NAME);
        private String propertyName;

        Type(final String propName) {
            this.propertyName = propName;
        }
    }
    private final XPathExpression xPathExpr;

    private XPathToDeveloperProvidedContentRefs(final String path) {
        super();
        try {
            xPathExpr = xPath.compile(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static List<XPathToDeveloperProvidedContentRefs> parse(final Properties p) {
        List<XPathToDeveloperProvidedContentRefs> result = new ArrayList<XPathToDeveloperProvidedContentRefs>();
        result.addAll(XPathToDeveloperProvidedContentRefs.
                parse(p, XPathToDeveloperProvidedContentRefs.Type.STATIC));
        result.addAll(XPathToDeveloperProvidedContentRefs.
                parse(p, XPathToDeveloperProvidedContentRefs.Type.DYNAMIC));
        return result;
    }
    
    /**
     * Extracts the relevant information from the Properties object and
     * creates the correct set of content objects depending on which type
     * of xpath reference the caller requested (static or dynamic).
     * @param p Properties read from the on-disk config file
     * @param type static or dynamic
     * @return xpath-related objects for the selected type of reference points
     */
    private static List<XPathToDeveloperProvidedContentRefs> parse(final Properties p, Type type) {
        final List<XPathToDeveloperProvidedContentRefs> result = new ArrayList<XPathToDeveloperProvidedContentRefs>();
        final String refs = p.getProperty(type.propertyName);
        for (String ref : refs.split(",")) {
            result.add((type == Type.STATIC) ? new XPathToStaticContent(ref) : new XPathToDynamicContent(ref));
        }
        return result;
    }

    XPathExpression xPathExpr() {
        return xPathExpr;
    }

    /**
     * Adds the referenced data for this object to either the static
     * or dynamic content, depending on whether this object is for
     * static or dynamic content.
     * <p>
     * The concrete implementation in the subclasses will actually
     * update either staticContent or dynamicContent but not both.
     * But providing both as arguments lets the caller not worry about
     * which type of xpath content this object is.
     *
     * @param codebase the code base from the containing document
     * @param pathToContent location of the content (relative or absolute)
     * @param loader class loader which could be used to locate referenced content
     * @param staticContent static content map
     * @param dynamicContent dynamic content map
     * @param appRootURI root URI for the application
     * @throws URISyntaxException
     * @throws IOException
     */
    abstract void addToContentIfInApp(
            DeveloperContentHandler dch,
            String referringDocument,
            URI codebase, String pathToContent,
            ClassLoader loader, Map<String, StaticContent> staticContent,
            Map<String, DynamicContent> dynamicContent, final URI appRootURI)
                throws URISyntaxException, IOException;

/**
     * Models XPath-related information for a developer-provided reference to
     * static content (such as to a JAR, a native library, or an image).
     */
    private static class XPathToStaticContent extends XPathToDeveloperProvidedContentRefs<StaticContent> {

        XPathToStaticContent(final String path) {
            super(path);
        }

        @Override
        void addToContentIfInApp(
                final DeveloperContentHandler dch,
                final String referringDocument,
                final URI codebase,
                final String pathToContent,
                final ClassLoader loader,
                final Map<String,StaticContent> staticContent,
                final Map<String,DynamicContent> dynamicContent,
                final URI appRootURI) throws URISyntaxException {
            final URI uriToContent = new URI(pathToContent);
            final URI absURI = codebase.resolve(uriToContent);
            if (absURI.equals(uriToContent)) {
                return;
            }
            final URI fileURI = appRootURI.resolve(pathToContent);
            final File f = new File(fileURI);
            /*
             * The developer might have referred to a JAR or other static file
             * that is not actually in the app.  If so, log a warning.
             */
            if ( ! f.exists() || ! f.canRead()) {
                logger.log(Level.WARNING,
                        "enterprise.deployment.appclient.jws.clientJNLPBadStaticContent",
                        new Object[] {referringDocument, pathToContent});
            } else {
                staticContent.put(pathToContent, new FixedContent(f));
            }
        }
    }

    /**
     * Models Xpath-related information for a developer-provided reference to
     * dynamic content (such as another JNLP document).
     */
    private static class XPathToDynamicContent extends XPathToDeveloperProvidedContentRefs<DynamicContent>{

        XPathToDynamicContent(final String path) {
            super(path);
        }

        @Override
        void addToContentIfInApp(
                final DeveloperContentHandler dch,
                final String referringDocument,
                final URI codebase,
                final String pathToContent,
                final ClassLoader loader,
                final Map<String,StaticContent> staticContent,
                final Map<String,DynamicContent> dynamicContent,
                final URI appRootURI) throws URISyntaxException, IOException {
            final URI uriToContent = new URI(pathToContent);
            final URI absURI = codebase.resolve(uriToContent);
            if (absURI.equals(uriToContent)) {
                return;
            }
            /*
             * Find the developer-provided content.
             */

            InputStream is = loader.getResourceAsStream(pathToContent);
            if (is == null) {
                return;
            }

            final byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            is.close();
            dynamicContent.put(pathToContent,
                    new SimpleDynamicContentImpl(
                        baos.toString(),
                        URLConnection.guessContentTypeFromName(pathToContent)));

            /*
             * Currently the only dynamic content processed from the developer's
             * JNLP is an <extension> element which refers to another
             * JNLP document.  So we need to recursively process that
             * document now also.
             */
            dch.addDeveloperContent(pathToContent);
        }

    }
}
