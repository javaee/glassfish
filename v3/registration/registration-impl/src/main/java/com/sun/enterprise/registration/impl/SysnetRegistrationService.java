/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.registration.impl;

import com.sun.enterprise.registration.RegistrationService;
import com.sun.enterprise.registration.RegistrationException;
import com.sun.enterprise.registration.RegistrationAccount;
import com.sun.enterprise.registration.RegistrationServiceConfig;
import com.sun.enterprise.registration.RegistrationDescriptor;

import com.sun.scn.servicetags.EnvironmentInformation;
import com.sun.scn.servicetags.SvcTag;
import com.sun.scn.servicetags.SunOnlineAccount;
import com.sun.scn.client.comm.RegistrationWrapper;
import com.sun.scn.client.comm.SvcTagException;

import java.net.InetAddress;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SysnetRegistrationService implements RegistrationService {
        
    /* Creates a new service which persists the registration data in the specified File.
     * Main entry point for application server code doing registry 
     * @param localRepositoryFile a File object for the local repository file
     */
    public SysnetRegistrationService(File localRepositoryFile) {
        this.localRepositoryFile = localRepositoryFile;
        regWrapper = new RegistrationWrapper(getDefaultRegistrationID());
    }

    /* Creates a new service which persists the registration data in the specified File.
     * Main entry point for application server code doing registry 
     * @param localRepositoryFile a File object for the local repository file
     * @param registrationID registration identifier
     */
    public SysnetRegistrationService(File localRepositoryFile, String registrationID) {
        this.localRepositoryFile = localRepositoryFile;
        regWrapper = new RegistrationWrapper(registrationID);
    }

    /* Creates a new service which persists the registration data in the specified File.
     * Main entry point for application server code doing registry 
     * @param localRepositoryFile a File object for the local repository file
     * @param proxyHost proxyhost name used to connect to internet
     * @param proxyPort proxy port number used to connect to internet
     */
    public SysnetRegistrationService(File localRepositoryFile, String proxyHost, int proxyPort) {
        this.localRepositoryFile = localRepositoryFile;
        regWrapper = new RegistrationWrapper(getDefaultRegistrationID(), proxyHost, proxyPort);
    }

    /* Creates a new service which persists the registration data in the specified File.
     * Main entry point for application server code doing registry 
     * @param localRepositoryFile a File object for the local repository file
     * @param proxyHost proxyhost name used to connect to internet
     * @param proxyPort proxy port number used to connect to internet
     */
    public SysnetRegistrationService(File localRepositoryFile, String proxyHost, int proxyPort, String registrationID) {
        this.localRepositoryFile = localRepositoryFile;
        regWrapper = new RegistrationWrapper(registrationID, proxyHost, proxyPort);
    }


    public SysnetRegistrationService(Object[] params) {
        this.localRepositoryFile = (File)params[0];
        regWrapper = new RegistrationWrapper(getDefaultRegistrationID(), 
                (String)params[1], ((Integer)params[2]).intValue());
    }

    public SysnetRegistrationService(RegistrationServiceConfig rc) {
        Object[] params = rc.getParams();
        this.localRepositoryFile = (File)params[0];
        if (params.length == 1) {
            regWrapper = new RegistrationWrapper(getDefaultRegistrationID()); 
        }
        else if (params.length ==2) { //filename, registrationid is passed
            regWrapper = new RegistrationWrapper((String)params[1]); 
        }
        else if (params.length == 3) { //filename, host, port
            regWrapper = new RegistrationWrapper((String)params[1], 
                (String)(rc.getParams()[1]), 
                ((Integer)(rc.getParams()[2])).intValue());
        }
        else { // filename, host, port, registrationid
            regWrapper = new RegistrationWrapper( 
                (String)(rc.getParams()[3]),  
                (String)(rc.getParams()[1]), 
                ((Integer)(rc.getParams()[2])).intValue());
        }
    }
    
    /* @return REGISTRATOR_ID used to create the sysnet RegistrationWrapper object */
    private String getDefaultRegistrationID() {
        return REGISTRATION_ID; 
    }
    
    
    public boolean isRegistrationEnabled() {
        // hack to disable registration on AIX. The sysnet registration APIs do not work on AIX.
         if (AIX.equalsIgnoreCase(System.getProperty("os.name")))
             return false;
        return localRepositoryFile.canWrite();
    }
    
    /*  Registers the generated ServiceTags to SunConnection backend */
    public void register(RegistrationAccount account) 
        throws RegistrationException, ConnectException, UnknownHostException {    
        try {
            List<ServiceTag> serviceTags = 
                    getRegistrationDescriptors(RegistrationDescriptor.RegistrationStatus.NOT_REGISTERED);
            if (serviceTags.size() == 0) {
                logger.log(Level.WARNING, "No unregistered tags found");
                return;
            }
            String hostName = "";    
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch(Exception ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }
            EnvironmentInformation env = new EnvironmentInformation(
                    hostName, "", // hostID
                    System.getProperty("os.name"), 
                    System.getProperty("os.version"), 
                    System.getProperty("os.arch"), 
                    "", //systemModel
                    "", //systemManuf.
                    "", //cpuManuf
                    "");
            
            List<SvcTag> svcTags = ServiceTag.getSvcTags(serviceTags);
            String registryURN = "urn:st:" + UUID.randomUUID().toString();
            
            logger.log(Level.FINEST, "registryURN = " + registryURN);
            logger.log(Level.FINE, "Attempting to  register " + svcTags.size() + " servicetags");
            SOAccount soAccount = (SOAccount)account;
            regWrapper.registerServiceTags(env, svcTags, registryURN, 
                    soAccount.getUserID(), soAccount.getPassword());
            setRegistrationStatus(RegistrationStatus.REGISTERED);
            logger.log(Level.INFO, "Registered " + svcTags.size() + " tags");
        } catch (SvcTagException ex) {
            throw new RegistrationException(ex);
       }
    }
    
    /* Creates a Sun Online Account 
     * @param soa SunOnlineAccount object
     * throws RegistrationException if the online account could not be created.
     */
    public void createRegistrationAccount(RegistrationAccount soa) 
            throws RegistrationException {        
        try {
            SunOnlineAccount account = ((SOAccount)soa).getSunOnlineAccount();
            regWrapper.createSunOnlineAccount(account);
        } catch(SvcTagException ex) {
            throw new RegistrationException(ex);
        }
    }
    
    public List getRegistrationDescriptors() throws RegistrationException {
        RepositoryManager rm = getRepositoryManager();
        // make sure runtime values are generated in RepositoryManager
        rm.updateRuntimeValues();
        return rm.getServiceTags();
    }

    public List getRegistrationDescriptors(String productURN) throws RegistrationException {
        List<ServiceTag> st1 = getRegistrationDescriptors();
        List<ServiceTag> st2 = new ArrayList();
        for (int i = 0; i < st1.size(); i++) {
            ServiceTag st = st1.get(i);
            if (st.getProductURN().equals(productURN))
                st2.add(st);
        }
        return st2;
    }
    
    public List getRegistrationDescriptors(RegistrationDescriptor.RegistrationStatus status) throws RegistrationException {
        List<ServiceTag> st1 = getRegistrationDescriptors();
        List<ServiceTag> st2 = new ArrayList();
        RepositoryManager rm = getRepositoryManager();
        for (int i = 0; i < st1.size(); i++) {
            ServiceTag st = st1.get(i);
            if (rm.getRegistrationStatus(st).equals(status))
                st2.add(st);
        }
        return st2;
    }

    public List<String> getAvailableCountries() {
        return regWrapper.getAvailableCountries();
    }
    

    /**
     *	<p> This method will return a List of 2 lists.  The first list is the
     *      country list of the specified locale. The second list is the country
     *      list in _en locale that the sysnet backend is expecting.  
     *       
     *	@param	Locale	loccale  locale of the list to be displayed
     *
     *	@return	List<List>  country list. First elemnt is in the sepcified local,
     *                      second is _en locale that backend expects. 
     */

    public List getAvailableCountries(Locale locale) {
        String cts =  StringManager.getString("COUNTRY_LIST_TOTAL_COUNT");

        int count = Integer.parseInt(cts);
        List displayList = new ArrayList();
        List actualList = new ArrayList();
        for(int i=1; i<count+1; i++) {
            displayList.add( StringManager.getString("COUNTRY-"+i) );
            actualList.add(StringManager.getString("en_COUNTRY-"+i));
        }
        List ret = new ArrayList(2);
        ret.add(0, displayList);
        ret.add(1, actualList);
        return ret;
    }

    public List<String> getAvailableSecurityQuestions() {
        return regWrapper.getAvailableSecurityQuestions();
    }
    
    /* read the registration reminder from local persistent store */
    public RegistrationReminder getRegistrationReminder() throws RegistrationException {
        return getRepositoryManager().getRegistrationReminder();
    }
    
    /* set the registration reminder to local persistent store */
    public void setRegistrationReminder(RegistrationReminder reminder) throws RegistrationException {        
        getRepositoryManager().setRegistrationReminder(reminder);
    }

    /* read the registration status from local persistent store */
    public RegistrationStatus getRegistrationStatus() throws RegistrationException {
        return getRepositoryManager().getRegistrationStatus();
    }

    /* set the registration status to local persistent store */
    public void setRegistrationStatus(RegistrationStatus status) throws RegistrationException {        
        getRepositoryManager().setRegistrationStatus(status);
    }
   
    public String getPasswordHelpURL() {
        return "https://reg.sun.com/accounthelp";
    }

    public boolean isRegistrationAccountValid(RegistrationAccount account) 
            throws RegistrationException, UnknownHostException, ConnectException {
        SOAccount soAccount = (SOAccount)account;
        try {
            regWrapper.authenticate(soAccount.getUserID(), soAccount.getPassword());
        } catch (SvcTagException sve) {
            throw new RegistrationException(sve);
        }
        return true;
    }

    /**
     * Transfers locally-registered service tags that have not yet been transferred
     * to the SysNet repository to the SysNet repository using the stclient 
     * utility and also updates the service tag's status in the private
     * repository to record the transfer.
     * 
     * @throws RegistrationException for errors reading the local repository or
     * invoking stclient to transfer them
     */
    public void transferEligibleServiceTagsToSysNet() throws RegistrationException {
        
        SysnetTransferManager transferManager = 
                new SysnetTransferManager(localRepositoryFile);
        
        transferManager.transferServiceTags();
    }

    private RepositoryManager getRepositoryManager() throws RegistrationException {
        return new RepositoryManager(localRepositoryFile);
    }
    
    private final RegistrationWrapper regWrapper;
    private final File localRepositoryFile;
    private static final String REGISTRATION_ID = "glassfish";
    private static final String AIX = "AIX";
    private static final Logger logger = RegistrationLogger.getLogger();
}
