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

package com.sun.javaee.blueprints.components.ui.popup;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Writer;

/**
 *
 * @author basler
 */
public class PopupUtil {
    //
    // TBD: Should cleanup methods that are no longer being used
    //
    
    private static boolean bDebug=false;
    
    /** Creates a new instance of PopupUtil */
    public PopupUtil() {
    }
    
    
   /**
    * Performs Substitution on passed in strings.  This method could also use a cache to loop up strings previously used
    *
    * @param sxString String to parse
    * @param hmSub A Hashmap of of keys/variables to change
    * @param bLookup Whether to see if the value is to be looked up in the cache.  If true the sxString is used as a key
    */
    public static String parseString(String sxString, HashMap hmSub, boolean bLookup) {

        int iPos=0;
        String sxStringx="";

        if(bLookup) {
            // pull from cache
            //sxStringx=(String)getPages().get(sxString);
        } else {		    
            sxStringx=sxString;
        }

        if (sxStringx != null) {
            // populate other values that are page specific
            Iterator iter=hmSub.keySet().iterator();
            String sxKey, sxValue;
            while(iter.hasNext()) {

                // get key and vaule for substitution
                sxKey=(String)iter.next();
                sxValue=(String)hmSub.get(sxKey);
                sxStringx=sxStringx.replaceAll(sxKey, sxValue);
            }
        }
        return sxStringx;
    }     
        

    /*
     * Reads in a file in for processing, using rendering class location
     *
     * @param sxFile The file name to read in
     * @return File in string format
     */
    public static String readInFragmentAsString(String sxURL, String resource) throws IOException {
        sxURL=getResourceURL(sxURL, resource);
        return readInFragmentAsString(new URL(sxURL));
    }
    
    /*
     * Reads in a file in for processing, using rendering class location
     *
     * @param sxFile The file name to read in
     * @return File in string format
     */
    public static String readInFragmentAsString(URL sxURL) throws IOException {
        BufferedReader bfReader=null;
        StringBuffer sxOut=new StringBuffer();

        try {
            bfReader=new BufferedReader(new InputStreamReader(openStream(sxURL)));
            
            int byteCnt=0;
            char[] buffer=new char[4096];
            while ((byteCnt=bfReader.read(buffer)) != -1) {
                if (byteCnt > 0) {
                   sxOut.append(buffer, 0, byteCnt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(bfReader != null) {
                try {
                    bfReader.close();
                } catch (Exception ee){}
            }
        }
        
        // place page in cache ???
        //htTemp.put(sxPageUrl,sxOut);
        
        return sxOut.toString();
    }    
    
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response output in binary format (needed for images).
     */
    public static void readWriteBinaryUtil(URL sxURL, OutputStream outStream) throws IOException {
        
        DataOutputStream outData=null;
        DataInputStream inData=null;
        int byteCnt=0;
        byte[] buffer=new byte[4096];

        // get full qualified path of class
        if(bDebug) System.out.println("RW Base Directory - " + sxURL);
        
        if(bDebug) System.out.println("RW Loading - " + sxURL);
        try {
            outData=new DataOutputStream(outStream);
            inData=new DataInputStream(openStream(sxURL));

            while ((byteCnt=inData.read(buffer)) != -1) {
                if (outData != null && byteCnt > 0) {
                    outData.write(buffer, 0, byteCnt);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if(inData != null) {
                    inData.close();
                }
            } catch (IOException ioe) {}
        }        
    }    
    
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response output in binary format (needed for images).
     */
    public static void readWriteBinaryUtil(String sxURL, String resource, OutputStream outStream) throws IOException {
                
        // remove class and add resource relative path
        sxURL=getResourceURL(sxURL, resource);
        readWriteBinaryUtil(new URL(sxURL), outStream);
    }    
    
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response writer in "char" format.
     */
    public static void readWriteCharUtil(URL sxURL, Writer writer) throws IOException {
        
        BufferedReader bfReader=null;
        int byteCnt=0;
        char[] buffer=new char[4096];

        // get full qualified path of class
        if(bDebug) System.out.println("RW Base Directory - " + sxURL);
        
        if(bDebug) System.out.println("RW Loading - " + sxURL);
        try {
            bfReader=new BufferedReader(new InputStreamReader(openStream(sxURL)));

            while ((byteCnt=bfReader.read(buffer)) != -1) {
                if (writer != null && byteCnt > 0) {
                    writer.write(buffer, 0, byteCnt);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if(bfReader != null) {
                    bfReader.close();
                }
            } catch (IOException ioe) {}
        }        
    }            
    
    /**
     * The URL looks like a request for a resource, such as a JavaScript or CSS file. Write
     * the given resource to the response writer in "char" format.
     */
    public static void readWriteCharUtil(String sxURL, String resource, Writer writer) throws IOException {
        
        // remove class and add resource relative path
        sxURL=getResourceURL(sxURL, resource);
        readWriteCharUtil(new URL(sxURL), writer);
    }                
    
    public static String getResourceURL(String sxURL, String resource) {
        return sxURL.substring(0, sxURL.lastIndexOf("/")) + resource;
    }        
    
    private static InputStream openStream(URL sxURL) throws IOException {
        // set cache to false, so the ./lib/bp-popup-balloon.jar file doesn't get locked on redeploy or undeploy on windows.
        URLConnection urlConn = sxURL.openConnection();
        urlConn.setUseCaches(false);
        return urlConn.getInputStream();
    } 
    private static InputStream openStream(String sxURL) throws IOException {
        return openStream(new URL(sxURL)); 
    }     
}
