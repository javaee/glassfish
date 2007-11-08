/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.validation;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import java.io.InputStreamReader;
import com.sun.org.apache.xml.internal.serializer.ToXMLStream;
import java.io.StringReader;
import java.io.BufferedWriter;

/**
   This class provides the mechanism to resolve variables present in
   some given {@link InputSource}.
*/

// This class works like a standard XMLFilter, except that it parses
// the input source twice. On the first pass through it constructs a
// frame set which holds all the variable definitions. On the second
// pass it uses those definitions to resolve all variable references.
// All that this class does is to wrap up the underlying classes that
// provide that functionality and coordinate their activities.
// The kind of bugs that will be present in this class will be those
// that are to do with XML handling and coordination, not variable
// dereferencing.

// I dont think that this really should be a filter - its been
// implemented as if its an XMLReader, and that's how its being used. 

public class VariableResolver extends XMLFilterImpl
{
    
    private final FrameHolderBuilder frameHolderBuilder = new FrameHolderBuilder();
    private final VariableExpander ve = new VariableExpander();
    
    
    VariableResolver() throws SAXException{
        super.setParent(getXMLReader());
    }


      /*
       * Parse the input source, using the given input source. Note
       * that the input source is used twice - this is probably an
       * error, in general. The solution is to store the first set of
       * events and then replay them.
       */

    public void parse(InputSource is) throws IOException, SAXException {
        final InputSource is2 = copyInputSource(is);
        super.setContentHandler(frameHolderBuilder);
        super.parse(is2);
        is2.getCharacterStream().reset();
        ve.setFramer(new Framer(frameHolderBuilder.getFrameHolder()));
        ve.setParent(getParent());
        ve.parse(is2);
    }

    private InputSource copyInputSource(InputSource is) throws IOException, SAXException {
        final StringWriter out = new StringWriter();
        final ToXMLStream xml = new ToXMLStream();
        xml.setWriter(out);
        final XMLReader r = getXMLReader();
        r.setContentHandler(xml);
        if (super.getEntityResolver() != null){
            r.setEntityResolver(super.getEntityResolver());
        }
        r.parse(is);
        final InputSource result= new InputSource(new StringReader(out.toString()){ public void close(){} });
        result.setPublicId(is.getPublicId());
        result.setSystemId(is.getSystemId());
        return result;
    }
    
    private XMLReader getXMLReader() throws SAXException {
        try{
        final XMLReader xr =  SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xr.setFeature("http://xml.org/sax/features/namespaces", true);
        xr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        return xr;
        }
        catch (ParserConfigurationException e){
            throw new SAXException(e);
        }
    }

    public void setContentHandler(ContentHandler contentHandler) {
        ve.setContentHandler(contentHandler);
    }

}
