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

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.pkg.client.Image;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.LicenseAction;
import com.sun.pkg.client.Manifest;
import com.sun.pkg.client.SystemInfo;
import com.sun.pkg.client.Version;
import java.util.Properties;


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
            GuiUtil.getMessage("updateCenter.NoImageDirectory") : image.getRootDirectory());
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
                details.put("category", manifest.getAttribute(CATEGORY));
                details.put("bytes", "" + manifest.getPackageSize() );
                details.put("pkgSize", getPkgSize(manifest));
                details.put("desc", manifest.getAttribute(DESC_LONG));
            }
            
        }catch(Exception ex){
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("details", details);
        
    }
    
    
    @Handler(id="getUcList",
    	input={
        @HandlerInput(name="state", type=String.class, required=true )},
        output={
        @HandlerOutput(name="result", type=java.util.List.class)})
    public static void getUcList(HandlerContext handlerCtx) {
        
        List result = new ArrayList();
        try {
            Image img = getUpdateCenterImage();
            if (img == null){
                handlerCtx.setOutputValue("result", result);
                return;
            }
            String state= (String)handlerCtx.getInputValue("state");	

            List<Fmri> displayList = null;
            if (state.equals("installed"))
                displayList = getInstalledList(img);
            else
            if (state.equals("addOn"))
                displayList = getAddOnList(img);
            else
                displayList = getUpdateList(img);
            
            
            for (Fmri fmri : displayList){
                Map oneRow = new HashMap();
                try{
                    Manifest manifest = img.getManifest(fmri);
                    oneRow.put("selected", false);
                    oneRow.put("fmri", fmri);
                    oneRow.put("fmriStr", fmri.toString());
                    putInfo(oneRow, "pkgName", fmri.getName());
                    putInfo(oneRow, "version", getPkgVersion(fmri.getVersion()));
                    putInfo(oneRow, "category", manifest.getAttribute(CATEGORY));
                    putInfo(oneRow, "pkgSize", getPkgSize(manifest));
                    oneRow.put( "size", Integer.valueOf(manifest.getPackageSize()));
                    putInfo(oneRow, "auth", fmri.getAuthority());
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
    
//    private static List<Fmri> getAddOnList(Image image){
//        ArrayList<String> allFmriName = new ArrayList();
//        Catalog catalog = image.getCatalog();
//        List<Fmri> allList = catalog.getFmris();
//        for(Fmri each : allList){
//            if (allFmriName.contains(each.getName()))
//                continue;
//            allFmriName.add(each.getName());
//        }
//        List<Image.FmriState> installedList = image.getInventory(null, false);
//        for (Image.FmriState fs : installedList){
//            if (allFmriName.contains(fs.fmri.getName())){
//                allFmriName.remove(fs.fmri.getName());
//            }
//        }
//        List<Fmri> result = new ArrayList();
//        for(String eachAddOn : allFmriName){
//            result.add(catalog.getMatchingFmri(eachAddOn));
//             }
//        return result;
//    }
    
    private static List<Fmri> getAddOnList(Image image){
        List<String> installed = new ArrayList<String>();
        for (Image.FmriState each : image.getInventory(null, false)) {
            installed.add(each.fmri.getName());
        }
        List<Fmri> result = new ArrayList();
        for (Image.FmriState each : image.getInventory(null, true)) {
            if (!each.upgradable && !each.installed &&
                    !installed.contains(each.fmri.getName())) {
                result.add(each.fmri);
            }
        }
        return result;
    }

   
    private static List<Fmri> getUpdateList(Image image){
        List<Image.FmriState> fList = image.getInventory(null, false);
        ArrayList<Fmri> result = new ArrayList();
        for(Image.FmriState fs: fList){
            if (fs.upgradable)
                result.add(fs.fmri);
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
            }else{
                image.uninstallPackages(fList);
            }
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
        @HandlerOutput(name="license", type=String.class)})
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
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
            ex.printStackTrace();
        }
     }
        
     
     //returns -1 for any error condition, otherwise the #of component that has update available.
     @Handler(id="getUpdateComponentCount",
        output={
        @HandlerOutput(name="count", type=Integer.class)})
    public static void getUpdateComponentCount(HandlerContext handlerCtx) {
         int count = -1;
         try{
            Image image = getUpdateCenterImage();
            List<Fmri>updateList =  getUpdateList(image);
            count = updateList.size();
         }catch(Exception ex){
            System.out.println("!!!!!!!!! error in getting update component list");
            System.out.println(ex.getMessage());
         }
         handlerCtx.setOutputValue("count", Integer.valueOf(count));
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
        String sizep = (size <= MB) ? 
            size/1024 + GuiUtil.getMessage("org.glassfish.updatecenter.admingui.Strings", "sizeKB") :
            size/MB + GuiUtil.getMessage("org.glassfish.updatecenter.admingui.Strings", "sizeMB")  ;
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
        if (GuiUtil.getSessionValue("_uc_Image_Error") != null)
            return null;
        Image image = (Image) GuiUtil.getSessionValue("_uc_Image");
        String installDir = AMXRoot.getInstance().getDomainRoot().getInstallDir(); //this will only give the glassfish installation. need to get its parent for UC info
        String ucDir = (new File (installDir)).getParent();
        if (ucDir == null) ucDir = installDir;
        if ( image == null ){
            try{        
                image = new Image (new File (ucDir));
                GuiUtil.setSessionValue("_uc_Image", image);
            }catch(Exception ex){
                System.out.println("!!! Cannot create update center Image for " + ucDir );
                ex.printStackTrace();
                try{
                    image = new Image (new File (installDir));
                    GuiUtil.setSessionValue("_uc_Image", image);
                }catch(Exception ex1){
                    GuiUtil.setSessionValue("_uc_image_Error", "Cannot create update center Image");
                    System.out.println("!!! Cannot create update center Image for " +  installDir);
                    ex1.printStackTrace();
                    return null;
                }
            }
        }
        return image;
    }
    
    
//    @Handler(id="testUCAPI",
//    output={
//        @HandlerOutput(name="installedList", type=String.class)})
//        public static void testUCAPI(HandlerContext handlerCtx) {
//        
//        File dir = new File("/Users/anilam/Sun/v3/glassfishv3-express-0709-pb14");
//        try{
//            Image img = new Image(dir);
//            java.util.List<Fmri> listFmri = img.getInventory();
//            for( Fmri one : listFmri ){
//                System.out.println(" Fmri Name = " + one.getName() + 
//                        ";  URLPath = " + one.getURLPath() + 
//                        ";  Version = " + one.getVersion());
//            }
//            System.out.println("!!!!!!!!!!! ========================  getInventory");
//            List<Image.FmriState> list2 = img.getInventory(null, false);
//            for (Image.FmriState fs : list2){
//                Fmri fmri = fs.fmri;
//                System.out.println("NAME = " + fmri.getName() +
//                        ";  VERSION = " + fmri.getVersion());
//            }
//            
//            String pkgs[] = { "jmaki" };
//            img.uninstallPackages(pkgs);
//            
//            System.out.println("After un-installation ---------");
//            List<Image.FmriState> list3 = img.getInventory(null, false);
//
//            for (Image.FmriState fs : list3){
//                Fmri fmri = fs.fmri;
//                System.out.println("NAME = " + fmri.getName() +
//                        ";  VERSION = " + fmri.getVersion());
//            }
//            
//
//            
//        }catch(Exception ex){
//            System.out.println("!!!!!!!  cannot create Image") ;
//        }
//    }
//    
    final private static String CATEGORY = "info.classification";
    final private static String DESC_LONG = "description_long";
    final private static int MB = 1024*1024;
}
