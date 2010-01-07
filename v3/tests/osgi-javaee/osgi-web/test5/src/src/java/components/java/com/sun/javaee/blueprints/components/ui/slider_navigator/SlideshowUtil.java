/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.io.Writer;

public class SlideshowUtil {
    
    private static boolean bDebug=false;
    
    /** Creates a new instance of SlideshowUtil */
    public SlideshowUtil() {
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
            Set<String> keySet = hmSub.keySet();
            String valueStr;
            for (String keyStr : keySet) {
                valueStr = (String)hmSub.get(keyStr);
                sxStringx = sxStringx.replaceAll(keyStr, valueStr);
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
        BufferedReader bfReader=null;
        StringBuffer sxOut=new StringBuffer();
        sxURL=getResourceURL(sxURL, resource);

        try {
            bfReader=new BufferedReader(new InputStreamReader(new URL(sxURL).openConnection().getInputStream()));
            
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
    public static void readWriteBinaryUtil(String sxURL, String resource, OutputStream outStream) throws IOException {
        
        DataOutputStream outData=null;
        DataInputStream inData=null;
        int byteCnt=0;
        byte[] buffer=new byte[4096];

        // get full qualified path of class
        System.out.println("RW Base Directory - " + sxURL);
        
        // remove class and add resource relative path
        sxURL=getResourceURL(sxURL, resource);

        System.out.println("RW Loading - " + sxURL);
        try {
            outData=new DataOutputStream(outStream);
            inData=new DataInputStream(new URL(sxURL).openConnection().getInputStream());

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
     * the given resource to the response writer in "char" format.
     */
    public static void readWriteCharUtil(String sxURL, String resource, Writer writer) throws IOException {
        
        BufferedReader bfReader=null;
        int byteCnt=0;
        char[] buffer=new char[4096];

        // get full qualified path of class
        System.out.println("RW Base Directory - " + sxURL);
        
        // remove class and add resource relative path
        sxURL=getResourceURL(sxURL, resource);

        System.out.println("RW Loading - " + sxURL);
        try {
            bfReader=new BufferedReader(new InputStreamReader(new URL(sxURL).openConnection().getInputStream()));

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
        
    
    
    public static String getResourceURL(String sxURL, String resource) {
        return sxURL.substring(0, sxURL.lastIndexOf("/")) + resource;
    }
    
}
