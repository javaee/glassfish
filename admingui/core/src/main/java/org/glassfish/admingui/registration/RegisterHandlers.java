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
 * RegisgterHandlers.java
 *
 * Created on March 24, 2008
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.registration;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File; 

import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.enterprise.registration.glassfish.RegistrationUtil;
import com.sun.enterprise.registration.RegistrationAccount;
import com.sun.enterprise.registration.RegistrationService;
import com.sun.enterprise.registration.RegistrationService.RegistrationStatus;
import com.sun.enterprise.registration.RegistrationService.RegistrationReminder;
import com.sun.enterprise.registration.impl.SOAccount;
import com.sun.enterprise.registration.impl.SysnetRegistrationService;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import javax.faces.context.FacesContext;

/**
 *
 * @author Anissa Lam
 */
public class RegisterHandlers {

    
    /** Creates a new instance of RegisterHandlers */
    public RegisterHandlers() {
    }


    /**
     *	<p> This handler test if registration related operation should be performed  </p>
     *
     *  <p> Output value: "value" -- Type: <code>Boolean</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="showRegistration",
    output={
        @HandlerOutput(name="value", type=Boolean.class)})
    public static void showRegistration(HandlerContext handlerCtx) {

        RegistrationService regService = getRegistrationService();
        if (regService == null) {
            GuiUtil.getLogger().info("No RegistrationService available.");
            handlerCtx.setOutputValue("value", false);
            return;
        }
        if (( getRegistrationStatus() == RegistrationStatus.NOT_REGISTERED) &&
                !regService.isRegistrationEnabled()){
            GuiUtil.getLogger().info("Product is not registered and registration is not Enabled");
            handlerCtx.setOutputValue("value", false);
            return;
        }
        handlerCtx.setOutputValue("value", true);
    }


    @Handler(id="setShowRegTreeNode")
    public static void testIfRegistered(HandlerContext handlerCtx) {
        if( GuiUtil.getSessionValue("showRegTreeNode") == null){
            GuiUtil.setSessionValue("showRegTreeNode", (getRegistrationStatus() != RegistrationStatus.REGISTERED));
        }
    }

    private static RegistrationStatus getRegistrationStatus() {
        if ((Boolean) GuiUtil.getSessionValue("_noNetwork")){
            return RegistrationStatus.REGISTERED;
        }
        RegistrationStatus regStatus = RegistrationStatus.NOT_REGISTERED;
        try {
            RegistrationService regService = getRegistrationService();
            if (regService != null) {
                regStatus = regService.getRegistrationStatus();
//                System.out.println("======== DEBUG ==== getRegistrationStatus returns " + regStatus);
	    }
        } catch(Exception ex) {
	    // FIXME: Log trace instead
            ex.printStackTrace();
        }

        return regStatus;
    }

    private static RegistrationReminder getRegistrationReminder() {
        if ((Boolean) GuiUtil.getSessionValue("_noNetwork")){
            return RegistrationReminder.DONT_ASK_FOR_REGISTRATION;
        }
        try {
            RegistrationService regService = getRegistrationService();
            if ( regService.getRegistrationStatus() == RegistrationStatus.REGISTERED){
//                System.out.println("====== DEBUG ===== getRegistrationStatus() returns  REGISTERED");
                return RegistrationReminder.DONT_ASK_FOR_REGISTRATION;
            }
            
            if ( !regService.isRegistrationEnabled() ){   //user has not write permission, so don't remind them
//                System.out.println("====== DEBUG ======  isRegistrationEnabled() return false" );
                return RegistrationReminder.DONT_ASK_FOR_REGISTRATION;
            }
            RegistrationReminder  rem = regService.getRegistrationReminder();
//            System.out.println("======== DEBUG ====== getRegistrationReminder() returns " + rem );
            return rem;
        }catch(Exception ex){
	    // FIXME: Log trace instead
            ex.printStackTrace();
            return RegistrationReminder.DONT_ASK_FOR_REGISTRATION;
        }
        
    }
    
    private static void setRegistrationReminder(RegistrationReminder value) {
        try {
            RegistrationService regService = getRegistrationService();
            regService.setRegistrationReminder(value);
//            System.out.println("======== DEBUG ====== setRegistrationReminder " + value );
        }catch(Exception ex){
	    // FIXME: Log trace instead
            ex.printStackTrace();
        }
    }
    
    
    /**
     *	<p> This handler set the Registration Status to Don't Ask </p>
     *
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="setRegistrationDontAsk")
    public static void setRegistrationDontAsk(HandlerContext handlerCtx) {
        setRegistrationReminder(RegistrationReminder.DONT_ASK_FOR_REGISTRATION);
	GuiUtil.setSessionValue("dontAskRegistrationInThisSession", Boolean.TRUE);
    }
    
    /**
     *	<p> This handler set the Registration Status to Remind Later </p>
     *
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="setRegistrationRemindLater")
    public static void setRegistrationRemindLater(HandlerContext handlerCtx) {
        setRegistrationReminder(RegistrationReminder.REMIND_LATER);
	GuiUtil.setSessionValue("dontAskRegistrationInThisSession", Boolean.TRUE);
    }


    private static RegistrationService getRegistrationService() {
        try {
            File registryFile = RegistrationUtil.getServiceTagRegistry();
            /*
            Object params[] = new Object[] { registryFile }; 
            RegistrationServiceConfig config = new RegistrationServiceConfig("com.sun.enterprise.registration.SysnetRegistrationService", params);
	    RegistrationService registrationService = RegistrationServiceFactory.getInstance().getRegistrationService(config);            //
            return registrationService;
             */
            return new SysnetRegistrationService(registryFile);
	} catch (Exception ex) {
	    // FIXME: Log trace instead
	    ex.printStackTrace();
            return null;
	}
    }
    
    private static RegistrationService getRegistrationService(String proxyHost, int proxyPort) {
        try {
            
            File registryFile = RegistrationUtil.getServiceTagRegistry();
            return new SysnetRegistrationService(registryFile, proxyHost, proxyPort); 
            /*
            Object params[] = new Object[] { registryFile, proxyHost, proxyPort }; 
            RegistrationServiceConfig config = new RegistrationServiceConfig("com.sun.enterprise.registration.SysnetRegistrationService", params);
	    RegistrationService registrationService = RegistrationServiceFactory.getInstance().getRegistrationService(config);
            return registrationService;
            */
	} catch (Exception ex) {
	    // FIXME: Log trace instead
	    ex.printStackTrace();
            return null;
	}
    }

    /**
     *	<p> This handler returns true if we should ask user to register  </p>
     *
     *  <p> Output value: "askRegistration" -- Type: <code>Boolean</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="askRegistration",
    input={
        @HandlerInput(name="cookie", type=String.class),
        @HandlerInput(name="thisSession", type=String.class)},
    output={
        @HandlerOutput(name="askRegistration", type=Boolean.class)})
    public static void askRegistration(HandlerContext handlerCtx) {

        String thisSession = (String) handlerCtx.getInputValue("thisSession");
        //Don't ask anymore in this session.
        if (! GuiUtil.isEmpty(thisSession)) {
            handlerCtx.setOutputValue("askRegistration", false);
            return;
        }

        RegistrationReminder reminder = getRegistrationReminder();
        if (reminder == RegistrationReminder.DONT_ASK_FOR_REGISTRATION ) {
            GuiUtil.setSessionValue("dontAskRegistrationInThisSession", Boolean.TRUE);
            handlerCtx.setOutputValue("askRegistration", false);
            return;
        }
        
        if (reminder == RegistrationReminder.ASK_FOR_REGISTRATION ) {
            handlerCtx.setOutputValue("askRegistration", true);
            return;
        }

        String cookie = (String) handlerCtx.getInputValue("cookie");
        //app server has never registered, check if cookie expired.
        if (GuiUtil.isEmpty(cookie) || cookie.equals("null")) {
            handlerCtx.setOutputValue("askRegistration", true);
	} else {
            GuiUtil.setSessionValue("dontAskRegistrationInThisSession", Boolean.TRUE);
            handlerCtx.setOutputValue("askRegistration", false);
        }

    }

    

    @Handler(id="registerAS",
    input={
        @HandlerInput(name="userName", type=String.class, required=true),
        @HandlerInput(name="pswd", type=String.class, required=true),
        @HandlerInput(name="proxy", type=String.class),
        @HandlerInput(name="port", type=String.class),
        @HandlerInput(name="emailAdr", type=String.class, required=true),
        @HandlerInput(name="newPswd", type=String.class, required=true),
        @HandlerInput(name="screenName", type=String.class),
        @HandlerInput(name="firstName", type=String.class),
        @HandlerInput(name="lastName", type=String.class),
        @HandlerInput(name="companyName", type=String.class),
        @HandlerInput(name="country", type=String.class, required=true),
        @HandlerInput(name="accountStatus", type=String.class, required=true),
        @HandlerInput(name="newProxy", type=String.class),
        @HandlerInput(name="newPort", type=String.class)
    })
    public static void registerAS(HandlerContext handlerCtx) {

        String accountStatus = (String) handlerCtx.getInputValue("accountStatus");
        if ("hasAccount".equals(accountStatus)) {
            String userName = (String) handlerCtx.getInputValue("userName");
            String pswd = (String) handlerCtx.getInputValue("pswd");

            Map map = new HashMap();
            map.put(RegistrationAccount.USERID, userName);
            map.put(RegistrationAccount.PASSWORD, pswd);

            Object[] accountParams = { map };
            try {
                /* TODO-V3
                RegistrationAccountConfig accountConfig =
                    new RegistrationAccountConfig("com.sun.enterprise.registration.SOAccount", accountParams);
                RegistrationAccount account =
                    RegistrationAccountFactory.getInstance().getRegistrationAccount(accountConfig);
                 */

                RegistrationAccount account = new SOAccount(map);
                String proxy = (String) handlerCtx.getInputValue("proxy");
                String port = (String) handlerCtx.getInputValue("port");
                RegistrationService regService = getRegServiceForRegister(proxy, port);
                if (regService == null){   // This shouldn't happen, error may occur only when we try to user this regService.
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("reg.error.noRegService"));
                    return;
                }
                regService.register(account);
            } catch (Exception ex) {
		// FIXME: Log trace instead
                ex.printStackTrace();
                GuiUtil.handleException(handlerCtx, ex);
                return;
            }
        } else {
            String emailAdr = (String) handlerCtx.getInputValue("emailAdr");
            String newPswd = (String) handlerCtx.getInputValue("newPswd");
            String screenName = (String) handlerCtx.getInputValue("screenName");
            String firstName = (String) handlerCtx.getInputValue("firstName");
            String lastName = (String) handlerCtx.getInputValue("lastName");
            String companyName = (String) handlerCtx.getInputValue("companyName");
            String country = (String) handlerCtx.getInputValue("country");

            Map map = new HashMap();
            map.put(RegistrationAccount.EMAIL, emailAdr);
            map.put(RegistrationAccount.PASSWORD, newPswd);
            map.put(RegistrationAccount.COUNTRY, country);
            
            if (GuiUtil.isEmpty(screenName)){
                screenName = emailAdr;
            }
            map.put(RegistrationAccount.USERID, screenName);
            
            if (GuiUtil.isEmpty(firstName)) {
                firstName=" ";
            }
            map.put(RegistrationAccount.FIRSTNAME, firstName);
            
            if (GuiUtil.isEmpty(lastName)) {
                lastName = " ";
            }
            map.put(RegistrationAccount.LASTNAME, lastName);
            
            if (! GuiUtil.isEmpty(companyName))
                //map.put(RegistrationAccount.COMPANY,  companyName);
                map.put("company",  companyName);
//System.out.println("====== DEBUG ====  Creating account with the following: " + map.toString());
            Object[] accountParams = { map };
            try {
                String proxy = (String) handlerCtx.getInputValue("newProxy");
                String port = (String) handlerCtx.getInputValue("newPort");
                RegistrationService regService = getRegServiceForRegister(proxy, port);
                if (regService == null){
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("reg.error.noRegService"));
                    return;
                }
                /* TODO-V3
                 * Siraj will have to fix the class loader issue
                RegistrationAccountConfig accountConfig =
                    new RegistrationAccountConfig("com.sun.enterprise.registration.SOAccount", accountParams);
                RegistrationAccount account =
                    RegistrationAccountFactory.getInstance().getRegistrationAccount(accountConfig);
                */
                RegistrationAccount account = new SOAccount(map);
                regService.createRegistrationAccount(account);
                regService.register(account);
            } catch(Exception ex) {
		// FIXME: Log trace instead
                ex.printStackTrace();
                GuiUtil.handleException(handlerCtx, ex);
                return;
            }
        }
        
    }
    
    
    private static RegistrationService getRegServiceForRegister(String proxy, String port){
        if (!GuiUtil.isEmpty(proxy) && !GuiUtil.isEmpty(port)){
            return  getRegistrationService(proxy,  Integer.parseInt(port));
        }else{
            return getRegistrationService();
        }
    }
    
    

    @Handler(id="getCountryListForRegistration",
     output={
        @HandlerOutput(name="labels", type=List.class),
        @HandlerOutput(name="values", type=List.class)
     })
    public static void getCountryListForRegistration(HandlerContext handlerCtx) {

        
        List lab = new ArrayList();
        lab.add("United States");
        lab.add("Albania");
        
        handlerCtx.setOutputValue("labels", lab );
        handlerCtx.setOutputValue("values", lab );
        Locale locale = com.sun.jsftemplating.util.Util.getLocale(FacesContext.getCurrentInstance());
        RegistrationService regService = getRegistrationService();
        List countryList = regService.getAvailableCountries(locale);
        handlerCtx.setOutputValue("labels", countryList.get(0) );
        handlerCtx.setOutputValue("values", countryList.get(1) );
    }

    
    /*
     *	<p> This method will return a List of 2 lists.  The first list is the
     *      country list of the specified locale. The second list is the country
     *      list in _en locale that the sysnet backend is expecting.  
     *       
     *	@param	Locale	loccale  locale of the list to be displayed
     *
     *	@return	List<List>  country list. First elemnt is in the sepcified local,
     *                      second is _en locale that backend expects. 
     */
    /* This is not needed since the RegService provides the API.
    static public List<List> getCountryList(Locale locale){
        ResourceBundle bundle = ResourceBundleManager.getInstance().getBundle("com.sun.enterprise.tools.admingui.resources.Country", locale);
        String cts =  bundle.getString("COUNTRY_LIST_TOTAL_COUNT");

        int count = Integer.parseInt(cts);
        List displayList = new ArrayList();
        List actualList = new ArrayList();
        for(int i=1; i<count+1; i++) {
            displayList.add( bundle.getString("COUNTRY-"+i) );
            actualList.add(bundle.getString("en_COUNTRY-"+i));
        }
        List ret = new ArrayList(2);
        ret.add(0, displayList);
        ret.add(1, actualList);
        return ret;
    }
     */
               
    @Handler(id="getSupportImages",
        input={
            @HandlerInput(name="count", type=Integer.class, defaultValue="5")
        },
     output={
        @HandlerOutput(name="imageList", type=List.class)
        })
    public static void getSupportImages(HandlerContext handlerCtx) {
        int maxImageCount = 25+1; //the # of images we have under images/square
        List result = new ArrayList();
        int cnt = ((Integer) handlerCtx.getInputValue("count")).intValue();
        Random random = new Random();
        for(int i=0; i < cnt; i++){
            for(;;){
                int num = Math.abs(random.nextInt() % maxImageCount);
                String imgName="square-"+num+".gif";
                if (! result.contains(imgName)){
                    result.add(imgName);
                    break;
                }
            }
        }
        handlerCtx.setOutputValue("imageList", result);       
    }

    @Handler(id="getIssueQueryString",
     output={
        @HandlerOutput(name="query", type=String.class)
        })
    public static void getIssueQueryString(HandlerContext handlerCtx)
    {
        Calendar current = new GregorianCalendar();
        current.add(Calendar.HOUR,  -168);
        int month = Integer.parseInt(""+current.get(Calendar.MONTH)) + 1;
        String startTime=""+current.get(Calendar.YEAR)+"-"+month+"-"+current.get(Calendar.DAY_OF_MONTH);
        String query = "https://glassfish.dev.java.net/issues/buglist.cgi?component=glassfish&issue_status=RESOLVED&chfield=issue_status&chfieldto=Now&cmdtype=doit&chfieldfrom="+startTime;
        handlerCtx.setOutputValue("query", query);       

    }

    
    @Handler(id="getProductInstanceURN")
    public static void getProductInstanceURN(HandlerContext handlerCtx)
    {
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
        //Ensure this method is called once per session
        String productInstanceURN = (String) sessionMap.get("productInstanceURN");
        if (!GuiUtil.isEmpty(productInstanceURN )){
            //System.out.println(" !!!!! productInstanceURN="+ productInstanceURN);
            return;
        }
        try{
            String urn = RegistrationUtil.getGFProductURN();
            sessionMap.put("productInstanceURN", urn);
            //System.out.println("getGFProductURN returns  " + urn);
        }catch(Exception ex){
            System.out.println("!!!!!! Cannot get ProductURN, set to '0000' ");
            sessionMap.put("productInstanceURN", "0000");
            ex.printStackTrace();
        }
        
        
    }
        

}
