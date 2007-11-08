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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
   This class implements the final transformation in the multi-step
   Schematron validation process. It is assumed that the user knows
   something about Schematron.

   Schematron instances are not thread-safe. Each instance can only be
   used in a single thread, but each instance can be used multiple
   times in sequence.

   We use the default Transformer, as per the
   javax.xml.transform.TransformFactory class. It is important that
   the transformer be compatable with the stylesheet being used, but
   apart from that this class has no constraints on these objects. It
   simply provides a convenient wrapper for obtaining a validator.
*/

class Schematron
{
    private Transformer transformer;

      /**
       * Construct an instance which will use the given resource name
       * to find the schema to be used for subsequent analyses. The
       * schemaName is looked up as a resource on the classpath using
       * {@link Class.getResourceAsStream(schemaName)}.
       * @param schemaName the name of the schema to be used
       * @throws TransformerConfigurationException if the
       * corresponding resource could not be found, or if the
       * transformer could not be constructed.
       * @throws TransformerFactoryConfigurationError if the
       * underlying transformer factory could not be instantiated.
       */
    Schematron(final String schemaName)  throws TransformerConfigurationException {
        transformer = getTransformer(schemaName);
    }
        
      /**
       *  Analyse the given source onto the given result, using the
       *  given schematron stylesheet, using XSLT transformations.
       *  @param src the XML source which is to be analyzed
       *  @param result the result onto which the analsis will be
       *  placed
       *  @throws TransformerException if there was a problem with
       *  the transformation.
       */
    void analyze(final Source src, final Result result) throws TransformerException{
        transformer.transform(src, result);
    }
    
    private Transformer getTransformer(final String schema) throws TransformerConfigurationException {
        final InputStream is = this.getClass().getResourceAsStream(schema);
        if (null == is){
            throw new TransformerConfigurationException("Couldn't construct a transformer to perform validation because I couldn't find the resource named \""+schema+"\" from "+this.getClass().getName());
        }
        final TransformerFactory f = TransformerFactory.newInstance();

        final Transformer t = f.newTransformer(new StreamSource(is));

        return t;
    }

}

