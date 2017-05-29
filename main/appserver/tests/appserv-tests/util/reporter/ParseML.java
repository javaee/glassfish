/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.ccl.reporter;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;

public class ParseML{
    private String vendorParserClass = 
    "org.apache.xerces.parsers.SAXParser";
    public static boolean debug = false;
    boolean inTestcase = false;
    boolean inTestcaseID = false;
    boolean gotStatus = false;
    static Hashtable hash =  null;
    static Vector[] fileDiffs = null;
    String key = "";
    String value = "";

    public static void main(String args[]){
        ParseML pml = new ParseML();
        if(args.length<2){
            pml.usage();
        }
        hash = new Hashtable();
        int no_of_files = args.length;
        if(debug){
            System.out.println("number of files:"+no_of_files);
        }
        Hashtable[] filehash = new Hashtable[no_of_files];
        fileDiffs = new Vector[no_of_files];
        for(int i = 0; i<args.length; i++){              
            filehash[i] = new Hashtable();
            fileDiffs[i] = new Vector();
            fileDiffs[i].add("</u><b>File:"+args[i]+"</b></u><br>\n"); // add the filename to the vector
            System.out.println("parsing file #"+i);
            filehash[i] = pml.buildTree(args[i]);
            hash = null;
            hash = new Hashtable();
            if(debug){
                System.out.print("========================");
                System.out.println("Displaying hashtable # "+i);
                pml.displayHash(filehash[i]);
            }
        }                                
        pml.diffHash(filehash);
        pml.displayVectorArr(fileDiffs);
        System.out.print("writing diffs to file...");
        pml.writeDiffs(fileDiffs);
        System.out.println("done");
    }
    public Hashtable buildTree(String xmlURI){
        //hash = null;
        try{
            //Initialize a reader
            XMLReader reader = 
            XMLReaderFactory.createXMLReader(vendorParserClass);
            //Register Content Handler
            ContentHandler myContentHandler = new MyContentHandler();
            reader.setContentHandler(myContentHandler);

            //Parse
            InputSource inputSource = 
            new InputSource(new java.io.FileInputStream(new java.io.File(xmlURI)));
            reader.parse(inputSource);

        } catch(Throwable th){
            th.printStackTrace();
        }
        return hash;
    }

    class MyContentHandler implements ContentHandler{
        private Locator locator;
        public void setDocumentLocator(Locator locator){
            this.locator = locator;
        }
        public void startDocument() throws SAXException{
            //initialize a 2D vector here
        }
        public void endDocument() throws SAXException{
        }
        public void processingInstruction(String target, String data)
            throws SAXException{
            // add target and data to the 2D vector
        }
        public void startPrefixMapping(String prefix, String uri){
        }
        public void endPrefixMapping(String prefix){
        }
        public void startElement(String namespaceURI, String localName, 
                                 String qName, Attributes atts)
            throws SAXException{
            // add the local name into vector
            if(localName.equals("testcase")){
                inTestcase = true;
            }
            if(localName.equals("id") && (inTestcase)){
                inTestcaseID = true;
                if(debug){
                    System.out.println("inside testcase id.");
                }
            }
            // assuming that when its time to get the status, the value for the keys would have been obtained. 
            if(localName.equals("status") && (inTestcase)){
                //get attributes: pass/fail
                if(debug){
                    System.out.println("getting testcase status...");                   
                }
                value = atts.getValue(0).trim();
                if((key!=null) && (!(key.equals(""))) && (value!=null) && (!value.equals(""))){
                    if(debug){
                        System.out.println("Key["+key+"] has value["+value+"]");                   
                    }
                    hash.put(key,value);
                    gotStatus = true;
                    key = ""; 
                    value = "";
                } 
                else{
                    if(key == null || key.equals("")){
                        System.out.println("invalid key!");
                    }
                    if(value== null || value.equals("")){
                        System.out.println("invalid value!");
                    }             
                }
            }
        }
        public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException{
            if(localName.equals("testcase")){
                inTestcase = false;
                if(debug){
                        System.out.println("Outside testcase tag");                   
                }
            }
            if(localName.equals("id") && (inTestcase)){
                inTestcaseID = false;
                if(debug){
                    System.out.println("Outside testcase-ID tag");                   
                }  /*
                key = key.trim();
                value = value.trim();
                if((key!=null) && (value!=null) && 
                   (!(value.equals(""))) && (!(key.equals("")))){

                }    */
            }
        }
        public void characters(char[] ch, int start, int length)
            throws SAXException{
            String s = new String(ch, start, length);
            if(debug){
                System.out.print("\nvalue of start index= "+start+", ");
                System.out.print("length of charectars = "+length+", ");
                System.out.println("string = ["+s+"]");
            }
            if(inTestcaseID){
                key += s.trim();
                if((key!=null) && (!key.equals(""))){
                    if(debug){
                        System.out.println("TestCase ID:"+key);
                    }
                }
            }
        }
        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException{
        }
        public void skippedEntity(String name) throws SAXException{
            System.out.println("Skipped entity is:"+name);
        }  
    }
    /*======= End of Inner Class Definition ================*/

    public void displayHash(Hashtable hashtable){ 
        for (Enumeration e = hashtable.keys() ; e.hasMoreElements() ;) {
            String keyval = (String)e.nextElement();
            System.out.println(keyval +":"+hashtable.get(keyval));
        }
    } 

    public void displayVector(Vector v){
        for(int i=0; i<v.size(); i++){
            System.out.println((String)v.get(i));
        }
    }

    public void displayVectorArr(Vector[] v){
        for (int i = 0; i<v.length; i++){
            displayVector(v[i]);
        }
    }

    public void writeDiffs(Vector[] v){
        try{
            File inputfile = new File("fileDiffs.html");
            FileWriter fw = new FileWriter(inputfile);
            String filecontent = "<h3>File Diff Output</h3><hr>\n";            
            for(int i = 0; i<v.length ; i++){    
                for(int j = 0; j<v[i].size(); j++){
                    filecontent += v[i].get(j)+"<br>\n";
                }
                filecontent += "<hr>\n";
            }
            fw.write(filecontent);
            fw.close();
        } catch(FileNotFoundException fnfe){
            System.out.println("File is not present. \n"+
                               "Please check the file name and try again");
        } catch(Exception ex){
            ex.printStackTrace();
        }        
    }

    public Vector getKeyList(Hashtable[] hashes){
        int hashtables = hashes.length;
        Vector allkeys= new Vector();
        Vector[] hashVecs = new Vector[hashtables];
        for(int i=0; i<hashtables; i++){            
            hashVecs[i] = new Vector((Collection)hashes[i].keySet());
        }
        allkeys = getUnion(hashVecs);
        return allkeys;
    }

    public Vector getUnion(Vector[] v){
        Vector union = new Vector();
        for(int i=0; i<v.length; i++){
            for(int j=0; j<v[i].size(); j++){
                if(!union.contains(v[i].get(j))){
                    union.add(v[i].get(j));
                }//end if
            }//end inner-for
        }// end outer-for
        return union;
    }// end method getUnion

    public void diffHash(Hashtable[] hashes){
        int hashtables = hashes.length;
        if(debug){
            System.out.println("total hashtables to diff:"+hashtables);
        }
        int bigHash = 0; // take the first hashtable as the golden file.
        /*Get a list of most keys*/ 
        Vector keylist = new Vector();
        keylist = getKeyList(hashes);
        /*Start comparing all other hashtable elements to this golden list of testcases*/

        int totalkeys = keylist.size();
        if(debug){
            System.out.println("Total number of testcases gathered: "+totalkeys);
        }
        for(int keyno=0; keyno<totalkeys; keyno++){ 
            String keyObj = (String)keylist.get(keyno);
            Object val = null;
            Object bigHashVal = null;
            // for all hashtables in the array...
            if(debug){
                System.out.println("checking out key:"+keyObj);
            }
            for(int i = 0; i<hashtables ; i++){               
                if(i==bigHash){ 
                    continue;
                }
                // key exists
                if((bigHashVal=hashes[bigHash].get(keyObj))!=null){ // key exists in bigHash
                    if((val = hashes[i].get(keyObj))!=null){
                        if (val.equals(bigHashVal)){
                            // key and value pair match the golden file
                            continue;
                        } else {
                            // key exists but the values are different
                            // add it into both the vectors, if not already there. 
                            String diffObj = "<font color=blue>"+keyObj+
                                " has an inconsistent status:"+(String)val+
                                "</font>"; 
                            String bigdiffObj =  "<font color=blue>"+keyObj+
                                " has an inconsistent status:"+(String)bigHashVal+
                                "</font>";
                            if(!fileDiffs[i].contains(diffObj)){
                                fileDiffs[i].add(diffObj);
                            }
                            if(!fileDiffs[bigHash].contains(bigdiffObj)){
                                fileDiffs[bigHash].add(bigdiffObj);
                            }
                        }   
                    } else {
                        // key doesn't exist
                        /*add it into the vector for smaller tables, if not already there.*/   
                        String diffObj =  "<font color=red>"+keyObj+
                            " testcase is missing </font>";
                        if(!fileDiffs[i].contains(diffObj)){
                            fileDiffs[i].add(diffObj);
                        }// end missing-key if
                    }   // end missing-key else
                    // end key-exists if in bighash
                } else {
                    // add the missing key into the vector for bighash
                    String bigdiffObj = "<font color=red>"+keyObj+
                        " testcase is missing </font>";
                    if(!fileDiffs[bigHash].contains(bigdiffObj)){
                        fileDiffs[bigHash].add(bigdiffObj);
                    }// end missing-key if                        
                }
            } // end for-loop for going through the hashtable array
        } // end for-loop for keys in the bigHash hashtable
    } // end diff-hash method              

    public void usage(){    
        String usageStr = "Usage:\n java ParseML <xml_file_1> <xml_file_2> <xml_file_3>..."+
            "\n\tThis Utility will let you 'diff' multiple xml"+
            "\n\t files and produce a fileDiff.html file in the "+
            "\n\t directory where this file is run from."+
            "\n\tDiff results are produced only by matching the contents "+
            "\n\tof the xml trees and not by comparing text characters."+
            "\n\n\tThis program takes arguments of two or more 'well_formed_xml' files.";
        System.out.println(usageStr);
        System.exit(0);
    }
}
