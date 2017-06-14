/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package jaxb1.impl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.unmarshaller.Messages;
import com.sun.xml.bind.unmarshaller.Tracer;
import com.sun.xml.bind.util.AttributesImpl;

/**
 * Implementation of {@link UnmarshallerHandler}.
 * 
 * This object converts SAX events into unmarshaller events and
 * cooridnates the entire unmarshalling process.
 *
 * @author
 *  <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class SAXUnmarshallerHandlerImpl
    implements SAXUnmarshallerHandler, UnmarshallingContext
{
    /**
     * This flag is set to true at the startDocument event
     * and false at the endDocument event.
     * 
     * Until the first document is unmarshalled, we don't
     * want to return an object. So this variable is initialized
     * to true.
     */
    private boolean isUnmarshalInProgress = true;
    
    
    
    public SAXUnmarshallerHandlerImpl( UnmarshallerImpl _parent, GrammarInfo _gi ) {
        this.parent = _parent;
        grammarInfo = _gi;
        startPrefixMapping("",""); // by default, the default ns is bound to "".
     }
    
    private final GrammarInfo grammarInfo;
    public GrammarInfo getGrammarInfo() { return grammarInfo; }
    
    /**
     * Returns true if we should be collecting characters in the current element.
     */
    private final boolean shouldCollectText() {
        return collectText[stackTop];
    }
    
    public void startDocument() throws SAXException {
        // reset the object
        result = null;
        handlerLen=0;
        patchers=null;
        patchersLen=0;
        aborted = false;
        isUnmarshalInProgress = true;
        
        stackTop=0;
        elementDepth=1;
    }
    
    public void endDocument() throws SAXException {
        runPatchers();
        isUnmarshalInProgress = false;
    }
    
    public void startElement( String uri, String local, String qname, Attributes atts )
            throws SAXException {
        
        // work gracefully with misconfigured parsers that don't support namespaces
        if( uri==null )
            uri="";
        if( local==null || local.length()==0 )
            local=qname;
        if( qname==null || qname.length()==0 )
            qname=local;
        
        if(result==null) {
            // this is the root element.
            // create a root object and start unmarshalling
            UnmarshallingEventHandler unmarshaller =
                grammarInfo.createUnmarshaller(uri,local,this);
            if(unmarshaller==null) {
                // the registry doesn't know about this element.
                //
                // the no.1 cause of this problem is that your application is configuring
                // an XML parser by your self and you forgot to call
                // the SAXParserFactory.setNamespaceAware(true). When this happens, you see
                // the namespace URI is reported as empty whereas you expect something else.
                throw new SAXParseException(
                    Messages.format( "Messages.UNEXPECTED_ROOT_ELEMENT2",
                        uri, local, computeExpectedRootElements() ),
                    getLocator() );
            }
            result = unmarshaller.owner();

            pushContentHandler(unmarshaller,0);
        }
    
        processText(true);
    
        getCurrentHandler().enterElement(uri,local,qname,atts);
    }

    public final void endElement( String uri, String local, String qname )
            throws SAXException {
        
        // work gracefully with misconfigured parsers that don't support namespaces
        if( uri==null )
            uri="";
        if( local==null || local.length()==0 )
            local=qname;
        if( qname==null || qname.length()==0 )
            qname=local;
        
        processText(false);
        getCurrentHandler().leaveElement(uri,local,qname);
    }
    
    
    
    
    
    /** Root object that is being unmarshalled. */
    private Object result;
    public Object getResult() throws UnmarshalException {
        if(isUnmarshalInProgress)
            throw new IllegalStateException();
        
        if(!aborted)       return result;
        
        // there was an error.
        throw new UnmarshalException((String)null);
    }

    
    
//
//
// handler stack maintainance
//
//
    private UnmarshallingEventHandler[] handlers = new UnmarshallingEventHandler[16];
    private int[] mementos = new int[16];
    private int handlerLen=0;
    
    public void pushContentHandler( UnmarshallingEventHandler handler, int memento ) {
        if(handlerLen==handlers.length) {
            // expand buffer
            UnmarshallingEventHandler[] h = new UnmarshallingEventHandler[handlerLen*2];
            int[] m = new int[handlerLen*2];
            System.arraycopy(handlers,0,h,0,handlerLen);
            System.arraycopy(mementos,0,m,0,handlerLen);
            handlers = h;
            mementos = m;
        }
        handlers[handlerLen] = handler;
        mementos[handlerLen] = memento;
        handlerLen++;
    }
    
    public void popContentHandler() throws SAXException {
        handlerLen--;
        handlers[handlerLen]=null;  // this handler is removed
        getCurrentHandler().leaveChild(mementos[handlerLen]);
    }

    public UnmarshallingEventHandler getCurrentHandler() {
        return handlers[handlerLen-1];
    }


//
//
// text handling
//
//    
    private StringBuffer buffer = new StringBuffer();
    
    protected void consumeText( String str, boolean ignorable ) throws SAXException {
         if(ignorable && str.trim().length()==0)
            // if we are allowed to ignore text and
            // the text is ignorable, ignore.
            return;
        
        // otherwise perform a transition by this token.
        getCurrentHandler().text(str);
    }
    private void processText( boolean ignorable ) throws SAXException {
        if( shouldCollectText() )
            consumeText(buffer.toString(),ignorable);
        
        // avoid excessive object allocation, but also avoid
        // keeping a huge array inside StringBuffer.
        if(buffer.length()<1024)    buffer.setLength(0);
        else                        buffer = new StringBuffer();
    }
    
    public final void characters( char[] buf, int start, int len ) {
        if( shouldCollectText() )
            buffer.append(buf,start,len);
    }

    public final void ignorableWhitespace( char[] buf, int start, int len ) {
        characters(buf,start,len);
    }



    
//
//
// namespace binding maintainance
//
//
    private String[] nsBind = new String[16];
    private int nsLen=0;
    
    // in the current scope, nsBind[0] - nsBind[idxStack[idxStackTop]-1]
    // are active.
    // use {@link #elementDepth} and {@link stackTop} to access.
    private int[] idxStack = new int[16];
    
    public void startPrefixMapping( String prefix, String uri ) {
        if(nsBind.length==nsLen) {
            // expand the buffer
            String[] n = new String[nsLen*2];
            System.arraycopy(nsBind,0,n,0,nsLen);
            nsBind=n;
        }
        nsBind[nsLen++] = prefix;
        nsBind[nsLen++] = uri;
    }
    public void endPrefixMapping( String prefix ) {
        nsLen-=2;
    }
    public String resolveNamespacePrefix( String prefix ) {
        if(prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";
        
        for( int i=idxStack[stackTop]-2; i>=0; i-=2 ) {
            if(prefix.equals(nsBind[i]))
                return nsBind[i+1];
        }
        return null;
    }
    public String[] getNewlyDeclaredPrefixes() {
        return getPrefixList( idxStack[stackTop-1] );
    }

    public String[] getAllDeclaredPrefixes() {
        return getPrefixList( 2 );  // skip the default ""->"" mapping
    }
    
    private String[] getPrefixList( int startIndex ) {
        int size = (idxStack[stackTop]-startIndex)/2;
        String[] r = new String[size];
        for( int i=0; i<r.length; i++ )
            r[i] = nsBind[startIndex+i*2];
        return r;
    }

    
    //
    //  NamespaceContext2 implementation 
    //
    public Iterator getPrefixes(String uri) {
        // wrap it into unmodifiable list so that the remove method
        // will throw UnsupportedOperationException.
        return Collections.unmodifiableList(
            getAllPrefixesInList(uri)).iterator();
    }
    
    private List getAllPrefixesInList(String uri) {
        List a = new ArrayList();
        
        if( uri.equals(XMLConstants.XML_NS_URI) ) {
            a.add(XMLConstants.XML_NS_PREFIX);
            return a;
        }
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) ) {
            a.add(XMLConstants.XMLNS_ATTRIBUTE);
            return a;
        }
        if( uri==null )
            throw new IllegalArgumentException();
          
        for( int i=nsLen-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    a.add(nsBind[i]);
         
        return a;
    }

    public String getPrefix(String uri) {
        if( uri.equals(XMLConstants.XML_NS_URI) )
            return XMLConstants.XML_NS_PREFIX;
        if( uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI) )
            return XMLConstants.XMLNS_ATTRIBUTE;
        if( uri==null )
            throw new IllegalArgumentException();
          
        for( int i=idxStack[stackTop]-2; i>=0; i-=2 )
            if(uri.equals(nsBind[i+1]))
                if( getNamespaceURI(nsBind[i]).equals(nsBind[i+1]) )
                    // make sure that this prefix is still effective.
                    return nsBind[i];
         
        return null;
    }

     public String getNamespaceURI(String prefix) {
         if( prefix==null )
             throw new IllegalArgumentException();
         if( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) )
             return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        
         return resolveNamespacePrefix(prefix);
     }

//
//
// Attribute handling
//
//
    /**
     * Attributes stack.
     */
    private AttributesImpl[] attStack = new AttributesImpl[16];
    /**
     * Element nesting level.
     */
    private int elementDepth;
    /**
     * Always {@link #elementDepth}-1.
     */
    private int stackTop;
    
    /**
     * Stack of collectText flag.
     * False means text can be ignored for this element.
     * 
     * Use {@link #elementDepth} and {@link #stackTop} to access the array.
     */ 
    private boolean[] collectText = new boolean[16];
    
    public void pushAttributes( Attributes atts, boolean collectTextFlag ) {
        
        if( attStack.length==elementDepth ) {
            // reallocate the buffer
            AttributesImpl[] buf1 = new AttributesImpl[attStack.length*2];
            System.arraycopy(attStack,0,buf1,0,attStack.length);
            attStack = buf1;
            
            int[] buf2 = new int[idxStack.length*2];
            System.arraycopy(idxStack,0,buf2,0,idxStack.length);
            idxStack = buf2;
            
            boolean[] buf3 = new boolean[collectText.length*2];
            System.arraycopy(collectText,0,buf3,0,collectText.length);
            collectText = buf3;
        }
        
        elementDepth++;
        stackTop++;
        
        // push the stack
        AttributesImpl a = attStack[stackTop];
        if( a==null )
            attStack[stackTop] = a = new AttributesImpl();
        else
            a.clear();
        
        // since Attributes object is mutable, it is criticall important
        // to make a copy.
        // also symbolize attribute names
        for( int i=0; i<atts.getLength(); i++ ) {
            String auri = atts.getURI(i);
            String alocal = atts.getLocalName(i);
            String avalue = atts.getValue(i);
            String aqname = atts.getQName(i);
            
            // work gracefully with misconfigured parsers that don't support namespaces
            if( auri==null )
                auri="";
            if( alocal==null || alocal.length()==0 )
                alocal=aqname;
            if( aqname==null || aqname.length()==0 )
                aqname=alocal;

            // <foo xsi:nil="false">some value</foo> is a valid fragment, however
            // we need a look ahead to correctly handle this case.
            // (because when we process @xsi:nil, we don't know what the value is,
            // and by the time we read "false", we can't cancel this attribute anymore.)
            //
            // as a quick workaround, we remove @xsi:nil if the value is false.
            if( auri=="http://www.w3.org/2001/XMLSchema-instance" && alocal=="nil" ) {
                String v = avalue.trim();
                if(v.equals("false") || v.equals("0"))
                    continue;   // skip this attribute
            }
            
            // otherwise just add it.
            a.addAttribute(
                    auri,
                    alocal,
                    aqname,
                    atts.getType(i),
                    avalue );
        }
        
        
        // start a new namespace scope
        idxStack[stackTop] = nsLen;
        
        collectText[stackTop] = collectTextFlag;
    }
    public void popAttributes() {
        stackTop--;
        elementDepth--;
    }
    public Attributes getUnconsumedAttributes() {
        return attStack[stackTop];
    }
    /**
     * @param uri,local
     *      has to be interned.
     */
    public int getAttribute( String uri, String local ) {
        return attStack[stackTop].getIndexFast(uri,local);
    }
    public void consumeAttribute( int idx ) throws SAXException {
        AttributesImpl a = attStack[stackTop];
        
        String uri = a.getURI(idx);
        String local = a.getLocalName(idx);
        String qname = a.getQName(idx);
        String value = a.getValue(idx);

        // mark the attribute as consumed
        // we need to remove the attribute before we process it
        // because the event handler might access attributes.
        a.removeAttribute(idx);
        
        
        getCurrentHandler().enterAttribute(uri,local,qname);
        consumeText(value,false);
        getCurrentHandler().leaveAttribute(uri,local,qname);
    }
    public String eatAttribute( int idx ) throws SAXException {
        AttributesImpl a = attStack[stackTop];
        
        String value = a.getValue(idx);

        // mark the attribute as consumed
        a.removeAttribute(idx);
        
        return value;
    }

//
//
// ID/IDREF related code
//
//
    /**
     * Submitted patchers in the order they've submitted.
     * Many XML vocabulary doesn't use ID/IDREF at all, so we
     * initialize it with null.
     */
    private Runnable[] patchers = null;
    private int patchersLen = 0;
    
    public void addPatcher( Runnable job ) {
        // re-allocate buffer if necessary
        if( patchers==null )
            patchers = new Runnable[32];
        if( patchers.length == patchersLen ) {
            Runnable[] buf = new Runnable[patchersLen*2];
            System.arraycopy(patchers,0,buf,0,patchersLen);
            patchers = buf;
        }
        patchers[patchersLen++] = job;
    }
    
    /** Executes all the patchers. */
    private void runPatchers() {
        if( patchers!=null ) {
            for( int i=0; i<patchersLen; i++ )
                patchers[i].run();
        }
    }

    /** Records ID->Object map. */
    private Hashtable idmap = null;

    public String addToIdTable( String id ) {
        if(idmap==null)     idmap = new Hashtable();
        idmap.put( id, getCurrentHandler().owner() );
        return id;
    }
    
    public Object getObjectFromId( String id ) {
        if(idmap==null)     return null;
        return idmap.get(id);
    }
    


//
//
// Other SAX callbacks
//
//
    public void skippedEntity( String name ) {
    }
    public void processingInstruction( String target, String data ) {
        // just ignore
    }
    public void setDocumentLocator( Locator loc ) {
        locator = loc;
    }
    public Locator getLocator() { return locator; }
    
    private Locator locator = DUMMY_LOCATOR;

    private static final Locator DUMMY_LOCATOR = new LocatorImpl();


//
//
// error handling
//
//
    private final UnmarshallerImpl parent;
    private boolean aborted = false;
    
    public void handleEvent(ValidationEvent event, boolean canRecover ) throws SAXException {
        ValidationEventHandler eventHandler;
        try {
            eventHandler = parent.getEventHandler();
        } catch( JAXBException e ) {
            // impossible.
            throw new JAXBAssertionError();
        }

        boolean recover = eventHandler.handleEvent(event);
        
        // if the handler says "abort", we will not return the object
        // from the unmarshaller.getResult()
        if(!recover)    aborted = true;
        
        if( !canRecover || !recover )
            throw new SAXException( new UnmarshalException(
                event.getMessage(),
                event.getLinkedException() ) );
    }
  
//
//
// ValidationContext implementation
//
//
    public String getBaseUri() { return null; }
    public boolean isUnparsedEntity(String s) { return true; }
    public boolean isNotation(String s) { return true; }


//
//
// debug trace methods
//
//
    private Tracer tracer;
    public void setTracer( Tracer t ) {
        this.tracer = t;
    }
    public Tracer getTracer() {
        if(tracer==null)
            tracer = new Tracer.Standard();
        return tracer;
    }
    
    /**
     * Computes the names of possible root elements for a better error diagnosis.
     */
    private String computeExpectedRootElements() {
        String r = "";
        
        String[] probePoints = grammarInfo.getProbePoints();
        for( int i=0; i<probePoints.length; i+=2 ) {
            if( grammarInfo.recognize(probePoints[i],probePoints[i+1]) ) {
                if(r.length()!=0)   r+=',';
                r += "<{"+probePoints[i]+"}"+probePoints[i+1]+">";
            }
        }
        
        return r;
    }
}
