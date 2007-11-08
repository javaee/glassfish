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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import java.io.InputStream;
import org.xml.sax.EntityResolver;

import java.io.OutputStreamWriter;



class RNGValidator
{
    void validate(InputSource schema, InputSource src, Writer w) throws VerifierConfigurationException, SAXException, IOException{
         validate(schema, src, new MyErrorHandler(w));
    }

    void validate(InputSource schema, InputSource src, XMLReader rdr, Writer w) throws VerifierConfigurationException, SAXException, IOException{
         validate(schema, src, rdr,  new MyErrorHandler(w) );
    }


    void validate(InputSource schema, InputSource src,  ErrorHandler eh) throws VerifierConfigurationException, SAXException, IOException{
        getVerifier(schema, eh).verify(src);
    }

        /**
         * Validate the given source against the given schema, using
         * the given reader to parser the source, handling errors
         * using the given {@link ErrorHandler}
         * @param schema the RELAX NG schema to be used to validate
         * the source document
         * @param src the source document to be validated
         * @param rdr the reader used to parse the source document
         * @param errorHandler used to handle errors during the parse
         * and validation
         * @throws VerifierConfigurationException if the verifier
         * cannot be configured
         * @throws SAXException if there's a fatal exception from the
         * SAX layer
         * @throws IOException if the IO fails
         */
    void validate(InputSource schema, InputSource src, XMLReader rdr, ErrorHandler errorHandler) throws VerifierConfigurationException, SAXException, IOException{
        rdr.setContentHandler(getVerifier(schema, rdr.getEntityResolver(), errorHandler).getVerifierHandler());
        rdr.parse(src);
    }

    private Verifier getVerifier(InputSource schema, ErrorHandler eh)
        throws VerifierConfigurationException, SAXException, IOException {
        return getVerifier(schema, null, eh);
    }
    private Verifier getVerifier(InputSource schema, EntityResolver er, ErrorHandler eh)
        throws VerifierConfigurationException, SAXException, IOException {
            // As of the 20030108 release of jarv this method doesn't
            // work. The new release added a class loader and that
            // seems to screw things up.
//         final VerifierFactory f =
//         VerifierFactory.newInstance("http://relaxng.org/ns/structure/1.0");
        final VerifierFactory f = new com.sun.msv.verifier.jarv.TheFactoryImpl();
        final Verifier verifier = f.newVerifier(schema);
        verifier.setErrorHandler(eh);
        verifier.setEntityResolver(er);
        return verifier;
    }
    

    private static class MyErrorHandler implements ErrorHandler
    {
        private PrintWriter out;
        MyErrorHandler(Writer out) {
            this.out = new PrintWriter(out);
        }

          /**
           * Returns a string describing parse exception details
           */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
            " Line=" + spe.getLineNumber() +
            ": " + spe.getMessage();
            return info;
        }

          // The following methods are standard SAX ErrorHandler methods.
          // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            out.println(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }


}
