/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package org.glassfish.embed.impl;

import org.glassfish.web.WebEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class EntityResolverImpl extends WebEntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String fileName = knownDTDs.get(publicId);
        if(fileName!=null) {
            URL url = getClass().getResource("/glassfish/lib/dtds/"+fileName);
            if(url!=null)
                return new InputSource(url.toExternalForm());
        }
        return null;
    }
}
