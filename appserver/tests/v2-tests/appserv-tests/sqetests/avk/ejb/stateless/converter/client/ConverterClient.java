/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1peqe.ejb.stateless.converter.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.ejb.stateless.converter.ejb.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import org.apache.xpath.*;

/**
 * A simple java client. This uses the services provided by the <code>ConverterBean</code> and
 * converts 100 US dollars to Yen and 100 Yen to Euro. 
 * <p>In this regard, it does the following in order
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 * <br>
 * <b>Locating the home interface:</b>
 * <blockquote><pre>
 *	Context initial = new InitialContext();
 *	Context myEnv = (Context)initial.lookup("java:comp/env");
 *	Object objref = myEnv.lookup("ejb/SimpleConverter");
 *  ConverterHome home = (ConverterHome)PortableRemoteObject.narrow(objref, ConverterHome.class);
 * </pre></blockquote>
 * <br>
 * <b>Creating the remote interface:</b>
 * <blockquote><pre>
 *	Converter currencyConverter = home.create();
 * </pre></blockquote>
 * <br>
 * <b>Invoking business methods:</b>
 * <blockquote><pre>
 *  BigDecimal param = new BigDecimal ("100.00");
 *	amount = currencyConverter.dollarToYen(param);
 *  amount = currencyConverter.yenToEuro(param);
 * </pre></blockquote>
 * <br>
 * <b>Output:</b>
 * <pre>
 * 12160.00
 * 0.77
 * </pre>
 *
 *
 */

public class ConverterClient {

    private SimpleReporterAdapter stat = 
            new SimpleReporterAdapter("appserv-tests");

    ConverterClient() {
    }

   /**
    * The main method of the client. This invokes the <code>ConverterBean</code> to use
    * its services. It then asks the bean to convert 100 dollars to yen and
    * 100 yen to euro. The results are printed at the terminal where the client is run.
    * See <code>appclient</code> documentation in SunONE app server to run the clinet.
    *
    */
    public static void main(String[] args) {
	ConverterClient client = new ConverterClient();
	client.run(args);
    }

    private void run(String[] args) {
	String testId = null;
        String xmlfile = args[0];

        try {
            stat.addDescription("Verify converter sample with avk");
            testId = "AVK- Converter::Sample AppClient";
            if ( validArchive( xmlfile)) {
               System.out.println("Static Check returned 0 - good!");
               stat.addStatus(testId, stat.PASS);
            } else {
               stat.addStatus(testId, stat.FAIL);
               System.out.println("Static Check returned non zero - not good!");
            }
        } catch (Exception ex) {
            stat.addStatus(testId, stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } finally {
	    stat.printSummary(testId);
        }
    }

    private boolean validReport(String verifierhome) throws Exception{
    String search  = "app-verification/ejb-percentage";
       if (verifierhome == null ) return false;
       // check the xml results file generated by running AVK StaticCheck
       System.out.println("verifierhome is " + verifierhome);
       // javke.home/reporttool/resultS
       String xmlfile = verifierhome +
                File.separator + "reporttool" + File.separator + "results" +
                File.separator + "result.xml";
       System.out.println("file path is " + xmlfile);
       File xmlFile = new File( xmlfile );
       boolean passed = false;
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       try {
            System.out.println(" get db");
            DocumentBuilder db = dbf.newDocumentBuilder();
            System.out.println(" parse ");
            Document doc = db.parse(xmlFile);
            System.out.println(" get root ");
            Element root    = doc.getDocumentElement();
            System.out.println( "The failed count is: " + findValue( root, search)); 
            if ( findValue( root, search).equals("100"))
                 passed = true;
        } catch (org.xml.sax.SAXException se) {
               throw new Exception(se);
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
               throw new Exception(pce.toString());
        } catch (Exception e) {
               throw new Exception(e.toString());
        }
    return passed;
    }

    private boolean validArchive( String xmlfile) throws Exception{

       // check the xml results file generated by running AVK StaticCheck 
       System.out.println("xmlfile is " +  xmlfile );
       // results of test is in current working directory.
       // String xmlfile = "ejb-stateless-converterApp.ear.xml";

       boolean passed = false;
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       String good = "<failure-number>0</failure-number>";
       try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            // The next method requires /sun/appserver8/lib/dtds/static-verification_1_4.dtd
            // static-verification_1_4.dtd needs to be moved to that location for method to succeed.
            //
            //     [exec] java.lang.Exception: java.io.FileNotFoundException: 
            //     /sun/appserver8/lib/dtds/static-verification_1_4.dtd
            //     (No such file or directory)

            Document doc = db.parse(xmlfile);
            Element root    = doc.getDocumentElement();
            System.out.println( "The failed count is: " + findValue( root, "/static-verification/failure-count/failure-number") );
            if ( findValue( root, "/static-verification/failure-count/failure-number").equals("0") ) 
                 passed = true;      

        } catch (org.xml.sax.SAXException se) {
               throw new Exception(se);
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
               throw new Exception(pce.toString());
        } catch (Exception e) {
               throw new Exception(e.toString());
        }
    return passed;
    }




  /**
   * Returns the contents of all immediate child text nodes, can strip whitespace
   * <p>
   * Takes a node as input and merges all its immediate text nodes into a
   * string.  If the strip whitespace flag is set, whitespace at the beggining
   * and end of each merged text node will be removed
   *
   * @param node                     node to extract text contents of
   * @param b_strip_whitespace    flag to set whitespace removal
   * @return                        string containing text contents of the node
   **/
  public static String getTextContents ( Node node )
  {
    NodeList childNodes;
    StringBuffer contents = new StringBuffer();

    childNodes =  node.getChildNodes();
    for(int i=0; i < childNodes.getLength(); i++ )
    {
      if( childNodes.item(i).getNodeType() == Node.TEXT_NODE )
      {
        contents.append(childNodes.item(i).getNodeValue());
      }
    }
    return contents.toString();
  }

  /**
   * Returns the text contents of the first node mathcing an XPath expression
   * <p>
   * Takes a context node and an xpath expression and finds a matching
   * node. The text contents of this node are returned as a string
   *
   * @param node    context node at which to eval xpath
   * @param xql   XPath expression
   * @return       Text contents of matching node
   **/
  public static String findValue(Node node, String xql) throws Exception
  {
    System.out.println(" looking for this xpath expression : " + xql);
    if( (xql == null) || (xql.length() == 0) ) {
      throw new Exception("findValue called with empty xql statement");
    }

    if(node == null) {
      throw new Exception("findValue called with null node");
    }
    return getTextContents( XPathAPI.selectSingleNode(node,xql) );
  }

}
