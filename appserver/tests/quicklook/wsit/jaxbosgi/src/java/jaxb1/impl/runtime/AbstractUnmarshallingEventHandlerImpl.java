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

import java.util.StringTokenizer;

import javax.xml.bind.Element;
import javax.xml.bind.ParseConversionEvent;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.helpers.ParseConversionEventImpl;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.bind.helpers.ValidationEventLocatorImpl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.unmarshaller.Messages;

/**
 * Convenient default implementation of
 * {@link UnmarshallingEventHandler}
 * to minimize code generation.
 * 
 * <p>
 * For historical reasons, sometimes this type is used where
 * {@link UnmarshallingEventHandler} should be used.   
 * 
 * Once an exception is in the form of UnmarshalException, we consider
 * it to be already reported to the client app.
 */
public abstract class AbstractUnmarshallingEventHandlerImpl implements UnmarshallingEventHandler
{
    public AbstractUnmarshallingEventHandlerImpl(UnmarshallingContext _ctxt,
        String _stateTextTypes ) {
        
        this.context = _ctxt;
        this.stateTextTypes = _stateTextTypes;
    }
    public final UnmarshallingContext context;
    
    /**
     * Text type of states encoded into a string.
     * 'L' means a list state.
     */
    private final String stateTextTypes;
    
//
//
// methods that will be provided by the generated code.
//
//    
    // internal events
    public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        unexpectedEnterElement(uri,local,qname,atts);
    }
    public void leaveElement(String uri, String local, String qname) throws SAXException {
        unexpectedLeaveElement(uri,local,qname);
    }
    public final void text(String text) throws SAXException {
        if(isListState()) {
            // in list state, we don't need to care about whitespaces.
            // if the text is all whitespace, this won't generate a text event,
            // so it would be just fine.
            
            StringTokenizer tokens = new StringTokenizer(text);
            if( tokens.countTokens()==1 ) {
                handleText(text);
            } else {
                while(tokens.hasMoreTokens())
                    // the handler can be switched during the text processing,
                    // so the current handler has to be obtained inside the loop
                    context.getCurrentHandler().text(tokens.nextToken());
            }
        } else {
            // otherwise process this token
            handleText(text);
        }
    }
    protected void handleText(String s) throws SAXException {
        unexpectedText(s);
    }
    public void enterAttribute(String uri, String local, String qname) throws SAXException {
        unexpectedEnterAttribute(uri,local,qname);
    }
    public void leaveAttribute(String uri, String local, String qname) throws SAXException {
        unexpectedLeaveAttribute(uri,local,qname);
    }
    public void leaveChild(int nextState) throws SAXException {
        this.state = nextState;
    }
    
    
    /**
     * Checks if the current state is marked as a list state.
     */
    protected final boolean isListState() {
        return stateTextTypes.charAt(state)=='L';
    }
    
    
    /** Current state of this automaton. */
    public int state;
    
    
    
    
//
//
// utility methods
//
//
    /** Called when a RuntimeException is thrown during unmarshalling a text. */
    protected void handleUnexpectedTextException( String text, RuntimeException e ) throws SAXException {
        // report this as an error
        reportError( Messages.format(Messages.UNEXPECTED_TEXT,text), e, true );
    }
    
    /**
     * Last resort when something goes terribly wrong within the unmarshaller.
     */
    protected void handleGenericException( Exception e ) throws SAXException {
        reportError( e.getMessage(), e, false );
    }
    
    
    protected final void dump() {
        System.err.println("state is :"+state);
    }
    private void reportError( String msg, boolean canRecover ) throws SAXException {
        reportError( msg, null, canRecover );
    }
    private void reportError( String msg, Exception nested, boolean canRecover ) throws SAXException {
        context.handleEvent( new ValidationEventImpl(
            canRecover? ValidationEvent.ERROR : ValidationEvent.FATAL_ERROR,
            msg, 
            new ValidationEventLocatorImpl(context.getLocator()),
            nested ), canRecover );
    }
    protected final void unexpectedEnterElement( String uri, String local, String qname, Attributes atts ) throws SAXException {
        // notify the error
        reportError( Messages.format(Messages.UNEXPECTED_ENTER_ELEMENT, uri, local ), true );
        // then recover by ignoring the whole element.
        context.pushContentHandler(new Discarder(context),state);
        context.getCurrentHandler().enterElement(uri,local,qname,atts);
    }
    protected final void unexpectedLeaveElement( String uri, String local, String qname ) throws SAXException {
        reportError( Messages.format(Messages.UNEXPECTED_LEAVE_ELEMENT, uri, local ), false );
    }
    protected final void unexpectedEnterAttribute( String uri, String local, String qname ) throws SAXException {
        reportError( Messages.format(Messages.UNEXPECTED_ENTER_ATTRIBUTE, uri, local ), false );
    }
    protected final void unexpectedLeaveAttribute( String uri, String local, String qname ) throws SAXException {
        reportError( Messages.format(Messages.UNEXPECTED_LEAVE_ATTRIBUTE, uri, local ), false );
    }
    protected final void unexpectedText( String str ) throws SAXException {
        // make str printable
        str = str.replace('\r',' ').replace('\n',' ').replace('\t',' ').trim();
        
        reportError( Messages.format(Messages.UNEXPECTED_TEXT, str ), true );
    }
    protected final void unexpectedLeaveChild() throws SAXException {
        // I believe this is really a bug of the compiler,
        // since when an object spawns a child object, it must be "prepared"
        // to receive this event.
        dump();
        throw new JAXBAssertionError( 
            Messages.format( Messages.UNEXPECTED_LEAVE_CHILD ) );
    }
    /**
     * This method is called by the generated derived class
     * when a datatype parse method throws an exception.
     */
    protected void handleParseConversionException(Exception e) throws SAXException {
        if( e instanceof RuntimeException )
            throw (RuntimeException)e;  // don't catch the runtime exception. just let it go.
        
        // wrap it into a ParseConversionEvent and report it
        ParseConversionEvent pce = new ParseConversionEventImpl(
            ValidationEvent.ERROR, e.getMessage(), 
            new ValidationEventLocatorImpl(context.getLocator()), e );
        context.handleEvent(pce,true);
    }
    
    
//
//    
// spawn a new child object
//
//    
    private UnmarshallingEventHandler spawnChild( Class clazz, int memento ) {
        
        UnmarshallableObject child;
        try {
            child = (UnmarshallableObject)clazz.newInstance();
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
        
        UnmarshallingEventHandler handler = child.createUnmarshaller(context);
        context.pushContentHandler(handler,memento);
        return handler;
    }
    
    protected final Object spawnChildFromEnterElement(Class clazz, int memento, String uri, String local, String qname, Attributes atts)
            throws SAXException {
        UnmarshallingEventHandler ueh = spawnChild(clazz,memento);
        ueh.enterElement(uri,local,qname,atts);
        return ueh.owner();
    }
    
    protected final Object spawnChildFromEnterAttribute(Class clazz, int memento, String uri, String local, String qname)
            throws SAXException {
        UnmarshallingEventHandler ueh = spawnChild(clazz,memento);
        ueh.enterAttribute(uri,local,qname);
        return ueh.owner();
    }
    
    protected final Object spawnChildFromText(Class clazz, int memento, String value)
            throws SAXException {
        UnmarshallingEventHandler ueh = spawnChild(clazz,memento);
        ueh.text(value);
        return ueh.owner();
    }

    // these methods can be used if a child object can be nullable
    protected final Object spawnChildFromLeaveElement(Class clazz, int memento, String uri, String local, String qname)
            throws SAXException {
        UnmarshallingEventHandler ueh = spawnChild(clazz,memento);
        ueh.leaveElement(uri,local,qname);
        return ueh.owner();
    }

    protected final Object spawnChildFromLeaveAttribute(Class clazz, int memento, String uri, String local, String qname)
            throws SAXException {
        UnmarshallingEventHandler ueh = spawnChild(clazz,memento);
        ueh.leaveAttribute(uri,local,qname);
        return ueh.owner();
    }
    
    protected final Element spawnWildcard( int memento, String uri, String local, String qname, Attributes atts )
            throws SAXException {
        UnmarshallingEventHandler ueh = context.getGrammarInfo().createUnmarshaller(uri,local,context);
        
        if(ueh!=null) {
            context.pushContentHandler(ueh,memento);
            ueh.enterElement(uri,local,qname,atts);
            return (Element)ueh.owner();
        } else {
            // if no class is available to unmarshal this element, discard
            // the sub-tree by feeding events to discarder.
            context.pushContentHandler( new Discarder(context), memento );
            context.getCurrentHandler().enterElement(uri,local,qname,atts);
            return null;    // return null so that the discarder will be ignored
        }
    }
    
//
//    
// spawn a new child handler.
//      used for super class and RELAXNG interleave handling. 
//    

    
    protected final void spawnHandlerFromEnterElement(
        UnmarshallingEventHandler unm, int memento, String uri, String local, String qname, Attributes atts )
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.enterElement(uri,local,qname,atts);
    }
    
    protected final void spawnHandlerFromEnterAttribute(
        UnmarshallingEventHandler unm, int memento, String uri, String local, String qname)
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.enterAttribute(uri,local,qname);
    }
    
    protected final void spawnHandlerFromFromText(
        UnmarshallingEventHandler unm, int memento, String value)
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.text(value);
    }
    
    protected final void spawnHandlerFromLeaveElement(
        UnmarshallingEventHandler unm, int memento, String uri, String local, String qname)
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.leaveElement(uri,local,qname);
    }
    
    protected final void spawnHandlerFromLeaveAttribute(
        UnmarshallingEventHandler unm, int memento, String uri, String local, String qname)
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.leaveAttribute(uri,local,qname);
    }
    
    protected final void spawnHandlerFromText(
        UnmarshallingEventHandler unm, int memento, String text )
            throws SAXException {
        
        context.pushContentHandler(unm,memento);
        unm.text(text);
    }
    
    
//
//    
// revert to parent
//
//    
    protected final void revertToParentFromEnterElement(String uri,String local, String qname,Attributes atts)
            throws SAXException {
        context.popContentHandler();
        context.getCurrentHandler().enterElement(uri,local,qname,atts);
    }
    protected final void revertToParentFromLeaveElement(String uri,String local, String qname)
            throws SAXException {
        context.popContentHandler();
        context.getCurrentHandler().leaveElement(uri,local,qname);
    }
    protected final void revertToParentFromEnterAttribute(String uri,String local, String qname)
            throws SAXException {
        context.popContentHandler();
        context.getCurrentHandler().enterAttribute(uri,local,qname);
    }
    protected final void revertToParentFromLeaveAttribute(String uri,String local, String qname)
            throws SAXException {
        context.popContentHandler();
        context.getCurrentHandler().leaveAttribute(uri,local,qname);
    }
    protected final void revertToParentFromText(String value)
            throws SAXException {
        context.popContentHandler();
        context.getCurrentHandler().text(value);
    }
}
