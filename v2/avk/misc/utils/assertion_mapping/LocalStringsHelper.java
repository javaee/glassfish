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

import java.util.*;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError; 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class LocalStringsHelper{
    public static void main(String[] args){
        try{
            checkArgs(args);
            if(args[0].equals("-map")){
                map(args);
            }else if(args[0].equals("-update")){
                update(args);
            }else if(args[0].equals("-transform")){
                transform(args);
            }
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkArgs(String[] args){
        if(!(args.length==4 && 
                     (args[0].equals("-map") || args[0].equals("-update") || args[0].equals("-transform")))){
            System.out.println ("java "+LocalStringsHelper.class.getName()+" -map <LocalStrings.properties> <starting pattern, e.g. com.sun.enterprise.tools.verifier.tests.ejb> <output XML file name>");
            System.out.println ("OR");
            System.out.println ("java "+LocalStringsHelper.class.getName()+" -update <input LocalStrings.properties> <output LocalStrings.properties> <input mapping XML file name>");
            System.out.println ("OR");
            System.out.println ("java "+LocalStringsHelper.class.getName()+" -transform <input XML> <input XSLT> <output file name>");
            System.exit(1);
        }
    }

    private static void map(String[] args) throws Exception{
        Properties prop=new Properties();
        prop.load(new FileInputStream(args[1]));
        String bp= args[2];//"com.sun.enterprise.tools.verifier.tests";
        map(prop, bp, new FileOutputStream(args[3]));
    }
    
    private static void update(String[] args) throws Exception{
        BufferedReader in=new BufferedReader(new FileReader(args[1]));
        PrintWriter out=new PrintWriter(new FileOutputStream(args[2]), true);
        Properties mappings=new Properties();
        mappings.load(new FileInputStream(args[3]));
        updateLocalStrings(in,out,mappings);
    }
    
    private static void transform(String[] args) throws Exception{
        FileInputStream xmlIn=new FileInputStream(args[1]);
        FileInputStream xsltIn=new FileInputStream(args[2]);
        FileOutputStream out=new FileOutputStream(args[3]);
        transform(xmlIn, xsltIn, out);
    }

    //Writes the XML formatted details of tests that match the given starting pattern.
    public static void map(Properties prop, String bp, OutputStream out) throws Exception{
        Document doc=getTestToAssertionMapping(prop, bp);
        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer t=tf.newTransformer();
        t.transform(new DOMSource(doc), new StreamResult(new PrintWriter(out)));
    }
    
    //returns lexicographically sorted list of test names
    //from LocalStrings.properties that match the given starting pattern 
    public static List getTestNames(Properties prop, String bp) throws Exception{
        List testNames=new ArrayList(); 
        String ep= ".assertion";
        for(Enumeration e=prop.propertyNames();e.hasMoreElements();){
            String cur=(String)e.nextElement();
            if(cur.startsWith(bp) && cur.endsWith(ep)){
                testNames.add(cur.substring(0, cur.indexOf(ep)));
            }
        }
        Collections.sort(testNames);
        return testNames;
    }

    //Returns DOM that contains the details of tests that match the given starting pattern.
    public static Document getTestToAssertionMapping(Properties prop, String bp) throws Exception{
        List testNames=getTestNames(prop, bp);
        Document doc=createDOM(bp);
        for(Iterator iter=testNames.iterator();iter.hasNext();){
            String name=(String)iter.next();
            String assertion=prop.getProperty(name+".assertion","");
            String mapping=prop.getProperty(name+".specMappingInfo","");
            addTestDetails(doc,name,assertion, mapping);
        }
        return doc;
    }
    
    private static Document createDOM(String bp) throws Exception{
        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc= builder.newDocument();
        Element tests= doc.createElement("Tests");
        Attr bpattern= doc.createAttribute("BeginningPattern");
        bpattern.setValue(bp);
        tests.setAttributeNode(bpattern);
        doc.appendChild(tests);
        tests.appendChild(doc.createTextNode("\n"));
        return doc;
    }

    //Creates a test node and adds it to the document.
    private static void addTestDetails(Document doc, String testName, String assertion, String specMappingInfo) throws Exception{
        Element testNode=doc.createElement("Test");
        doc.getDocumentElement().appendChild(testNode);
        testNode.appendChild(doc.createTextNode("\n"));

        String bp=doc.getElementsByTagName("Tests").item(0).getAttributes().getNamedItem("BeginningPattern").getNodeValue();
        Node nameNode=doc.createElement("Name");
        nameNode.appendChild(doc.createTextNode(testName.trim().substring(bp.length())));
        testNode.appendChild(nameNode);
        testNode.appendChild(doc.createTextNode("\n"));
        
        Node assertionNode=doc.createElement("Assertion");
        assertionNode.appendChild(doc.createTextNode(assertion.trim()));
        testNode.appendChild(assertionNode);
        testNode.appendChild(doc.createTextNode("\n"));

        Node specNode=doc.createElement("SpecMappingInfo");
        specMappingInfo=specMappingInfo.trim();
        specMappingInfo=specMappingInfo.length()==0? "Not yet mapped":specMappingInfo;
        specNode.appendChild(doc.createTextNode(specMappingInfo));
        testNode.appendChild(specNode);
        testNode.appendChild(doc.createTextNode("\n"));
        
        testNode.getParentNode().appendChild(doc.createTextNode("\n"));
    }
    
    public static void transform(InputStream xmlIn, InputStream xsltIn, OutputStream out) throws Exception{
        javax.xml.transform.Source xmlSource =
                new javax.xml.transform.stream.StreamSource(xmlIn);
        javax.xml.transform.Source xsltSource =
                new javax.xml.transform.stream.StreamSource(xsltIn);
        javax.xml.transform.Result result =
                new javax.xml.transform.stream.StreamResult(out);
 
        // create an instance of TransformerFactory
        javax.xml.transform.TransformerFactory transFact =
                javax.xml.transform.TransformerFactory.newInstance(  );
 
        javax.xml.transform.Transformer trans =
                transFact.newTransformer(xsltSource);
 
        trans.transform(xmlSource, result);
    }

    //creates a new LocalStrings.properties from a given LocalStrings.properties and spec mapping information.
    //This is a one time operation, which I had to do to import the mappings into the
    //LocalStrings.properties for the first time.
    public static void updateLocalStrings(BufferedReader src, PrintWriter dest, Properties mappings) throws Exception{
        String bp= "com.sun.enterprise.tools.verifier.tests.";
        String ep= ".assertion=\\";
        //Pattern pattern=Pattern.compile(p,DOTALL);
        String prev, cur;
        while((cur=src.readLine())!=null){
            //System.out.println("Read line "+cur);
            //if(pattern.matcher(cur).matches()){
            dest.println(cur);
            if(cur.startsWith(bp) && cur.endsWith(ep)){
                String test=cur.substring(0,cur.indexOf(ep));
                String mapping=mappings.getProperty(test);
                if(mapping==null) mapping="";
                do {
                    String nextLine=src.readLine();
                    if(nextLine == null){
                        System.err.println(test +" has a broken assertion. Either there is no line following \\ or there is no assertion for this test.");
                        break;
                    }
                    dest.println(nextLine);
                    int length=nextLine.length();
                    if(nextLine.charAt(length-1)!='\\'){
                        if(mapping.length()==0){
                            dest.println(test+".specMappingInfo=");
                        }else {
                        	dest.println(test+".specMappingInfo=\\\n\tPlease refer to "+mapping+" for further information.");
			}
                        break;
                    }
                } while(true);
            }//found a test name pattern
        }//end of file
        return;
    }
}
