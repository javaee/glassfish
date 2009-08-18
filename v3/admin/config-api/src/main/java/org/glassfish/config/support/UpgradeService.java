package org.glassfish.config.support;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.api.admin.config.Property;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Startup service to update existing domain.xml to the latest expected format
 *
 * @author Jerome Dochez
 */
@Service
public class UpgradeService implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Domain domain;

    private static final String MODULE_TYPE = "moduleType";
    
    public void postConstruct() {
        upgradeApplicationElements();
    }

    private void upgradeApplicationElements() {
        upgradeV2ApplicationElements();
        upgradeV3PreludeApplicationElements();
    }

    private void upgradeV3PreludeApplicationElements() {
        // in v3-prelude, engines were created under application directly,
        // in v3 final, engines are placed under individual modules composing the application
        // so if we have engines under application and not modules deployed, we need to upgrade
        for (Application app : domain.getApplications().getModules(Application.class)) {
            if (app.getEngine()!=null && app.getEngine().size()>0 &&
                    (app.getModule()==null || app.getModule().size()==0)) {
                // we need to update the application declaration from v3 prelude,
                // we can safely assume this was a single module application
                try {
                    ConfigSupport.apply(new SingleConfigCode<Application>() {
                        public Object run(Application application) throws PropertyVetoException, TransactionFailure {
                            Module module = application.createChild(Module.class);
                            module.setName(application.getName());
                            for (Engine engine : application.getEngine()) {
                                module.getEngines().add(engine);
                            }
                            application.getModule().add(module);
                            application.getEngine().clear();
                            return null;
                        }
                    }, app);
                } catch(TransactionFailure tf) {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading application "
                            + app.getName() + " please redeploy", tf);
                    throw new RuntimeException(tf);
                }
            }
        }
    }

    private void upgradeV2ApplicationElements() {
        // in v2, we have ejb-module, web-module, j2ee-application etc elements 
        // to represent different type of applications
        // in v3 final, we have one generic application element to represent 
        // all type of applications 
        // we will do three things
        // 1. tranform partially all the old application related elements to 
        //    the new generic application elements (only top level, no module 
        //    sub element).
        // 2. remove all the system applications 
        Applications apps = domain.getApplications();
        Server server = domain.getServerNamed("server");

        try {
            ConfigSupport.apply(new ConfigCode() {
                public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {
                    Applications applications = (Applications) params[0];
                    Server servr = (Server) params[1];

                    // 1. transform all old application elements to new 
                    //    application element

                    // connector module
                    for (ConnectorModule connectorModule : 
                        applications.getModules(ConnectorModule.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(connectorModule.getName());
                        app.setLocation(getLocationAsURIString(
                            connectorModule.getLocation()));
                        app.setObjectType(connectorModule.getObjectType());
                        app.setDescription(connectorModule.getDescription());
                        app.setEnabled(connectorModule.getEnabled());
                        app.setDirectoryDeployed(
                            connectorModule.getDirectoryDeployed());
                        for (Property property : 
                            connectorModule.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }

                        Property prop =
                            app.createChild(Property.class);
                        prop.setName(MODULE_TYPE);
                        prop.setValue(ServerTags.CONNECTOR_MODULE);
                        app.getProperty().add(prop);

                        // removing the old connector module 
                        applications.getModules().remove(connectorModule);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // ejb-module
                    for (EjbModule ejbModule :
                        applications.getModules(EjbModule.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(ejbModule.getName());
                        app.setLocation(getLocationAsURIString(
                            ejbModule.getLocation()));
                        app.setObjectType(ejbModule.getObjectType());
                        app.setDescription(ejbModule.getDescription());
                        app.setEnabled(ejbModule.getEnabled());
                        app.setDirectoryDeployed(
                            ejbModule.getDirectoryDeployed());
                        app.setLibraries(ejbModule.getLibraries());
                        app.setAvailabilityEnabled(
                            ejbModule.getAvailabilityEnabled());
                        for (Property property :
                            ejbModule.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }

                        Property prop =
                            app.createChild(Property.class);
                        prop.setName(MODULE_TYPE);
                        prop.setValue(ServerTags.EJB_MODULE);
                        app.getProperty().add(prop);

                        // removing the old ejb module
                        applications.getModules().remove(ejbModule);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // web-module
                    for (WebModule webModule :
                        applications.getModules(WebModule.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(webModule.getName());
                        app.setLocation(getLocationAsURIString(
                            webModule.getLocation()));
                        app.setObjectType(webModule.getObjectType());
                        app.setDescription(webModule.getDescription());
                        app.setEnabled(webModule.getEnabled());
                        app.setDirectoryDeployed(
                            webModule.getDirectoryDeployed());
                        app.setLibraries(webModule.getLibraries());
                        app.setContextRoot(webModule.getContextRoot());
                        app.setAvailabilityEnabled(
                            webModule.getAvailabilityEnabled());
                        for (Property property :
                            webModule.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }

                        Property prop =
                            app.createChild(Property.class);
                        prop.setName(MODULE_TYPE);
                        prop.setValue(ServerTags.WEB_MODULE);
                        app.getProperty().add(prop);

                        // removing the old web module
                        applications.getModules().remove(webModule);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // appclient-module
                    for (AppclientModule appclientModule :
                        applications.getModules(AppclientModule.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(appclientModule.getName());
                        app.setLocation(getLocationAsURIString(
                            appclientModule.getLocation()));
                        app.setObjectType("user");
                        app.setDescription(appclientModule.getDescription());
                        app.setEnabled("true");
                        app.setDirectoryDeployed(
                            appclientModule.getDirectoryDeployed());
                        for (Property property :
                            appclientModule.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }
                        Property prop = 
                            app.createChild(Property.class);
                        prop.setName(ServerTags.JAVA_WEB_START_ENABLED);
                        prop.setValue(
                            appclientModule.getJavaWebStartEnabled());
                        app.getProperty().add(prop);

                        Property prop2 =
                            app.createChild(Property.class);
                        prop2.setName(MODULE_TYPE);
                        prop2.setValue(ServerTags.APPCLIENT_MODULE);
                        app.getProperty().add(prop2);

                        // removing the old appclient module
                        applications.getModules().remove(appclientModule);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // j2ee-application
                    for (J2eeApplication j2eeApp :
                        applications.getModules(J2eeApplication.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(j2eeApp.getName());
                        app.setLocation(getLocationAsURIString(
                            j2eeApp.getLocation()));
                        app.setObjectType(j2eeApp.getObjectType());
                        app.setDescription(j2eeApp.getDescription());
                        app.setEnabled(j2eeApp.getEnabled());
                        app.setDirectoryDeployed(
                            j2eeApp.getDirectoryDeployed());
                        app.setLibraries(j2eeApp.getLibraries());
                        app.setAvailabilityEnabled(
                            j2eeApp.getAvailabilityEnabled());
                        for (Property property :
                            j2eeApp.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }
                        Property prop = 
                            app.createChild(Property.class);
                        prop.setName(ServerTags.JAVA_WEB_START_ENABLED);
                        prop.setValue(
                            j2eeApp.getJavaWebStartEnabled());
                        app.getProperty().add(prop);

                        Property prop2 =
                            app.createChild(Property.class);
                        prop2.setName(MODULE_TYPE);
                        prop2.setValue(ServerTags.J2EE_APPLICATION);
                        app.getProperty().add(prop2);

                        // removing the old j2eeapplication module
                        applications.getModules().remove(j2eeApp);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // extension-module
                    if (applications.getModules(
                        ExtensionModule.class).size() > 0) {
                        Logger.getAnonymousLogger().log(Level.WARNING, "Ignoring extension-module elements. GlassFish v3 does not support extension modules from GlassFish v2."); 
                    }
                    for (ExtensionModule extensionModule :
                        applications.getModules(ExtensionModule.class)) {
                        // removing the extension module
                        applications.getModules().remove(extensionModule);
                    }

                    // lifecycle-module
                    for (LifecycleModule lifecycleModule :
                        applications.getModules(LifecycleModule.class)) {

                        // adding the new application element
                        Application app = applications.createChild(
                            Application.class);
                        app.setName(lifecycleModule.getName());
                        app.setObjectType(lifecycleModule.getObjectType());
                        app.setDescription(lifecycleModule.getDescription());
                        app.setEnabled(lifecycleModule.getEnabled());
                        for (Property property :
                            lifecycleModule.getProperty()) {
                            Property prop = 
                                app.createChild(Property.class);
                            prop.setName(property.getName());
                            prop.setValue(property.getValue());
                            app.getProperty().add(prop);
                        }

                        Property prop = 
                            app.createChild(Property.class);
                        prop.setName(ServerTags.CLASS_NAME);
                        prop.setValue(
                            lifecycleModule.getClassName());
                        app.getProperty().add(prop);

                        if (lifecycleModule.getClasspath() != null) {
                            Property prop1 =
                                app.createChild(Property.class);
                            prop1.setName(ServerTags.CLASSPATH);
                            prop1.setValue(
                                lifecycleModule.getClasspath());
                            app.getProperty().add(prop1);

                        }
                        if (lifecycleModule.getLoadOrder() != null) {
                            Property prop2 =
                                app.createChild(Property.class);
                            prop2.setName(ServerTags.LOAD_ORDER);
                            prop2.setValue(
                                lifecycleModule.getLoadOrder());
                            app.getProperty().add(prop2);
                        }

                        Property prop3 =
                            app.createChild(Property.class);
                        prop3.setName(ServerTags.IS_FAILURE_FATAL);
                        prop3.setValue(
                            lifecycleModule.getIsFailureFatal());
                        app.getProperty().add(prop3);

                        Property prop4 =
                            app.createChild(Property.class);
                        prop4.setName("isLifecycle");
                        prop4.setValue("true");
                        app.getProperty().add(prop4);

                        // removing the old lifecycle module
                        applications.getModules().remove(lifecycleModule);
                        // adding the new application element
                        applications.getModules().add(app);
                    }

                    // custom mbean
                    if (applications.getModules(Mbean.class).size() > 0) {
                        Logger.getAnonymousLogger().log(Level.WARNING, "Ignoring mbean elements. GlassFish v3 does not support custom MBeans from GlassFish v2."); 
                    }
                    for (Mbean mbean :
                        applications.getModules(Mbean.class)) {
                        // removing the custom mbean
                        applications.getModules().remove(mbean);
                    }

                    // 2. remove all system applications
                    for (Application application : 
                        applications.getModules(Application.class)) {
                        if (application.getObjectType().startsWith(
                            "system-")) {
                            applications.getModules().remove(application);
                        }
                    }

                    return null;
                }

            }, apps, server);
        } catch(TransactionFailure tf) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading application", tf);
            throw new RuntimeException(tf);
        }
    }

    private String getLocationAsURIString(String location) {
        File appFile = new File(location);
        return appFile.toURI().toString();
    }
}
