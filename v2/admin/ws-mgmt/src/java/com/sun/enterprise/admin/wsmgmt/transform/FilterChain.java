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
package com.sun.enterprise.admin.wsmgmt.transform;

import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;

import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import com.sun.enterprise.admin.wsmgmt.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPPart;

import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Ordered XSLT filter chain. 
 */
public class FilterChain {

    /**
     * Public Constructor.
     *
     * @param appId   name of the application
     * @param endpoint   end point name for which stats are collected
     */
    public FilterChain(TransformationRule[] rules, boolean reverse) 
                throws TransformException {
        try {
            // Set up to the Transformer
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser parser = spf.newSAXParser();
            _reader = parser.getXMLReader();
   
            _stf = (SAXTransformerFactory) TransformerFactory.newInstance();
            _transformer = _stf.newTransformer();
            setupTemplates(rules, reverse);
        } catch(Exception e) {
            throw new TransformException(e);
        }
    }

    private synchronized void setupTemplates(TransformationRule[] tRules,
            boolean reverse)
            throws TransformException {
        if ((tRules == null) || (tRules.length == 0)) {
            return;
        }
        templatesData = new TemplatesData[tRules.length];
        
        if (!reverse) {    
            for(int idx =0; idx < tRules.length; idx++) {
                addTemplate(idx, tRules[idx]);
            }
        } else {
            int index =0;
            for(int idx = tRules.length -1; idx > -1; idx--) {
                addTemplate(index, tRules[idx]);
                index++;
            }
        }
    }

    private synchronized void addTemplate(int index, TransformationRule tRule) 
            throws TransformException {
        try {
            if (tRule == null) {
                return;
            }
            String name = tRule.getName();
            // check if the Rule File Location is relative, then prepend the 
            // instance root to the path
            File stylesheet = new File(tRule.getRuleFileLocation());
            if ( !stylesheet.isAbsolute()) {
                // construct the absolute path
                String instanceRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

                stylesheet = new File( instanceRoot 
                 + File.separator+tRule.getRuleFileLocation());
            } 
//            XMLFilter filter = _stf.newXMLFilter(new StreamSource(stylesheet));
            templatesData[index] = 
                new TemplatesData(name, _stf.newTemplates(new StreamSource(stylesheet)));            
            // addToList(index,name,templates);
        } catch(Exception e) {
            String msg = _stringMgr.getString("rule_not_compiled",
            tRule.getRuleFileLocation(), tRule.getName());
            _logger.log(Level.WARNING, msg); 
            throw new TransformException(e);
        }
    }

    private LinkedList getFilterList() throws TransformerConfigurationException {
        LinkedList filterList = new LinkedList();
        if (templatesData == null)
            return filterList;
        for (int i = 0; i < templatesData.length; i++) {
            TemplatesData t = templatesData[i];
            XMLFilter f = _stf.newXMLFilter(t.getTemplates());
            addToList(filterList, i, f);
        }
        return filterList;
    }
    
    private void addToList(LinkedList filterList, int index, XMLFilter filter) {
        XMLFilter parent = null;
        if ( index == 0) {
            filter.setParent(_reader);
        } else {
            parent = (XMLFilter) filterList.get(index-1);
            filter.setParent(parent);
            // check if child the exists
            if ( filterList.size() -1 > index) {
                XMLFilter child = (XMLFilter) filterList.get(index);
                if ( child != null) {
                    child.setParent(filter);
                }
            }
        }
        filterList.add(index, filter);        
    }

    /**
     * Invoke the filter.
     * 
     * @param  context  filter context 
     */
    public synchronized void process(FilterContext context) 
            throws TransformException, TransformerConfigurationException  {
            
        LinkedList filterList = getFilterList();
        if (( filterList.size() == 0 ) || (context == null)) {
            return;
        }

        try {

             SOAPMessageContext smc = context.getMessageContext();
             SOAPMessage sm = smc.getMessage();
             SOAPPart sp = sm.getSOAPPart();
             // gets the SOAP envelope as a JAXP Source object.
             Source s = sp.getContent();

            ByteArrayInputStream bis = null;
            InputSource inputSrc;
            if (s instanceof SAXSource) {
                inputSrc = SAXSource.sourceToInputSource(s);
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer xtransformer = tf.newTransformer();
                xtransformer.transform(s, new StreamResult(bos));
                byte[] buf = bos.toByteArray();
                bos.flush();
                bos.close();
                
                bis = new ByteArrayInputStream(buf);
                inputSrc = new InputSource(bis);
            }

            // Set up the transformer to process the SAX events generated
            // by the last filter in the chain
            //inputSrc = SAXSource.sourceToInputSource(s);
            SAXSource transformSource = new SAXSource( 
                ((XMLFilter)filterList.getLast()), inputSrc);

             StringWriter sw = new StringWriter(STRING_WRITER_INIT_SIZE);
             Result r = new StreamResult(sw);
             _transformer.transform(transformSource, r);
             // get the string from the result.
             sw.flush();
             sw.close();
             if (bis != null)
                bis.close();
             StringBufferInputStream in = new StringBufferInputStream(
                            sw.toString());
             MessageFactory mFactory = MessageFactory.newInstance();
             SOAPMessage resMsg = mFactory.createMessage(null, in);
                 smc.setMessage(resMsg);


        } catch(Exception e) {
            _logger.fine("XSLT transformation failed for " 
                + context.getFullyQualifiedName());
            throw new TransformException(e);
        }
    }

    // -- PRIVATE - VARIABLES -------------------------
    XMLReader _reader = null;
    SAXTransformerFactory _stf = null;
    Transformer _transformer = null;
    TemplatesData[] templatesData = null;
    
    // intial size for StringWriter
    static final int STRING_WRITER_INIT_SIZE = 4028;

     private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(FilterChain.class);

}
