/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.updatecenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import org.glassfish.admingui.util.AMXRoot;
import org.glassfish.admingui.util.GuiUtil;

import com.sun.pkg.client.Image;
import com.sun.pkg.client.Fmri;
import com.sun.pkg.client.Catalog;
import com.sun.pkg.client.Version;



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
                HashMap oneRow = new HashMap();
                try{
                    oneRow.put("selected", false);
                    oneRow.put("fmri", fmri);
                    oneRow.put("pkgName", fmri.getName());
                    Version version = fmri.getVersion();
                    oneRow.put("version", getPkgVersion(version));
                    oneRow.put("pkgDate", getPkgDate(version));
                    //oneRow.put("rawDate", getRawDate(version));
                    oneRow.put("urlPath", fmri.getURLPath());
                    //System.out.println("NAME = " + fmri.getName() + ";  \nVERSION = " + fmri.getVersion() + "\nURLPath = " + fmri.getURLPath() );
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
    

    private static List<Fmri> getInstalledList(Image image){
        List<Image.FmriState> fList = image.getInventory(null, false);
        ArrayList<Fmri> result = new ArrayList();
        for(Image.FmriState fs: fList){
            result.add(fs.fmri);
        }
        return result;

    }
    
    private static List<Fmri> getAddOnList(Image image){
        ArrayList<String> allFmriName = new ArrayList();
        Catalog catalog = image.getCatalog();
        List<Fmri> allList = catalog.getFmris();
        for(Fmri each : allList){
            if (allFmriName.contains(each.getName()))
                continue;
            allFmriName.add(each.getName());
        }
        List<Image.FmriState> installedList = image.getInventory(null, false);
        for (Image.FmriState fs : installedList){
            if (allFmriName.contains(fs.fmri.getName())){
                allFmriName.remove(fs.fmri.getName());
            }
        }
        List<Fmri> result = new ArrayList();
        for(String eachAddOn : allFmriName){
            result.add(catalog.getMatchingFmri(eachAddOn));
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
            ex.printStackTrace();
        }
    }
    
    private static String getPkgVersion(Version version){
        //The version format is release[,build_release]-branch:datetime, which is decomposed into three DotSequences and the datetime. 
        //eg. 2.4.4,0-8.724:20080612T135341Z
        String verStr = version.toString();
        String verInfo = verStr.substring(0, verStr.indexOf(":"));
        String build = "";
        int commaIndex = verInfo.indexOf(",");
        int dashIndex = verInfo.indexOf("-");
        if (commaIndex == -1){
            build = verInfo.substring(0, dashIndex);
        }else
            build = verInfo.substring(0, commaIndex);
        String branch = verInfo.substring(dashIndex, verInfo.length() );
        return build + branch;
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
                try{
                    image = new Image (new File (installDir));
                    GuiUtil.setSessionValue("_uc_Image", image);
                }catch(Exception ex1){
                    GuiUtil.setSessionValue("_uc_image_Error", "Cannot create update center Image");
                    System.out.println("!!! Cannot create update center Image for " + ucDir + " or " + installDir);
                    return null;
                }
            }
        }
        return image;
    }
    
    
    @Handler(id="testUCAPI",
    output={
        @HandlerOutput(name="installedList", type=String.class)})
        public static void testUCAPI(HandlerContext handlerCtx) {
        
        File dir = new File("/Users/anilam/Sun/v3/glassfishv3-express-0709-pb14");
        try{
            Image img = new Image(dir);
            img.refreshCatalogs();
            System.out.println("image.getRootDirectory() = " + img.getRootDirectory());
            Catalog catalog = img.getCatalog();
            catalog.refresh();
            System.out.println ("cataglog size = " + catalog.size());
            java.util.List<Fmri> listFmri = catalog.getFmris();
            for( Fmri one : listFmri ){
                System.out.println(" Fmri Name = " + one.getName() + 
                        ";  URLPath = " + one.getURLPath() + 
                        ";  Version = " + one.getVersion());
            }
            System.out.println("!!!!!!!!!!! ========================  getInventory");
            List<Image.FmriState> list2 = img.getInventory(null, false);
            for (Image.FmriState fs : list2){
                Fmri fmri = fs.fmri;
                System.out.println("NAME = " + fmri.getName() +
                        ";  VERSION = " + fmri.getVersion());
            }
            
            String pkgs[] = { "jmaki" };
            img.uninstallPackages(pkgs);
            
            System.out.println("After un-installation ---------");
            List<Image.FmriState> list3 = img.getInventory(null, false);

            for (Image.FmriState fs : list3){
                Fmri fmri = fs.fmri;
                System.out.println("NAME = " + fmri.getName() +
                        ";  VERSION = " + fmri.getVersion());
            }
            

            
        }catch(Exception ex){
            System.out.println("!!!!!!!  cannot create Image") ;
        }
    }
    

}
