/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.security.admingui.handlers;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AuditModuleConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  

import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;


/**
 *
 * @author anilam
 */
public class SecurityHandlers {
    
    
/**
     *	<p> This handler returns the values for all the attributes in 
     *      Security Settings Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Audit"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Principal"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Password"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RoleMapping"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Mapped"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realm"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Module"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jacc"  -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getSecuritySettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },        
    output={
        @HandlerOutput(name="Audit",      type=Boolean.class),
        @HandlerOutput(name="Principal",  type=String.class),
        @HandlerOutput(name="Password",    type=String.class),
        @HandlerOutput(name="RoleMapping",       type=Boolean.class),
        @HandlerOutput(name="Mapped", type=String.class),
        @HandlerOutput(name="Realm", type=String.class),
        @HandlerOutput(name="Module", type=String.class),
        @HandlerOutput(name="Jacc", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})
        
        public static void getSecuritySettings(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        String principal = sConfig.getDefaultPrincipal();
        String password = sConfig.getDefaultPrincipalPassword();
        String mapped = sConfig.getMappedPrincipalClass();
        String realm = sConfig.getDefaultRealm();
        String module = sConfig.getAuditModules();
        String jacc = sConfig.getJACC();
        handlerCtx.setOutputValue("Audit", sConfig.getAuditEnabled());
        handlerCtx.setOutputValue("Principal", principal);
        handlerCtx.setOutputValue("Password", password);
        handlerCtx.setOutputValue("RoleMapping", sConfig.getActivateDefaultPrincipalToRoleMapping());
        handlerCtx.setOutputValue("Mapped", mapped);
        handlerCtx.setOutputValue("Realm", realm);
        handlerCtx.setOutputValue("Module", module);
        handlerCtx.setOutputValue("Jacc", jacc);
        handlerCtx.setOutputValue("Properties", sConfig.getPropertyConfigMap());
        
    }    
    
/**            
     *	<p> This handler saves the values for Security </p>
      *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
      *	<p> Input value: "Audit"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Principal"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Password"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RoleMapping"        -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Mapped"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jaccs"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Modules"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Realms"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SecurityManager"     -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveSecuritySettings",
    input={
       @HandlerInput(name="ConfigName",      type=String.class, required=true),        
        @HandlerInput(name="Audit",      type=String.class),
        @HandlerInput(name="Principal",  type=String.class),
        @HandlerInput(name="Password",    type=String.class),
        @HandlerInput(name="RoleMapping",       type=String.class),
        @HandlerInput(name="Mapped", type=String.class),
        @HandlerInput(name="Jaccs",      type=String.class),
        @HandlerInput(name="Modules",    type=String.class),
        @HandlerInput(name="Realms",      type=String.class),
        @HandlerInput(name="newProps", type=Map.class)
        })
    public static void saveSecuritySettings(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        AMXUtil.updateProperties( sConfig, (Map)handlerCtx.getInputValue("newProps"));
        sConfig.setAuditEnabled((String)handlerCtx.getInputValue("Audit"));
        sConfig.setDefaultPrincipal(((String)handlerCtx.getInputValue("Principal")));
        sConfig.setDefaultPrincipalPassword(((String)handlerCtx.getInputValue("Password")));
        sConfig.setActivateDefaultPrincipalToRoleMapping((String)handlerCtx.getInputValue("RoleMapping"));
        sConfig.setMappedPrincipalClass(((String)handlerCtx.getInputValue("Mapped")));
        sConfig.setJACC((String)handlerCtx.getInputValue("Jaccs"));
        sConfig.setAuditModules((String)handlerCtx.getInputValue("Modules"));
        sConfig.setDefaultRealm((String)handlerCtx.getInputValue("Realms"));
        
    }    
    
    
    /**
     *	<p> This handler returns the value for Security Manager in 
     *      Security Settings </p>
     *	<p> Input value: "objectName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "securityManagerEnabled"   -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "getSecurityManagerStatus", input = {
    @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
    @HandlerOutput(name = "securityManagerEnabled", type = Boolean.class)
    })
    public static void getSecurityManagerStatus(HandlerContext handlerCtx) {

        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        handlerCtx.setOutputValue("securityManagerEnabled", isSecurityManagerEnabled(config));
    }

    
    /**            
     *	<p> This handler saves the value for Security Manager</p>
     *	<p> Input value: "ObjectName"       -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "SecurityManager"   -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    
    @Handler(id="saveSecurityManagerStatus",
    input={
       @HandlerInput(name="ConfigName",      type=String.class, required=true),        
       @HandlerInput(name="SecurityManagerStatus",      type=String.class, required=true)
    })
    public static void saveSecurityManagerStatus(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        String value = (String)handlerCtx.getInputValue("SecurityManagerStatus");

        Boolean status = isSecurityManagerEnabled(config);
        Boolean userValue = new Boolean(value);
        if (status.equals(userValue)){
            //no need to change
            return;
        }
        
        ArrayList newOptions = new ArrayList();
        final JavaConfig javaConfig = config.getJavaConfig();
        String[] origOptions = javaConfig.getJVMOptions();
        if(userValue){
            for(int i=0; i<origOptions.length; i++){
                newOptions.add(origOptions[i]);
            }
            newOptions.add(JVM_OPTION_SECURITY_MANAGER);
        }else{
            for(int i=0; i<origOptions.length; i++){
                if (! (origOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                        origOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL))){
                   newOptions.add(origOptions[i]);
                }
            }
        }
        String[] jvmOptions = (String[])newOptions.toArray(new String[0]);
        javaConfig.setJVMOptions(jvmOptions);
    }    
    
   /**
     *	<p> This handler returns the DEFAULT values for all the attributes in the
     *      Security Settings Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Audit" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Principal" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Password" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RoleMapping" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Mapped" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaccs" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Modules" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realms" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "SecurityManager" -- Type: <code>java.lang.Boolean</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getSecurityDefaultAttributes",
	input={
	    @HandlerInput(name="ConfigName", type=String.class, required=true)},      
    output={
        @HandlerOutput(name="Audit",     type=Boolean.class),
        @HandlerOutput(name="Principal", type=String.class),
        @HandlerOutput(name="Password", type=String.class),
        @HandlerOutput(name="RoleMapping", type=Boolean.class),
        @HandlerOutput(name="Mapped", type=String.class),
        @HandlerOutput(name="Jaccs", type=String.class),
        @HandlerOutput(name="Modules", type=String.class),
        @HandlerOutput(name="Realms", type=String.class),
        @HandlerOutput(name="SecurityManager", type=String.class)})     
        public static void getSecurityDefaultAttributes(HandlerContext handlerCtx) {
        
        Map <String,String> defaultMap = AMXRoot.getInstance().getResourcesConfig().getDefaultValues(XTypes.SECURITY_SERVICE_CONFIG); 
        handlerCtx.setOutputValue("Audit", defaultMap.get("AuditEnabled"));
        handlerCtx.setOutputValue("Principal", defaultMap.get("DefaultPrincipal"));
        handlerCtx.setOutputValue("Password", defaultMap.get("DefaultPrincipalPassword"));
        handlerCtx.setOutputValue("RoleMapping", defaultMap.get("ActivateDefaultPrincipalToRoleMapping"));
        handlerCtx.setOutputValue("Mapped", defaultMap.get("MappedPrincipalClass"));
        handlerCtx.setOutputValue("Jaccs", defaultMap.get("JACC")) ;
        handlerCtx.setOutputValue("Modules", defaultMap.get("AuditModules"));
        handlerCtx.setOutputValue("Realms", defaultMap.get("DefaultRealm"));
    }
    
    /**
     *	<p> This handler returns dropdown values for Default Realm
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realms" -- Type: <code>java.util.Array</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRealms",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="Realms", type=java.util.List.class)})

        public static void getRealms(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuthRealmConfig>realmsMap = sConfig.getAuthRealmConfigMap();
        List realms = new ArrayList (realmsMap.keySet());
        handlerCtx.setOutputValue("Realms", realms);
    }
    
    
    /**
     *	<p> This handler returns the values for Audit Modules
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AuditModules" -- Type: <code>java.util.Array</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getAuditModules",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="AuditModules", type=java.util.List.class)})

        public static void getAuditModules(HandlerContext handlerCtx) {
        
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuditModuleConfig>modules = sConfig.getAuditModuleConfigMap();
        List modulesMap = new ArrayList (modules.keySet());
        handlerCtx.setOutputValue("AuditModules", modulesMap);
    }
    
    
   private static Boolean isSecurityManagerEnabled(ConfigConfig config){
       
        String[] jvmOptions = config.getJavaConfig().getJVMOptions();
        if (jvmOptions != null && (jvmOptions.length > 0)){
            for(int i=0; i< jvmOptions.length; i++){
                if (jvmOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                        jvmOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL)){
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
   
    private static final String JVM_OPTION_SECURITY_MANAGER = "-Djava.security.manager";
    private static final String JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL = "-Djava.security.manager=";    

}
