/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.runtime.JnlpDocDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.SimpleDynamicContentImpl;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;
import org.glassfish.appclient.server.core.jws.servedcontent.TokenHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Processes developer-provided content in (or directly or indirectly
 * referenced from) an optional JNLP file included in the client or the EAR.
 *
 * @author tjquinn
 */
public class DeveloperContentHandler {

    private final ClassLoader loader;
    private final JnlpDocDescriptor jnlpDoc;
    private final Map<String,StaticContent> staticContent;
    private final Map<String,DynamicContent> dynamicContent;
    private final TokenHelper tHelper;
    private final File appRootDir;
    private final URI appRootURI;
    
    private static DocumentBuilderFactory dbf = null;

    private final static XPathFactory xPathFactory = XPathFactory.newInstance();
    
    private final static XPath xPath = xPathFactory.newXPath();

    private abstract class XPathToDeveloperProvidedContent {

        private final XPathExpression xPathExpr;

        XPathToDeveloperProvidedContent(final String path) {
            try {
                xPathExpr = xPath.compile(path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        XPathExpression xPathExpr() {
            return xPathExpr;
        }

        abstract void addToContentIfInApp(
                URI codebase, 
                String pathToContent,
                ClassLoader loader) 
                throws URISyntaxException, IOException;
    }

    private class XPathToStaticContent extends XPathToDeveloperProvidedContent {

        XPathToStaticContent(final String path) {
            super(path);
        }

        @Override
        void addToContentIfInApp(
                final URI codebase, 
                final String pathToContent,
                final ClassLoader loader) throws URISyntaxException {
            final URI uriToContent = new URI(pathToContent);
            final URI absURI = codebase.resolve(uriToContent);
            if (absURI.equals(uriToContent)) {
                return;
            }
            final URI fileURI = appRootURI.resolve(pathToContent);
            final File f = new File(fileURI);
            staticContent.put(pathToContent, new FixedContent(f));
        }
    }
    
    private class XPathToDynamicContent extends XPathToDeveloperProvidedContent {

        XPathToDynamicContent(final String path) {
            super(path);
        }

        @Override
        void addToContentIfInApp(
                final URI codebase, 
                final String pathToContent,
                final ClassLoader loader) throws URISyntaxException, IOException {
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
        }
        
    }

    private final XPathToDeveloperProvidedContent[] xPathsToDevContent = new XPathToDeveloperProvidedContent[] {
        new XPathToStaticContent("jnlp/information/homepage/@href"),
        new XPathToStaticContent("jnlp/information/icon/@href"),
        new XPathToStaticContent("jnlp/resources/java/resources/jar/@href"),
        new XPathToStaticContent("jnlp/resources/jar/@href"),
        new XPathToStaticContent("jnlp/resources/nativelib/@href"),
        new XPathToStaticContent("jnlp/related-content/@href"),
        
        new XPathToDynamicContent("jnlp/resources/extension/@href")
    };
    

    
    private final static CombinedXPath[] xPaths = new CombinedXPath[] {
        new CombinedXPath.OwnedXPath(xPath, "/jnlp", "/@codebase"),
        new CombinedXPath.OwnedXPath(xPath, "/jnlp", "/@href"),
        new CombinedXPath.OwnedXPath(xPath, "/jnlp", "/security"),
        new CombinedXPath.OwnedXPath(xPath, "/jnlp", "/application-desc"),

        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/@spec"),
        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/@version"),
        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/information"),
        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/resources/javase"),
        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/resources/javase/@version"),
        new CombinedXPath.DefaultedXPath(xPath, "/jnlp", "/resources/javase/@java-vm-args"),

        new CombinedXPath.MergedXPath(xPath, "/jnlp/resources", "/jar"),
        new CombinedXPath.MergedXPath(xPath, "/jnlp/resources", "/property"),
        new CombinedXPath.MergedXPath(xPath, "/jnlp/resources", "/extension")
    };


    public static DeveloperContentHandler addDeveloperContent(
            final ClassLoader loader,
            final JnlpDocDescriptor jnlpDoc,
            final TokenHelper tHelper,
            final File appRootDir,
            final Map<String,StaticContent> staticContent,
            final Map<String,DynamicContent> dynamicContent) {

        DeveloperContentHandler h = new DeveloperContentHandler(loader, jnlpDoc,
                tHelper, appRootDir, staticContent, dynamicContent);
        h.process();
        return h;
    }

    private DeveloperContentHandler(ClassLoader loader, JnlpDocDescriptor jnlpDoc,
            final TokenHelper tHelper, 
            final File appRootDir,
            final Map<String,StaticContent> staticContent,
            final Map<String,DynamicContent> dynamicContent) {
        this.loader = loader;
        this.jnlpDoc = jnlpDoc;
        this.tHelper = tHelper;
        this.appRootDir = appRootDir;
        this.appRootURI = appRootDir.toURI();
        this.staticContent = staticContent;
        this.dynamicContent = dynamicContent;
    }

    private void processDeveloperJNLP() {
        /*
         * There is no work to do unless the developer specified a JNLP
         * document.
         */
        String href;
        if (jnlpDoc == null || (href = jnlpDoc.getHref()) == null) {
            return;
        }

        /*
         * Find the developer's JNLP.
         */
        final InputStream devJNLPStream = loader.getResourceAsStream(href);
        if (devJNLPStream == null) {
            return;
        }

        /*
         * Get the generated main JNLP document.
         */
        final String generatedJNLP = dynamicContent.get(tHelper.mainJNLP())
                .getOrCreateInstance(new Properties())
                .getText();
        final InputSource generatedJNLPSource = new InputSource(new StringReader(generatedJNLP));

        /*
         * Start with the developer-provided document, then override the parts
         * that we insist on providing ourselves, then merge in other parts that
         * we add to the developer's corresponding parts.
         */
        DocumentBuilder db;
        Document developerDOM;
        Document gfDOM;
        try {
            db = getDocumentBuilderFactory().newDocumentBuilder();
            developerDOM = db.parse(devJNLPStream);
            gfDOM = db.parse(generatedJNLPSource);

            for (CombinedXPath combinedXPath : xPaths) {
                combinedXPath.process(developerDOM, gfDOM);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }




    }

    private void process() {
        /*
         * There is no work to do unless the developer specified a JNLP
         * document.
         */
        String jnlpDocHref;
        if (jnlpDoc == null || (jnlpDocHref = jnlpDoc.getHref()) == null) {
            return;
        }

        /*
         * Find the developer's JNLP.
         */
        final InputStream devJNLPStream = loader.getResourceAsStream(jnlpDocHref);
        if (devJNLPStream == null) {
            return;
        }

        DocumentBuilder db;
        Document developerDOM;
        try {
            final URI codebaseURI = new URI(tHelper.appCodebasePath());
            db = getDocumentBuilderFactory().newDocumentBuilder();
            developerDOM = db.parse(devJNLPStream);
            /*
             * Search for hrefs to static content.  Add each that falls within
             * the codebase to the static content.
             */
             for (XPathToDeveloperProvidedContent c : xPathsToDevContent) {
                 NodeList nodes = (NodeList) c.xPathExpr().evaluate(developerDOM, XPathConstants.NODESET);
                 if (nodes.getLength() > 0) {
                     for (int i = 0; i < nodes.getLength(); i++) {
                         final String href = nodes.item(i).getNodeValue();
                         c.addToContentIfInApp(codebaseURI, href, loader);
                     }
                 }
             }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    
    private synchronized static DocumentBuilderFactory getDocumentBuilderFactory() {
        if (dbf == null) {
            dbf = DocumentBuilderFactory.newInstance();
        }
        return dbf;
    }

}
