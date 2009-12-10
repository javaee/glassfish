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
 *
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.uc.admingui;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.pkg.client.Image;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.LicenseAction;
import com.sun.pkg.client.Manifest;
import com.sun.pkg.client.SystemInfo;
import com.sun.pkg.client.SystemInfo.UpdateCheckFrequency;
import com.sun.pkg.client.Version;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMXUtil;



/**
 *
 * @author anilam
 */
public class UpdateCenterHandlers {
    
    
//    getInstalledPath(result=>$attribute{installedPath});
//        getAuthority(result=>$attribute{authority});
//        getUcList(state="update", result=>$attribute{listOfRows} );
//        updateCenterProcess(selectedRows="${selectedRows}" action="$attribute{action}" );

    @Handler(id="getInstalledPath",
        output={
        @HandlerOutput(name="result", type=String.class)})
    public static void getInstalledPath(HandlerContext handlerCtx) {
        Image image = getUpdateCenterImage();
        handlerCtx.setOutputValue("result",  (image == null) ? 
            GuiUtil.getMessage(BUNDLE, "updateCenter.NoImageDirectory") : image.getRootDirectory());
    }
    
    
    @Handler(id="getAuthority",
        output={
        @HandlerOutput(name="result", type=String.class)})
    public static void getAuthority(HandlerContext handlerCtx) {
        Image image = getUpdateCenterImage();
        handlerCtx.setOutputValue("result",  (image == null) ? "" : image.getPreferredAuthorityName());
    }
    
    
    @Handler(id="getPkgDetailsInfo",
    	input={
        @HandlerInput(name="fmriStr", type=String.class, required=true ),
        @HandlerInput(name="auth", type=String.class, required=true )},
        output={
        @HandlerOutput(name="details", type=java.util.Map.class)})
    public static void getPkgDetailsInfo(HandlerContext handlerCtx) {
        String fmriStr = (String)handlerCtx.getInputValue("fmriStr");
        //Called by the intiPage and don't need to process.  When we can use beforeCreate to do this, we can remove this check.
        if (fmriStr == null){
            handlerCtx.setOutputValue("details", new HashMap());
            return;
        }
        Fmri fmri = new Fmri(fmriStr);
        Map details = new HashMap();  
        Image img = getUpdateCenterImage();
        try{
            details.put("pkgName", fmri.getName());
            details.put("uid", fmri.toString());
            details.put("version", getPkgVersion(fmri.getVersion()));
            details.put("date", fmri.getVersion().getPublishDate());
            details.put("auth", (String) handlerCtx.getInputValue("auth"));
            details.put("url", fmri.getURLPath());
            if (img != null){
                Manifest manifest = img.getManifest(fmri);
                details.put("category", getCategory(manifest));
                details.put("bytes", "" + manifest.getPackageSize() );
                details.put("pkgSize", getPkgSize(manifest));
                // look for description in the following order:
                // pkg.description, description_long, pkg.summary, description
                // since description_long and description has been deprecated.
                String desc = manifest.getAttribute(PKG_DESC);
                if (GuiUtil.isEmpty(desc)){
                    desc = manifest.getAttribute(DESC_LONG);
                    if (GuiUtil.isEmpty(desc)){
                        desc = manifest.getAttribute(PKG_SUMMARY);
                        if (GuiUtil.isEmpty(desc))
                            desc = manifest.getAttribute(DESC);
                    }
                }
                details.put("desc", desc);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("details", details);
        
    }

    private static String  getCategory(Manifest manifest){
        String attr = manifest.getAttribute(CATEGORY);
        //attr is of the form  scheme:catgory   refer to issue# 8494.
        int index = attr.indexOf(":");
        return (index==-1) ? attr : attr.substring(index+1);
    }
    
    @Handler(id="getUcList",
    	input={
        @HandlerInput(name="state", type=String.class, required=true )},
        output={
        @HandlerOutput(name="result", type=java.util.List.class)})
    public static void getUcList(HandlerContext handlerCtx) {
        
        GuiUtil.setSessionValue(USER_OK, Boolean.TRUE);
        List result = new ArrayList();
        try {
            Image img = getUpdateCenterImage();
            if (img == null){
                handlerCtx.setOutputValue("result", result);
                return;
            }
            String state= (String)handlerCtx.getInputValue("state");
            if (state.equals("update")){
                handlerCtx.setOutputValue("result", getUpdateDisplayList(img));
                return;
            }
            
            List<Fmri> displayList = null;
            if (state.equals("installed"))
                displayList = getInstalledList(img);
            else
            if (state.equals("addOn"))
                displayList = getAddOnList(img);
            
            for (Fmri fmri : displayList){
                Map oneRow = new HashMap();
                try{
                    Manifest manifest = img.getManifest(fmri);
                    oneRow.put("selected", false);
                    oneRow.put("fmri", fmri);
                    oneRow.put("fmriStr", fmri.toString());
                    putInfo(oneRow, "pkgName", fmri.getName());
                    putInfo(oneRow, "version", getPkgVersion(fmri.getVersion()));
                    putInfo(oneRow, "newVersion", "");
                    putInfo(oneRow, "category", getCategory(manifest));
                    putInfo(oneRow, "pkgSize", getPkgSize(manifest));
                    oneRow.put( "size", Integer.valueOf(manifest.getPackageSize()));
                    putInfo(oneRow, "auth", fmri.getAuthority());
                    String tooltip = manifest.getAttribute(PKG_SUMMARY);
                    if (GuiUtil.isEmpty(tooltip))
                        tooltip = manifest.getAttribute(DESC);
                    putInfo(oneRow, "tooltip", tooltip);
                    result.add(oneRow);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    
    @Handler(id="getAuthList",
        output={
        @HandlerOutput(name="result", type=java.util.List.class)})
    public static void getAuthList(HandlerContext handlerCtx) {
        
        List result = new ArrayList();
        try {
            Image image = getUpdateCenterImage();
            if (image == null){
                handlerCtx.setOutputValue("result", result);
                return;
            }
            String[] auths = image.getAuthorityNames();
            for(int i=0; i< auths.length; i++){
                Map oneRow = new HashMap();
                    oneRow.put("authName", auths[i]);
                    result.add(oneRow);
            }
        }catch(Exception ex1){
            ex1.printStackTrace();
        }
        handlerCtx.setOutputValue("result", result);
    }
    
    
    @Handler(id="getProxyInfo",
        output={
        @HandlerOutput(name="connection", type=String.class),
        @HandlerOutput(name="host", type=String.class),
        @HandlerOutput(name="port", type=String.class)}
        )
    public static void getProxyInfo(HandlerContext handlerCtx) {
        
        Proxy proxy = SystemInfo.getProxy();
        if (proxy != null){
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            if (address != null){
                handlerCtx.setOutputValue("connection", "useProxy");
                handlerCtx.setOutputValue("host", address.getHostName());
                handlerCtx.setOutputValue("port", address.getPort());
                return;
            }
        }
        handlerCtx.setOutputValue("connection", "direct");
        handlerCtx.setOutputValue("host", "");
        handlerCtx.setOutputValue("port", "");
    }
    
    @Handler(id="setProxyInfo",
        input={
        @HandlerInput(name="connection", type=String.class),
        @HandlerInput(name="host", type=String.class),
        @HandlerInput(name="port", type=String.class)}
        )
    public static void setProxyInfo(HandlerContext handlerCtx) {
        String connection = (String)handlerCtx.getInputValue("connection");
        String host = (String)handlerCtx.getInputValue("host");
        String port = (String)handlerCtx.getInputValue("port");
        try{
            Image image = getUpdateCenterImage();
            if (connection.equals("useProxy")){
                int portNo = Integer.parseInt(port);
                SocketAddress address = new InetSocketAddress(host, portNo);
                image.setProxy(new Proxy(Proxy.Type.HTTP, address));
                String url="http://"+host+":"+portNo;
                Properties prop = new Properties();
                prop.setProperty("proxy.URL", url);
                SystemInfo.initUpdateToolProps(prop);
            }else{
                image.setProxy(null);
                Properties prop = new Properties();
                prop.setProperty("proxy.URL", "");
                SystemInfo.initUpdateToolProps(prop);
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
                    
    
    private static void putInfo( Map oneRow, String key, String value){
        oneRow.put( key, GuiUtil.isEmpty(value) ? "" : value);
    }

    private static List<Fmri> getInstalledList(Image image){
        List<Image.FmriState> fList = image.getInventory(null, false);
        ArrayList<Fmri> result = new ArrayList();
        for(Image.FmriState fs: fList){
            result.add(fs.fmri);
        }
        return result;

    }
    
    private static List<Fmri> getAddOnList(Image image){
        List<String> installed = new ArrayList<String>();
        for (Image.FmriState each : image.getInventory(null, false)) {
            installed.add(each.fmri.getName());
        }
        String pAuth = image.getPreferredAuthorityName();
        Map<String, Fmri> pMap = new HashMap();
        List<Fmri> allList = new ArrayList();
        for (Image.FmriState each : image.getInventory(null, true)) {
            Fmri fmri = each.fmri;
            if (!each.upgradable && !each.installed &&
                    !installed.contains(fmri.getName())) {
                allList.add(fmri);
                if (fmri.getAuthority().equals(pAuth)){
                    pMap.put(fmri.getName(), fmri);
                }
            }
        }
        
        //If the package exist in different repo, only show the one thats from
        //the preferred repo.
        List result = new ArrayList();
        for(Fmri test: allList){
            if (pMap.get(test.getName()) == null){
                result.add(test);
                continue;
            }
            if (test.getAuthority().equals(pAuth)){
                result.add(test);
            }
        }
        return result;
    }
   
    private static List getUpdateDisplayList(Image image){
        List<Image.FmriState> installed = image.getInventory(null, false);
        Map<String, Fmri> updateListMap = new HashMap();
        List<String> nameList = new ArrayList();
        for(Image.FmriState fs: installed){
            if (fs.upgradable){
                Fmri fmri = fs.fmri;
                updateListMap.put(fmri.getName(),fmri);
                nameList.add(fmri.getName());
            }
        }
        List result = new ArrayList();
        String[] pkgsName = nameList.toArray(new String[nameList.size()]);
        try{
            Image.ImagePlan ip = image.makeInstallPlan(pkgsName);
            Fmri[] proposed = ip.getProposedFmris();
            for( Fmri newPkg : proposed){
                Map oneRow = new HashMap();
                try{
                    String name = newPkg.getName();
                    Fmri oldPkg = updateListMap.get(name);
                    Manifest manifest = image.getManifest(newPkg);
                    int changedSize = manifest.getPackageSize() - image.getManifest(oldPkg).getPackageSize();
                    oneRow.put("selected", false);
                    oneRow.put("fmri", newPkg);
                    oneRow.put("fmriStr", newPkg.toString());
                    putInfo(oneRow, "pkgName", name);
                    putInfo(oneRow, "newVersion", getPkgVersion(newPkg.getVersion()));
                    putInfo(oneRow, "version", getPkgVersion(oldPkg.getVersion()));
                    putInfo(oneRow, "category", getCategory(manifest));
                    putInfo(oneRow, "pkgSize", convertSizeForDispay(changedSize));
                    oneRow.put( "size", Integer.valueOf(changedSize));
                    putInfo(oneRow, "auth", newPkg.getAuthority());
                    String tooltip = manifest.getAttribute(PKG_SUMMARY);
                    if (GuiUtil.isEmpty(tooltip))
                        tooltip = manifest.getAttribute(DESC);
                    putInfo(oneRow, "tooltip", tooltip);
                    result.add(oneRow);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
       return result;
    }
    
    
    
    @Handler(id="updateCenterProcess",
    	input={
        @HandlerInput(name="action", type=String.class, required=true ),
        @HandlerInput(name="selectedRows", type=java.util.List.class, required=true )})
    public static void updateCenterProcess(HandlerContext handlerCtx) {
        Image image = getUpdateCenterImage();
        boolean install = false;
        String action= (String)handlerCtx.getInputValue("action");
        if (action.equals("install"))
            install=true;
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        if (obj == null){
            System.out.println("updateCenterProcess: No row selected for ");
            return;
        }
        List<Map> selectedRows = (List) obj;
        List<Fmri> fList = new ArrayList();
        try {
            for (Map oneRow : selectedRows) {
                fList.add((Fmri)oneRow.get("fmri"));
            }
            if (install){
                image.installPackages(fList);
                updateCountInSession(image);      //ensure the # of update component count is updated.
            }else{
                image.uninstallPackages(fList);
            }
            GuiUtil.setSessionValue("restartRequired", Boolean.TRUE);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
            ex.printStackTrace();
        }
    }
    
   // getLicenseText(selectedRows="#{selectedRows}" license=>$page{license});
     @Handler(id="getLicenseText",
    	input={
        @HandlerInput(name="selectedRows", type=java.util.List.class, required=true)},
        output={
        @HandlerOutput(name="license", type=String.class),
        @HandlerOutput(name="hasLicense", type=Boolean.class)})
    public static void getLicenseText(HandlerContext handlerCtx) {
         
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        Image image = getUpdateCenterImage();
        List<Map> selectedRows = (List) obj;
        try {
            StringBuffer allLicense = new StringBuffer();
            for (Map oneRow : selectedRows) {
                Fmri fmri = (Fmri)oneRow.get("fmri");
                allLicense.append(getLicense(image, fmri));
            }
            handlerCtx.setOutputValue("license", ""+allLicense);
            handlerCtx.setOutputValue("hasLicense", (allLicense.length() > 0));
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
            //ex.printStackTrace();
        }
     }
        
     
    //returns -1 for any error condition, otherwise the #of component that has update available.
    @Handler(id = "getUpdateComponentCount", output = {
        @HandlerOutput(name = "count", type = Integer.class)
    })
    public static void getUpdateComponentCount(HandlerContext handlerCtx) {
        Boolean userOK = (Boolean) GuiUtil.getSessionValue(USER_OK);
        if (userOK == null){
            UpdateCheckFrequency userPreference = SystemInfo.getUpdateCheckFrequency();
            boolean donotping = userPreference == UpdateCheckFrequency.NEVER;
            if(donotping){
                GuiUtil.getLogger().info("UpdateCheckFrequency is set to NEVER by user.  Component update count not performed. ");
                GuiUtil.setSessionValue(USER_OK, Boolean.FALSE);
                handlerCtx.setOutputValue("count", -1);
                return;
            }else{
                GuiUtil.setSessionValue(USER_OK, Boolean.TRUE);
            }
        }else{
            if (! userOK.booleanValue()){
                handlerCtx.setOutputValue("count", -1);
                return;
            } 
        }
        Integer countInt = (Integer) GuiUtil.getSessionValue(UPDATE_COUNT);
        if (countInt == null) {
            Image image = getUpdateCenterImage();
            countInt = updateCountInSession(image);
        }
        GuiUtil.getLogger().info("Update Component count = " + countInt);
        handlerCtx.setOutputValue("count", countInt);
    }

     
     private static Integer updateCountInSession(Image image){
         int count = 0;
         try{
            List<Image.FmriState> installed = image.getInventory(null, false);
            for(Image.FmriState fs: installed){
                if (fs.upgradable){
                    count++;
                }
            }
         }catch(Exception ex){
            count = -1;
            System.out.println("error in getting update component list");
            //System.out.println(ex.getMessage());
         }
         Integer countInt = Integer.valueOf(count);
         GuiUtil.setSessionValue(UPDATE_COUNT, countInt);
         return countInt ;
     }
        
     
    private static String getLicense(Image img, Fmri fmri){
        StringBuffer licenseText = new StringBuffer();
        try{
            Manifest manifest = img.getManifest(fmri);
            List<LicenseAction> lla = manifest.getActionsByType(LicenseAction.class);
            for (LicenseAction la : lla) {
                licenseText.append("============= ").append(la.getName()).append(" ================\n");
                licenseText.append(fmri.toString());
                licenseText.append("\n\n");
                licenseText.append(la.getText());
                licenseText.append("\n\n");
            }
            return "" + licenseText;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    private static String getPkgVersion(Version version){
        //The version format is release[,build_release]-branch:datetime, which is decomposed into three DotSequences and the datetime. 
        //eg. 2.4.4,0-8.724:20080612T135341Z
        
        String dotSequence = version.getRelease().toString();
        String branch = version.getBranch().toString();
        return GuiUtil.isEmpty(branch) ? dotSequence : dotSequence+"-"+branch; 
    }
    
    private static String getPkgSize(Manifest manifest){
        int size = manifest.getPackageSize();
        return convertSizeForDispay(size);
    }
    
    private static String convertSizeForDispay(int size){
        String sizep = (size <= MB) ?
            size/1024 + GuiUtil.getMessage(BUNDLE, "sizeKB") :
            size/MB + GuiUtil.getMessage(BUNDLE, "sizeMB")  ;
        return sizep;
    }
    
    private static String getPkgDate(Version version){
        //TODO localize the date format
        int begin = version.toString().indexOf(":");
        int end = version.toString().indexOf("T");
        String dateStr = version.toString().substring(begin+1, end);
        String result = dateStr.substring(0,4) + "/" + dateStr.substring(4,6) + "/" + dateStr.substring(6,8);
        return result;
        
    }
    
    
    private static Image getUpdateCenterImage(){
        String ucDir = (String) GuiUtil.getSessionValue(UCDIR);
        if (ucDir == null){
            String installDir = (String)V3AMXUtil.getInstallDir();
            //installDir will only give the glassfish installation. need to get its parent for UC info
            ucDir = (new File (installDir)).getParent();
            GuiUtil.setSessionValue(UCDIR, ucDir);
        }
        Image image = null;
        try{
            image = new Image (new File (ucDir));
            refreshCatalog(image);
        }catch(Exception ex){
            System.out.println("Cannot create update center Image for " + ucDir  + "; Update Center functionality will not be available in Admin Console ");
            //ex.printStackTrace();
        }
        return image;
    }
    
   
    private static synchronized void refreshCatalog (Image image){
        try{
            if (GuiUtil.getSessionValue(CATALOG_REFRESHED) == null){
                GuiUtil.setSessionValue(CATALOG_REFRESHED, "TRUE");
                image.refreshCatalogs();
            }
        }catch(Exception ex){
            System.out.println("Cannot refresh Catalog : " + ex.getMessage());
        }
    } 

    final private static String CATEGORY = "info.classification";
    final private static String DESC_LONG = "description_long";
    final private static String PKG_DESC = "pkg.description";
    final private static String PKG_SUMMARY = "pkg.summary";
    final private static String DESC = "description";
    final private static String UPDATE_COUNT = "__gui_uc_update_count";
    final private static String CATALOG_REFRESHED = "__gui_uc_catalog_refreshed";
    final private static String UCDIR = "__gui_uc_installation_dir";
    final private static String USER_OK = "__gui_uc_userok";
    final private static String BUNDLE = "org.glassfish.updatecenter.admingui.Strings";
    final private static int MB = 1024*1024;
    
}
