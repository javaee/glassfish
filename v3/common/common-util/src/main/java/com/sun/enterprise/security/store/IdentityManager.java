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
package com.sun.enterprise.security.store;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class IdentityManager {

    public static final String PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY = "com.sun.aas.promptForIdentity";
    private static final String USER_ALIAS="admin-user";  
    private static final String PASSWORD_ALIAS="admin-password";  
    private static final String MASTER_PASSWORD_ALIAS="master-password";  
    private static final String IDENTITY_STORE_FILE_NAME=".identity";  

    private static String _user=null;
    private static String _password=null;
    private static String _masterPassword=null;
    private static Hashtable _htIdentity=new Hashtable();
    private static boolean bDebug=false;        
    private static boolean _keystorePropertyWasSet = true;
    private static boolean _truststorePropertyWasSet = true;
    private static boolean _nssDbPasswordPropertyWasSet = true;

    // make private so it can't be instantiated
    private IdentityManager() {}

    /**
     * getIdentityArray - This method is used when the identity information
     * needs to be passed to another process
     */
    public static String[] getIdentityArray() {
        ArrayList ar=new ArrayList();
        // add in standard values
        ar.add(getUser());
        ar.add(getPassword());
        ar.add(getMasterPassword());
        
        // add in other identity info
        Iterator it=_htIdentity.keySet().iterator();
        String key=null;
        while(it.hasNext()) {
            key=(String)it.next();
            ar.add(key + "=" + (String)_htIdentity.get(key));
        }
        
        String[] identity=new String[ar.size()];
        identity=(String[])ar.toArray(identity);

        return identity;
    }

   
    /**
    * populateFromInputStream - This method uses the stdin to populate the variables
    * of this class in the order user, password, masterpassword
    * It will not write noise to stdout.
    */
    public static void populateFromInputStreamQuietly() throws IOException {
        populateFromInputStream(System.in, true);
    }
  
    /**
    * populateFromInputStream - This method uses the stdin to populate the variables
    * of this class in the order user, password, masterpassword
    */
    public static void populateFromInputStream() throws IOException {
        populateFromInputStream(System.in);
    }


    /**
    * populateFromInputStream - This method uses the passed in InputStream to populate the variables
    * of this class in the order user, password, masterpassword
    */
    public static void populateFromInputStream(InputStream in) throws IOException {
        populateFromInputStream(in, false);
    }
        
    /**
    * populateFromInputStream - This method uses the passed in InputStream to populate the variables
    * of this class in the order user, password, masterpassword
    * @param in the InputStream to read
    * @param quiet if set to true, don't write to stdout
    */
    public static void populateFromInputStream(InputStream in, boolean quiet) throws IOException {

        // if not input stream or read identity is not enables (usually processLauncher.xml)
        // then retirn.  Wanted to make sure we could turn of the prompting if java command ran from
        // comman line
        if (bDebug) System.out.println("IM seeing if need to read in security properties from stdin");
        if (in == null || System.getProperty(PROPMPT_FOR_IDENTITY_SYSTEM_PROPERTY) == null) {
            return;
        }

        BufferedReader br=null;
        try {
            // read in each line and populate structure in the order user, password, masterpassword
            if (bDebug) System.out.println("IM attempting to read from inputstream");
            br=new BufferedReader(new InputStreamReader(System.in));
            String sxLine=null;
            int cnt=0, ipos=0;
            // help for users who are not running the command through the exposed asadmin command
            if(!quiet)
                System.out.println("Enter Admin User:");
            while ((sxLine=br.readLine()) != null) {
                if (bDebug) System.out.println("IM Number read - Reading Line:" + cnt + " - " + sxLine);
                
                // get input lines from process if any
                switch (cnt) {
                    case 0:
                        setUser(sxLine);
                        // print next prompt
                        if(!quiet)
                            System.out.println("Enter Admin Password:");
                        break;
                    case 1:
                        setPassword(sxLine);
                        // print next prompt
                        if(!quiet)
                            System.out.println("Enter Master Password:");
                        break;
                    case 2:
                        setMasterPassword(sxLine);
                        if(!quiet)
                            System.out.println("Enter Other Password Information (or ctrl-D or ctrl-Z):");
                        break;
                    default:
                        // see if tokenized string separated by and "="
                        putTokenizedString(sxLine)                        ;
                        if(!quiet)
                            System.out.println("Enter Other Password Information (or ctrl-D or ctrl-Z):");
                }
                // increment cound for next input field
                cnt++;

            }
        } catch (IOException e) {
            throw e;
        }
    }

    
    /**
     * writeToOutputStream - This method is used to writeout the contents of this class
     * to the outputstream
     */
    public static void writeToOutputStream(OutputStream out) {
        // return if no output
        if (out == null) return;
        
        PrintWriter writer=null;
        // open the output stream
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        if (bDebug) System.out.println("Writing to OutputStream: " + getFormatedContents());
        // get input lines from process if any
        writer.println(getUser());
        writer.println(getPassword());
        writer.println(getMasterPassword());

        // add in other identity items
        Iterator it=_htIdentity.keySet().iterator();
        String key=null;
        while(it.hasNext()) {
            key=(String)it.next();
            writer.println(key + "=" + (String)_htIdentity.get(key));
        }
        writer.flush();
        writer.close();
    }
    
    
    /**
     * This method is used instead of the toString method for static instances
     */
    public static String getFormatedContents() {        
        StringBuffer sb=new StringBuffer("IdentityManager Data: User:" + getUser());
        //Only display password information if compiling with debug enabled; otherwise, this 
        //is a security violation.
        if (bDebug) {
            sb.append(", ");
            sb.append("Password:" + getPassword() + ", ");
            sb.append("MasterPassword:" + getMasterPassword() + ", ");
            Iterator it=_htIdentity.keySet().iterator();
            String key=null;
            while(it.hasNext()) {
                key=(String)it.next();
                sb.append(key + ":" + (String)_htIdentity.get(key) + ", ");
            }
        }
        return sb.toString();
    }
    
    
    //
    // public access and mutators for convienence
    public static void setUser(String userx) {
        _user=userx;
    }
    public static String getUser() {
        return _user;
    }

    public static void setPassword(String passwordx) {
        _password=passwordx;
    }
    
    public static String getPassword() {
        return _password;
    }

    public static void setMasterPassword(String masterPasswordx) {
        _masterPassword=masterPasswordx;
        //We set the keystore and truststore password properties (used for JSSE) 
        //to the master password value if they are not already set. This is necessary
        //for PE only and not for SE/EE since NSS is used.
        //The xxxWasSet booleans keep track of whether the system property was initially set 
        //(e.g. in domain.xml). When false, this indicates that we have set the property and 
        //that we should continue to set it if the master password is changed. This is necessary
        //since the master password can be changed (e.g. asadmin change-master-password) and
        //setMasterPassword called multiple times.
        if (System.getProperty(SystemPropertyConstants.KEYSTORE_PROPERTY) != null) {
            if (!_keystorePropertyWasSet || 
                System.getProperty(SystemPropertyConstants.KEYSTORE_PASSWORD_PROPERTY) == null) 
            {
                System.setProperty(SystemPropertyConstants.KEYSTORE_PASSWORD_PROPERTY, 
                    getMasterPassword());
                _keystorePropertyWasSet = false;
            }
        }
        if (System.getProperty(SystemPropertyConstants.TRUSTSTORE_PROPERTY) != null) {
            if (!_truststorePropertyWasSet || 
                System.getProperty(SystemPropertyConstants.TRUSTSTORE_PASSWORD_PROPERTY) == null) 
            {
                System.setProperty(SystemPropertyConstants.TRUSTSTORE_PASSWORD_PROPERTY, 
                    getMasterPassword());
                _truststorePropertyWasSet = false;
            }
        }
        if (System.getProperty(SystemPropertyConstants.NSS_DB_PROPERTY) != null) {
            if (!_nssDbPasswordPropertyWasSet ||
                System.getProperty(SystemPropertyConstants.NSS_DB_PASSWORD_PROPERTY) == null) 
            {
                System.setProperty(SystemPropertyConstants.NSS_DB_PASSWORD_PROPERTY, 
                    getMasterPassword());
                _nssDbPasswordPropertyWasSet = false;
            }
        }
    }

    public static void setMasterPassword(char[] passwd) {
       _masterPassword = new String(passwd);
    }
  
    public static String getMasterPassword() {
        return _masterPassword;
    }
    
    
    public static void putTokenizedString(String sxToken) {
        // put value in mapped file for use by nssutils and unknown numbers of input
        // see if tokenized string separated by and "="
        int ipos=sxToken.indexOf("=");
        if (ipos > 0) {
            // break into key value pair and put into map
            put(sxToken.substring(0, ipos), sxToken.substring(ipos + 1));
        }
    }
    
    
    public static void put(String key, String value) {
        // put value in mapped file for use by nssutils and unknown numbers of input
        _htIdentity.put(key, value);
    }

    public static String get(String key) {
        return (String)_htIdentity.get(key);
    }

    public static void addToMap(HashMap map) 
    {
        Iterator it = map.keySet().iterator();
        String key = null;
        while(it.hasNext()) {
            key = (String)it.next();
            put(key, (String)map.get(key));
        }
    }
    
    public static Map getMap() {
        // create a deep copy of the map so it can't be
        // side effected by a thirdparty util
        HashMap hm=new HashMap();
        Iterator it=_htIdentity.keySet().iterator();
        String key=null;
        while(it.hasNext()) {
            key=(String)it.next();
            hm.put(new String(key), new String((String)_htIdentity.get(key)));
        }
        return hm;
    }
    
    
    
    
    
    
    //
    // these methods write out the identitymanager so it cab be read in later
    
    /**
     * createIdentityStore - This method takes the IdentityManager singleton and
     * writes its information into a keystore for later retrieval.  This method is used for temportary
     * storage of Identity information for use by task such as restart.  The extra token information
     * is read in through the appropriate manager because its variable nature was problematic to store.
     */
     public static void createIdentityStore() 
        throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, IOException
     {

        // create temporary keystore for start to read from
        Properties aliasPasswordProps=new Properties();
        aliasPasswordProps.setProperty(USER_ALIAS, getUser());
        aliasPasswordProps.setProperty(PASSWORD_ALIAS, getPassword());
        aliasPasswordProps.setProperty(MASTER_PASSWORD_ALIAS, getMasterPassword());
        
        File instanceRoot = new File(System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY), IDENTITY_STORE_FILE_NAME);        

        // get the password for the keystore
        PasswordAdapter p = new PasswordAdapter(instanceRoot.getAbsolutePath(), 
           getMasterPasswordPassword());

        // loop through properties and set passwords for aliases
        Iterator iter=aliasPasswordProps.keySet().iterator();
        String alias=null, pass=null;
        while(iter.hasNext()) {
            alias=(String)iter.next();
            pass=aliasPasswordProps.getProperty(alias);
            p.setPasswordForAlias(alias, pass.getBytes());
        }
    }
     
     
    /**
     * readIdentityManagerFile - This method us used to populate the IdentityManager singleton and
     * reas its information from a keystore that was previously created.  This method is used to retrieve
     * temportarily storaged Identity information for use by task such as restart. The extra token information
     * is read in through the appropriate manager because its variable nature was problematic to store.
     */
    public static void readIdentityStore() 
        throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException 
    {
        
        
        File instanceRoot = new File(System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY), IDENTITY_STORE_FILE_NAME);        
        
        System.out.println("****** READING IDENTITY FROM ====>" + instanceRoot.getAbsolutePath());
        
        if (instanceRoot.exists()) {            
            PasswordAdapter p = new PasswordAdapter(instanceRoot.getAbsolutePath(), 
                getMasterPasswordPassword());

            setUser(p.getPasswordForAlias(USER_ALIAS));
            setPassword(p.getPasswordForAlias(PASSWORD_ALIAS));
            setMasterPassword(p.getPasswordForAlias(MASTER_PASSWORD_ALIAS));
        }
    }
     
    
    
    public static void deleteIdentityStore() {
        File instanceRoot = new File(System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY), IDENTITY_STORE_FILE_NAME);        
        instanceRoot.delete();
    }
    
    
    
    /**
     *
     * @return The password protecting the master password keywtore
     */    
    private static char[] getMasterPasswordPassword()
    {
        //FIXTHIS: Need a better password which varies across machines but is not the ip address.      
        return MASTER_PASSWORD_ALIAS.toCharArray();
    }
   
     
}
