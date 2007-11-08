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

/*
 *  ValidationUtilities.java
 */

package com.sun.jbi.jsf.util;

import com.sun.jbi.jsf.bean.ArchiveBean;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;



/**
 *
 * Utilities to format XML for display on JBI JavaServer Faces pages.
 *
 **/

public final class ValidationUtilities
{
    /**
     * Controls printing of diagnostic messages to the log
     */
    private static Logger sLog = JBILogger.getInstance();


    public final static String GLASSFISH_INSTALL_ROOT = "com.sun.aas.installRoot"; //not i18n

    /*
     * Scaffolded location of jbi.xsd schema file for scaffolding testing purpose
     */
    public final static String SCAF_JBI_SCHEMA_LOC = "../generated/xml/j2ee-modules/admin-jsf" +
            "/WEB-INF/classes/com/sun/jbi/jsf/scaf/jbi.xsd";

    /*
     * Expected location of jbi.xml descriptor file in jbi archive
     */
    private final static String JBI_MANIFEST = "META-INF/jbi.xml"; // not i18n

    /**
     * XPATH expression to extract a binding-component name from a JBI Binding Component archive's /META-INF/jbi.xml
     */
    private final static String XPATH_BC_NAME =
            "/jbi/component[@type='binding-component']/identification/name";

    /**
     * XPATH expression to extract a binding-component description from a JBI Binding Component archive's /META-INF/jbi.xml
     */
    private final static String XPATH_BC_DESCR =
            "/jbi/component[@type='binding-component']/identification/description";

    /**
     * XPATH expression to extract a service-assembly name from a JBI Service Assembly archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SA_NAME =
            "/jbi/service-assembly/identification/name";

    /**
     * XPATH expression to extract a service-assembly description from a JBI Service Assembly archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SA_DESCR =
            "/jbi/service-assembly/identification/description";

    /**
     * XPATH expression to extract a service-engine name from a JBI Service Engine archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SE_NAME =
            "/jbi/component[@type='service-engine']/identification/name";

    /**
     * XPATH expression to extract a service-engine description from a JBI Service Engine archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SE_DESCR =
            "/jbi/component[@type='service-engine']/identification/description";

    /**
     * XPATH expression to extract a shared-library name from a JBI Shared Library archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SL_NAME =
            "/jbi/shared-library/identification/name";

    /**
     * XPATH expression to extract a shared-library description from a JBI Shared Library archive's /META-INF/jbi.xml
     */
    private final static String XPATH_SL_DESCR =
            "/jbi/shared-library/identification/description";

    public static Document getJbiDocument()
    {
        Document doc ;
        try
        {
            DocumentBuilderFactory docBuilderFactory =
                    DocumentBuilderFactory.newInstance ();
            docBuilderFactory.setNamespaceAware (false);
            docBuilderFactory.setValidating (false);
            docBuilderFactory.setExpandEntityReferences (false);
            DocumentBuilder docBuilder =
                    docBuilderFactory.newDocumentBuilder ();
            doc = docBuilder.parse (getMetaDataEntry ());
        }
        catch (javax.xml.parsers.ParserConfigurationException pcEx)
        {
            sLog.fine("getJbiDocument(), caught pcEx.getMessage()=" + pcEx.getMessage ());
            doc = null;
        }
        catch (org.xml.sax.SAXException saXex)
        {
            sLog.fine("getJbiDocument(), caught saXex.getMessage()=" + saXex.getMessage ());
            doc = null;
        }
        catch (java.io.IOException ioEx)
        {
            sLog.fine("getJbiDocument(), caught ioEx.getMessage()=" + ioEx.getMessage ());
            doc = null;
        }
        return doc;
    }



    /**
     * determines the JBI Type for a given JBI metadata descriptor (jbi.xml)
     * @param aJbiDescriptor an XML document representing a valid jbi.xml file
     * @return the JBI type <code> binding-component, service-assembly, service-engine, or shared-library</code>
     */
    public static String getJbiType (Document aJbiDescriptor)
    {
        String result = null;

    if (null != aJbiDescriptor)
        {
        if (tryXpathExpInDescriptor(XPATH_BC_NAME, XPATH_BC_DESCR, aJbiDescriptor))
            {
            result = JBIConstants.JBI_BINDING_COMPONENT_TYPE;
            }
        else if (tryXpathExpInDescriptor(XPATH_SA_NAME, XPATH_SA_DESCR, aJbiDescriptor))
            {
            result = JBIConstants.JBI_SERVICE_ASSEMBLY_TYPE;
            }
        else if (tryXpathExpInDescriptor(XPATH_SE_NAME, XPATH_SE_DESCR, aJbiDescriptor))
            {
            result = JBIConstants.JBI_SERVICE_ENGINE_TYPE;
            }
        else if (tryXpathExpInDescriptor(XPATH_SL_NAME, XPATH_SL_DESCR, aJbiDescriptor))
            {
            result = JBIConstants.JBI_SHARED_LIBRARY_TYPE;
            }
        else
            {
            sLog.fine("ValidationUtilities.getJbiType() " +
                       "cannot find an expected type in supplied descriptor");
            //throw new IllegalArgumentException(); // unsupported type of JBI descriptor
            return  "unknown jbi  type"; //not i18n
            }
        }

    sLog.fine("ValidationUtilities.getJbiType(), result=" + result);
    return result;
    }

    /**
     * determines if a given XPATH expression is found in a given JBI descriptor.
     * @param aJbiDescriptor
     * @return true if the XPATH expression is found, else false
     */
    private static boolean tryXpathExpInDescriptor(String anXpathNameExpression,
                                            String anXpathDescExpression,
                        Document aJbiDescriptor)
    {
    boolean result = false;

        ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
    sLog.fine("ValidationUtilities.tryXpathExpInDescriptor(" + anXpathNameExpression + ", "+ anXpathDescExpression +"," + aJbiDescriptor + ")");

    try
        {
        NodeList nodeList =
            XPathAPI.selectNodeList (aJbiDescriptor,
                         anXpathNameExpression);

        if (0 != nodeList.getLength ())
            {
                        NodeList descNodeList = XPathAPI.selectNodeList (aJbiDescriptor,
                                                                        anXpathDescExpression);
                        if ((null != descNodeList.item(0))
                            &&(null != descNodeList.item(0).getFirstChild()))
                        {
                            archiveBean.setDescription (descNodeList.item (0).getFirstChild ().getNodeValue ());
                        }
                        else
                        {
                            archiveBean.setDescription(""); // none provided (BT CR 6529558)
                        }
                        if ((null != nodeList.item(0))
                            &&(null != nodeList.item(0).getFirstChild()))
                        {
                            archiveBean.setJbiName (nodeList.item (0).getFirstChild().getNodeValue ());
                            result = true;
                        }
                        else
                        {
                            // result = false; // required name missing
                        }
            }
        }
    catch (Exception ex)
        {
        // TBD use logging warning
        sLog.fine("getType caught ex=" + ex);
        ex.printStackTrace(System.err);
        }

    sLog.fine("ValidationUtilities.tryXpathExpInDescriptor(...), result=" + result);
    return result;
    }


   /**
     * Get the jbi.xml file from given jar/zip file if present.
     * @param zipEntry is the first entry of ZipArchive (guarantees not an empty archive)
     * @return result InputStream containing jbi.xml data, or empty stream if no jbi.xml
     */
    public static InputStream getMetaDataEntry ()
    {
        ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
        InputStream result =
                new ByteArrayInputStream (new byte[0]);
        long available = 0;
        int count = 0;
        String name = "";
        ZipEntry zipEntry ;

        String filePath = archiveBean.getArchiveAbsolutePath ();
        // TBD log.fine("getMetaDataEntry(), <replace with info> filePath=" + filePath);
        ZipInputStream zipFile = null;

        if (null != filePath)
        {
            try
            {
                zipFile = new ZipInputStream (new FileInputStream (filePath));

                if ( null != zipFile )
                {
                    zipEntry = zipFile.getNextEntry ();
                    if (null != zipEntry)
                    {
                        name = zipEntry.getName ();
                    }
                    while( null != zipEntry && (! name.equals (JBI_MANIFEST)))
                    {
                        ++count;
                        zipEntry = zipFile.getNextEntry ();
                        if (zipEntry!=null)
                        {
                            name = zipEntry.getName ();
                        }
                    }
                }
                //zipArchive has jbi.xml
                if (name.equals (JBI_MANIFEST))
                {
                    ByteArrayOutputStream baos =
                            new ByteArrayOutputStream ();
                    byte[] buffer = new byte[4096];
                    int len = zipFile.read (buffer);
                    while (0 < len)
                    {
                        baos.write (buffer, 0, len);
                        len = zipFile.read (buffer);
                    }
                    result =
                            new ByteArrayInputStream (baos.toByteArray ());
                    available = result.available ();
                }
                else
                {
                    archiveBean.setHasJbiXml (false);
                }
            }
            catch (ZipException zex)
            {
                // TBD log.fine("getMetaDataEntry caught zex=" + zex);
                archiveBean.setZipFileReadError (true);
            }
            catch (FileNotFoundException fnfex)
            {
                // TBD log.fine("getMetaDataEntry caught fnfex=" + fnfex);
                archiveBean.setFileReadError (true);
            }
            catch (IOException ioex)
            {
                // TBD log.fine("getMetaDataEntry caught ioex=" + ioex);
                archiveBean.setFileReadError (true);
            }
            finally
            {
                close(zipFile);
            }
        }
        else
        {
            archiveBean.setFileReadError (true); // no file specified
        }
        return result;
    }



/**
 * Check zip file entry.
 * @return true iff the zipfile is empty
 */
public static boolean isArchiveEmptyOrInValid ()
{
    ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
    boolean result = false; // assume zip file has some entries

    String filePath = archiveBean.getArchiveAbsolutePath ();
    // TBD log.fine("isArchiveEmptyOrInValid(), filePath=" + filePath);

    ZipInputStream zipArchive = null;
    if (null != filePath)
    {
        try
        {
            zipArchive
                    = new ZipInputStream
                    ( new FileInputStream ( filePath ) );
            boolean done = false;
            boolean empty = true;
            while (!done)
            {
                ZipEntry entry = zipArchive.getNextEntry ();

                if (null != entry)
                {
                    empty = false;
                    // TBD log.fine("reading: " + entry.getName());
                }
                else
                {
                    done = true;
                }
            }
            if ( empty )
            {
                // TBD log.fine("isArchiveEmptyOrInValid() empty");
                result =  true;
            }
            else
            {
                // TBD log.fine("isArchiveEmptyOrInValid() not empty");
            }
        }
        catch (ZipException zex)  // not a valid archive
        {
            archiveBean.setZipFileReadError (true);
            sLog.fine("isArchiveEmptyOrInValid() bad archive, zex=" + zex);
            result =  true;
        }
        catch (IOException ioex) // not a valid file
        {
            archiveBean.setFileReadError (true);
            sLog.fine("isArchiveEmptyOrInValid() bad file, ioex=" + ioex);
            result = true;
        }
        finally
        {
            close(zipArchive);
        }
    }
    else // no file specified
    {
        sLog.fine("isArchiveEmptyOrInValid() no file");
        archiveBean.setFileReadError (true);
        result = true;
    }


    // TBD log.fine("isArchiveEmptyOrInValid(), filePath=" + filePath + ", result=" + result);
    return result;
}

  /**
 * Validate the jbi.xml file for well-formedness.
 * @param istr is the InputStream to the "jbi.xml" file
 * @return true iff the file passes validation
 */
public static boolean isJbiXmlWellformed ()
{
    // TBD log.fine("isJbiXmlWellformed()");
    boolean result = false;
    try
    {
        DOMParser parser = new DOMParser ();
        InputStream istr = getMetaDataEntry ();
        InputSource inSrc = new InputSource (istr);
        sLog.fine ("Created InputSOurce");
        parser.parse (inSrc);
        istr.close ();
        result = true;
    }
    catch (SAXParseException spe)
    {
        sLog.fine("isJbiXmlWellformed(), caught spe=" + spe);
        result = false;
    }
    catch (Exception e)
    {
        sLog.fine("isJbiXmlWellformed(), caught e=" + e);
        result = false;
    }

    // TBD log.fine("isJbiXmlWellformed(), result=" + result);
    return result;
}

/**
* Validate the jbi.xml file against jbi schema.
* @return true if the file is schema valid
*/
public static boolean isJbiXmlSchemaValid ()
{
boolean result = false;
String runtimeSchema ;
Schema schema;
Validator validator;
// TBD log.fine("isJbiXmlSchemaValid()");


try
{
    DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance ();
    DocumentBuilder docBuilder =
            docBuilderFactory.newDocumentBuilder ();

    SchemaFactory factory =
            SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try
    {
        runtimeSchema = System.getProperty (GLASSFISH_INSTALL_ROOT) + File.separator +
                "jbi" + File.separator +
                "schemas" + File.separator +
                "jbi.xsd";
        schema = factory.newSchema (new File (runtimeSchema));
        // TBD log.fine("using JBI runtime schema=" + schema);
    }
    catch (Exception fnfEx)
    {
        schema = factory.newSchema (new File (SCAF_JBI_SCHEMA_LOC));
        sLog.fine("using scaffolded JBI schema=" + schema);
    }

    docBuilderFactory.setNamespaceAware (true);
    validator = schema.newValidator ();

    InputStream istr= getMetaDataEntry ();
    Document doc = docBuilder.parse (istr);
    validator.validate (new DOMSource (doc));
    istr.close ();
    // TBD log.fine("valid");
    result = true;
}
catch (org.xml.sax.SAXParseException spe)
{
    sLog.fine("isJbiXmlSchemaValid(), caught spe=" + spe);
    result = false;
}
catch (Exception e)
{
    sLog.fine("isJbiXmlSchemaValid(), caught e=" + e);
    result = false;
}
// TBD log.fine("isJbiXmlSchemaValid(), result=" + result);
return result;
}

private static void close( InputStream is ) {
  if ( is != null ) {
    try {
      is.close();
    }
    catch ( IOException ioe ) {
      sLog.fine("IO Exception error in closing the zip archive" + ioe.getMessage());
    }
  }
}

}
