/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.parser;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import javax.persistence.spi.PersistenceUnitTransactionType;

import oracle.toplink.essentials.ejb.cmp3.persistence.SEPersistenceUnitInfo;
import oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base.DataSourceImpl;
import oracle.toplink.essentials.logging.AbstractSessionLog;

public class PersistenceContentHandler implements ContentHandler {
    private static final String NAMESPACE_URI = "http://java.sun.com/xml/ns/persistence";
    private static final String ELEMENT_PERSISTENCE_UNIT = "persistence-unit";
    private static final String ELEMENT_PROVIDER = "provider";
    private static final String ELEMENT_JTA_DATA_SOURCE = "jta-data-source";
    private static final String ELEMENT_NON_JTA_DATA_SOURCE = "non-jta-data-source";
    private static final String ELEMENT_MAPPING_FILE = "mapping-file";
    private static final String ELEMENT_JAR_FILE = "jar-file";
    private static final String ELEMENT_CLASS = "class";
    private static final String ELEMENT_EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";  
    private static final String ATTRIBUTE_TRANSACTION_TYPE = "transaction-type";  

    private SEPersistenceUnitInfo persistenceUnitInfo;
    private Vector<SEPersistenceUnitInfo> persistenceUnits;
    private StringBuffer stringBuffer;
    private boolean readCharacters = false;

    public PersistenceContentHandler() {
        super();
        stringBuffer = new StringBuffer();
        persistenceUnits = new Vector();
    }

   public Vector<SEPersistenceUnitInfo> getPersistenceUnits() {
        return persistenceUnits;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (NAMESPACE_URI.equals(namespaceURI)) {
            if (ELEMENT_PERSISTENCE_UNIT.equals(localName)) {
                persistenceUnitInfo = new SEPersistenceUnitInfo();
                persistenceUnitInfo.setPersistenceUnitName(atts.getValue(ATTRIBUTE_NAME));
                String transactionType = atts.getValue(ATTRIBUTE_TRANSACTION_TYPE);
                if(transactionType != null) {
                    persistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.valueOf(transactionType));
                }
                return;
            } else if (ELEMENT_PROPERTY.equals(localName)) {
                String name = atts.getValue(ATTRIBUTE_NAME);
                String value = atts.getValue(ATTRIBUTE_VALUE);
                persistenceUnitInfo.getProperties().setProperty(name, value);
            } else if (ELEMENT_PROVIDER.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_JTA_DATA_SOURCE.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_NON_JTA_DATA_SOURCE.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_MAPPING_FILE.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_JAR_FILE.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_EXCLUDE_UNLISTED_CLASSES.equals(localName)) {
                readCharacters = true;
                return;
            } else if (ELEMENT_CLASS.equals(localName)) {
                readCharacters = true;
                return;
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        String string = stringBuffer.toString().trim();
        stringBuffer.delete(0, stringBuffer.length());
        readCharacters = false;

        if (NAMESPACE_URI.equals(namespaceURI)) {
            if (ELEMENT_PROVIDER.equals(localName)) {
                persistenceUnitInfo.setPersistenceProviderClassName(string);
                return;
            } else if (ELEMENT_JTA_DATA_SOURCE.equals(localName)) {
                persistenceUnitInfo.setJtaDataSource(
                    // Create a dummy DataSource that will 
                    // throw an exception on access
                    new DataSourceImpl(string, null, null, null));
                return;
            } else if (ELEMENT_NON_JTA_DATA_SOURCE.equals(localName)) {
                persistenceUnitInfo.setNonJtaDataSource(
                    // Create a dummy DataSource that will 
                    // throw an exception on access
                    new DataSourceImpl(string, null, null, null));
                return;
            } else if (ELEMENT_MAPPING_FILE.equals(localName)) {
                persistenceUnitInfo.getMappingFileNames().add(string);
                return;
            } else if (ELEMENT_JAR_FILE.equals(localName)) {
                persistenceUnitInfo.getJarFiles().add(string);
                return;
            } else if (ELEMENT_CLASS.equals(localName)) {
                persistenceUnitInfo.getManagedClassNames().add(string);
                return;
            } else if (ELEMENT_EXCLUDE_UNLISTED_CLASSES.equals(localName)) {
                if (string.equals("true") || string.equals("1")){
                    persistenceUnitInfo.setExcludeUnlistedClasses(true);
                } else {
                    persistenceUnitInfo.setExcludeUnlistedClasses(false);
                }
                return;
            } else if (ELEMENT_PERSISTENCE_UNIT.equals(localName)) {
                if (persistenceUnitInfo != null){
                    persistenceUnits.add(persistenceUnitInfo);
                    persistenceUnitInfo = null;
                }
            } 
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (readCharacters) {
            stringBuffer.append(ch, start, length);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
