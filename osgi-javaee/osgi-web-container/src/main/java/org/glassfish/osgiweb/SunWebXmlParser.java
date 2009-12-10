/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgiweb;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.*;
import java.io.InputStream;

/**
 * A mini parser to parse sun-web.xml for entries of interest to us.
 * Currently, we only read context-root value.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class SunWebXmlParser
{
    private static XMLInputFactory xmlIf = null;

    static {
        xmlIf = XMLInputFactory.newInstance();
        xmlIf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    String contextRoot;

    /**
     * The caller should close the input stream.
     * @param in InputStream for sun-web.xml
     */
    SunWebXmlParser(InputStream in) throws XMLStreamException
    {
        XMLStreamReader reader = xmlIf.createXMLStreamReader(in);
        try {
            int event;
            while (reader.hasNext() && (event = reader.next()) != END_DOCUMENT) {
                if (event == START_ELEMENT) {
                    String element = reader.getLocalName();
                    if (element.equals("context-root")) {
                        contextRoot = reader.getElementText();
                        break;
                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    public String getContextRoot()
    {
        return contextRoot;
    }
}
