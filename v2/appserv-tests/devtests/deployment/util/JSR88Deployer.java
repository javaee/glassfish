/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package devtests.deployment.util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.beans.*;
import java.util.jar.Manifest;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.factories.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.shared.ModuleType;

/**
 *
 * @author  administrator
 */
public class JSR88Deployer implements ProgressListener {

    private String host;
    private String port;
    private String user;
    private String password;
    private String uri;
    private DeploymentFactory deploymentFactory;
    private DeploymentManager dm;
    private Target[] targets;
    private final String J2EE_DEPLOYMENT_MANAGER =
                        "J2EE-DeploymentFactory-Implementation-Class";
    private static List systemApps = new ArrayList();
    
    /** Record all events delivered to this deployer (which is also a progress listener. */
    private Vector receivedEvents = new Vector();
    
    /** Record the TargetModuleIDs that resulted from the most recent operation. */
    private TargetModuleID [] mostRecentTargetModuleIDs = null;
    
    /** Creates a new instance of JSR88Deployer */
    public JSR88Deployer(String uri, String user, String password) {
        this.user = user;
        this.password = password;
        this.uri = uri;
        loadDeploymentFactory();
        loadSystemApps(); //system apps to be filtered

        log("Connecting using uri = " + uri + "; user = " + user + "; password = " + password);
        try {
            dm = DeploymentFactoryManager.getInstance().getDeploymentManager(uri, user, password);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);              
        }

        Target[] allTargets = dm.getTargets();
        if (allTargets.length == 0) {
            log("Can't find deployment targets...");
            System.exit(-1);              
        }
        
        // If test being run on EE, exclude the DAS server instance from the deploy targets
        String targetPlatform = System.getProperty("deploymentTarget");
        List filteredTargets = new ArrayList();
        if( ("SERVER".equals(targetPlatform)) || ("CLUSTER".equals(targetPlatform)) ) {
            for(int i=0; i<allTargets.length; i++) {
                if(allTargets[i].getName().equals("server")) {
                    continue;
                }
                filteredTargets.add(allTargets[i]);
            }
            targets = (Target[])(filteredTargets.toArray(new Target[filteredTargets.size()]));
        } else {
            targets = allTargets;
        }

        for(int i=0; i<targets.length; i++) {
            log("DBG : Target " + i + " -> " + targets[i].getName());
        }
    }

    private int waitTillComplete(ProgressObject po) throws Exception {
        DeploymentStatus deploymentStatus = null;
        do {
            /*
             *The progress object may return a DeploymentStatus object that is a snapshot of the
             *current status and will never change.  (The spec is unclear on this behavior.)  So
             *to be sure, get a new deployment status every time through the loop to be sure of
             *getting the up-to-date status.
             */
            deploymentStatus = po.getDeploymentStatus();
            Thread.currentThread().sleep(200);
        } while (!(deploymentStatus.isCompleted() || deploymentStatus.isFailed()));   
        log("Deployment status is " + deploymentStatus.getState().toString());
        if(deploymentStatus.isFailed())
                return(-1);
        return(0);
    }

    public ProgressObject deploy(File archive, File deploymentPlan,
                                boolean startByDefault, boolean useStream,
                                ModuleType type) 
        throws Exception {

        ProgressObject dpo = null;
        ProgressObject po = null;
        if (deploymentPlan == null || deploymentPlan.getName().equals("null")) {
            log("Warning, deploying with null deployment plan");
            if (useStream) {
                if (type == null) {
                    dpo = dm.distribute(targets, 
                                    new FileInputStream(archive.getAbsolutePath()),
                                    null);
                } else {
                    dpo = dm.distribute(targets, type,
                                    new FileInputStream(archive.getAbsolutePath()),
                                    null);
                }
            } else {
                dpo = dm.distribute(targets, archive, null);          
            }
        } else {
            if (useStream) {
                if (type == null) {
                    dpo = dm.distribute(targets, 
                                    new FileInputStream(archive.getAbsolutePath()),
                                    new FileInputStream(deploymentPlan.getAbsolutePath()));
                } else {
                    dpo = dm.distribute(targets, type,
                                    new FileInputStream(archive.getAbsolutePath()),
                                    new FileInputStream(deploymentPlan.getAbsolutePath()));
                }
            } else {
                dpo = dm.distribute(targets, archive, deploymentPlan);          
            }
        }            
        if (dpo!=null) {
            dpo.addProgressListener(this);
            if(waitTillComplete(dpo) != 0) {
                log("DEPLOY Action Failed");
                return(null);
            }

            TargetModuleID[] targetModuleIDs = dpo.getResultTargetModuleIDs();        
            this.mostRecentTargetModuleIDs = targetModuleIDs;
            dumpResultModuleIDs("Deployed " , dpo);

            if (startByDefault) {
                log("STARTINNG... " + targetModuleIDs);
                po = dm.start(targetModuleIDs);
                if (po!=null) {
                    po.addProgressListener(this);
                    if(waitTillComplete(po) != 0) {
                        log("START Action Failed");
                        return(null);
                    }
                }
                return po;
            }
        }
        return(dpo);
    }

    public ProgressObject redeploy(String moduleID, File archive,
                                File deploymentPlan, boolean useStream) throws Exception {

        TargetModuleID[] list = null;
        if (moduleID == null) { //redeploy all but system apps
            throw new UnsupportedOperationException("DO NOT SUPPORT REDEPLOY MULTIPLE APPS");
        } else {
            list = findApplication(moduleID, null);
        }

        TargetModuleID[] modules = filterSystemApps(list);
        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                log("REDEPLOYING... " + modules[i]);
            }
        }

        ProgressObject dpo = null;
        if (deploymentPlan == null || deploymentPlan.getName().equals("null")) {
            log("Warning, redeploying with null deployment plan");
            if (useStream) {            
                dpo = dm.redeploy(modules, 
                                    new FileInputStream(archive.getAbsolutePath()),
                                    null);
            } else {
                dpo = dm.redeploy(modules, archive, null);
            }
        } else {
            if (useStream) {            
                dpo = dm.redeploy(modules, 
                                    new FileInputStream(archive.getAbsolutePath()),
                                    new FileInputStream(deploymentPlan.getAbsolutePath()));
            } else {
                dpo = dm.redeploy(modules, archive, deploymentPlan);          
            }
        }            
        if (dpo!=null) {
            dpo.addProgressListener(this);
            if(waitTillComplete(dpo) != 0) {
                log("REDEPLOY Action Failed");
                return(null);
            }

            TargetModuleID[] targetModuleIDs = dpo.getResultTargetModuleIDs();        
            this.mostRecentTargetModuleIDs = targetModuleIDs;
            dumpResultModuleIDs("Redeployed " , dpo);
        }
        return(dpo);
    }

    public ProgressObject stop(String moduleID) throws Exception {
        TargetModuleID[] list = null;
        if (moduleID == null) { //stop all but system apps
            list = getAllApplications(Boolean.TRUE);
        } else {
            list = findApplication(moduleID, Boolean.TRUE);
        }

        ProgressObject dpo = null;
        TargetModuleID[] modules = filterSystemApps(list);
        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                log("STOPPING... " + modules[i]);
            }
            dpo = dm.stop(modules);
            if (dpo!=null) {
                dpo.addProgressListener(this);
                if(waitTillComplete(dpo) != 0) {
                    log("STOP Action Failed");
                    return(null);
                }
                this.mostRecentTargetModuleIDs = dpo.getResultTargetModuleIDs();
            }
        }
        return(dpo);
    }

    /**
     *Starts an application, with an option of waiting between the time the operation is requested
     *and the time the deployer is added as a listener.  This option helps with the test for race
     *conditions involved with managing the list of listeners and the list of delivered events.
     */
    public ProgressObject start(String moduleID, int delayBeforeRegisteringListener) throws Exception {
        TargetModuleID[] list = null;
        if (moduleID == null) { //start all but system apps
            list = getAllApplications(Boolean.FALSE);
        } else {
            list = findApplication(moduleID, Boolean.FALSE);
        }

        TargetModuleID[] modules = filterSystemApps(list);
        ProgressObject dpo = null;
        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                log("STARTINNG... " + modules[i]);
            }
            dpo = dm.start(modules);
            if (delayBeforeRegisteringListener > 0) {
                try {
                    log("Pausing before adding self as a progress listener");
                    Thread.currentThread().sleep(delayBeforeRegisteringListener);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(this.getClass().getName() + " was interrupted sleeping before adding itself as a progresslistener", ie);
                }
            }
            if (dpo!=null) {
                dpo.addProgressListener(this);
                if (delayBeforeRegisteringListener > 0) {
                    log("Now registered as a progress listener");
                }
                if(waitTillComplete(dpo) != 0) {
                    log("START Action Failed");
                    return(null);
                }
                this.mostRecentTargetModuleIDs = dpo.getResultTargetModuleIDs();
            }
        }
        return(dpo);
    }

    public ProgressObject start(String moduleID) throws Exception {
        return start(moduleID, 0);
    }

    public ProgressObject undeploy(String moduleID) throws Exception {
        TargetModuleID[] list = null;
        //log ("000 trying to undeploy moduleID = " + moduleID);
        if (moduleID == null) { //undeploy all but system apps
            list = getAllApplications(null);
        } else {
            list = findApplication(moduleID, null);
        }

        ProgressObject dpo = null;
        TargetModuleID[] modules = filterSystemApps(list);
        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                log("UNDEPLOYING... " + modules[i]);
            }
            dpo = dm.undeploy(modules);
            if (dpo!=null) {
                dpo.addProgressListener(this);
                if(waitTillComplete(dpo) != 0) {
                    log("UNDEPLOY Action Failed");
                    return(null);
                }
                this.mostRecentTargetModuleIDs = dpo.getResultTargetModuleIDs();
            }
        }
        return(dpo);
    }

    protected void dumpResultModuleIDs(String prefix, ProgressObject po) {
        TargetModuleID[] targetModuleIDs = po.getResultTargetModuleIDs();
	dumpModulesIDs(prefix, targetModuleIDs);
    }

    protected void dumpModulesIDs(String prefix, TargetModuleID[] targetModuleIDs) {
        for (int i=0;i<targetModuleIDs.length;i++) {            
            dumpModulesIDs(prefix, targetModuleIDs[i]);            
        }
    }
    
    protected void dumpModulesIDs(String prefix, TargetModuleID targetModuleID) {
        log(prefix + targetModuleID);
        TargetModuleID[] subs = targetModuleID.getChildTargetModuleID();
        if (subs!=null) {
            for (int i=0;i<subs.length;i++) {
                log(" Child " + i + " = " + subs[i]);
            }
        }
    }

    public TargetModuleID[] getApplications(ModuleType moduleType, Boolean running) 
        throws Exception {
        TargetModuleID[] modules = null;
        if (running==null) {
            modules = dm.getAvailableModules(moduleType, targets);
        } else if (running.booleanValue()) {
            modules = dm.getRunningModules(moduleType, targets);
        } else {
            modules = dm.getNonRunningModules(moduleType, targets);
        }

        return modules;
    }

    public TargetModuleID[] getAllApplications(Boolean running) 
        throws Exception {
        //log("222. getAllApplications, running = " + running);
        TargetModuleID[] ears = getApplications(ModuleType.EAR, running);
        TargetModuleID[] wars = getApplications(ModuleType.WAR, running);
        TargetModuleID[] cars = getApplications(ModuleType.CAR, running);
        TargetModuleID[] ejbs = getApplications(ModuleType.EJB, running);
        TargetModuleID[] rars = getApplications(ModuleType.RAR, running);

        List list = new ArrayList();
        for (int i = 0; i < ears.length; i++) { list.add(ears[i]); }
        for (int i = 0; i < wars.length; i++) { list.add(wars[i]); }
        for (int i = 0; i < cars.length; i++) { list.add(cars[i]); }
        for (int i = 0; i < ejbs.length; i++) { list.add(ejbs[i]); }
        for (int i = 0; i < rars.length; i++) { list.add(rars[i]); }
    
        TargetModuleID[] results = new TargetModuleID[list.size()];
        for (int i = 0; i < list.size(); i++) { 
            results[i] = (TargetModuleID) list.get(i);
        }
        return results;
    }

    protected TargetModuleID[] findApplication(String moduleID, ModuleType moduleType, Boolean running) 
        throws Exception {
        /*
         *The DeploymentFacility requires that start, stop, redeploy, and undeploy requests
         *operate on the same set of targets that the original deployment did.  As written currently, this test
         *class always deploys to all available targets so the other functions should also
         *apply to all available targets.
         */
        TargetModuleID[] list = getApplications(moduleType, running);
        return filterTargetModuleIDsByModule(list, moduleID);
    }

    protected TargetModuleID[] findApplication(String moduleID, Boolean running) 
        throws Exception {
        //log("111 trying to get everything, moduleid = " + moduleID + "; boolean = " + running);
        TargetModuleID[] list = getAllApplications(running);
        return filterTargetModuleIDsByModule(list, moduleID);
    }

    /**
     *Filter an array of TargetModuleID, keeping only those that match the specified module ID.
     *@param tmids the array of TargetModuleID to be filtered
     *@param moduleID the name of the module of interest
     *@return new TargetModuleID array, containing only the elements from the original array that match the module ID
     */
    protected TargetModuleID[] filterTargetModuleIDsByModule(TargetModuleID [] tmids, String moduleID) {
        List tmidsToUse = new ArrayList();
        /*
         *Add to the vector of TMIDs each TMID from getApplications that also matches the
         *module ID.
         */
        for (int i = 0; i < tmids.length; i++) {
            if (moduleID.equals(tmids[i].getModuleID())) {
                tmidsToUse.add(tmids[i]);
            }
        }
        return (TargetModuleID [])(tmidsToUse.toArray(new TargetModuleID[tmidsToUse.size()]));
    }
    
    public void listApplications(ModuleType moduleType, Boolean running) 
        throws Exception {
        TargetModuleID[] modules = getApplications(moduleType, running); 
        if (modules == null) {
        } else {
            for (int i = 0; i < modules.length; i++) {
                if (running==null) {
                    dumpModulesIDs("    AVAILABLE ", modules[i]);
                } else if (running.booleanValue()) {
                    dumpModulesIDs("    RUNNING ", modules[i]);
                } else {
                    dumpModulesIDs("    NOT RUNNING ", modules[i]); 
                }
            }
        }
    }
    
    private void invokeAppClient(ProgressObject po, TargetModuleID targetModuleID) {
        TargetModuleID[] subs = targetModuleID.getChildTargetModuleID();   
        if (subs==null) {
            return;
        }
        for (int i=0;i<subs.length;i++) {
            if (subs[i].getModuleID().indexOf("client")!=-1) {
                log(" App Client child " + subs[i]);
                ClientConfiguration cc = po.getClientConfiguration(subs[i]);
                try {
                    cc.execute();
                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }    

    
    public static void main(String[] args) {

        int finalExitValue = 0;

        if (args.length == 0 || "help".equals(args[0])) { 
            usage(); 
            System.exit(1);
        }
        if (args.length < 5) {
            usage(); 
            System.exit(1);
        }

        JSR88Deployer deployer = new JSR88Deployer(args[1], args[2], args[3]);
        if ("deploy".equals(args[0]) || "deploy-stream".equals(args[0])) {
            if (args.length < 6) { 
                usage(); 
                System.exit(1);
            }
            boolean useStream = "deploy-stream".equals(args[0]);
            boolean startByDefault = "true".equals(args[4]);
            java.io.File inputFile = new java.io.File(args[5]);
            if (!inputFile.exists()) {
                error("File not found : " + inputFile.getPath());
                System.exit(1);
            }
            File deploymentFile = null;
            if (args.length > 6) {
                deploymentFile = new File(args[6]);
                if (!args[6].equals("null")) {
                    if (!deploymentFile.exists()) {
                        error("Deployment File not found : " + deploymentFile.getPath());
                        System.exit(1);
                    }
                }
            }
            try {
                log("Deploying " + inputFile + " plan: " + deploymentFile);
                if(deployer.deploy(inputFile, deploymentFile,
                                        startByDefault, useStream, null) == null)
                    finalExitValue = -1;
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if ("deploy-stream-withtype".equals(args[0])) {
            if (args.length < 7) { 
                usage(); 
                System.exit(1);
            }
            boolean useStream = true;
            boolean startByDefault = "true".equals(args[4]);
            java.io.File inputFile = new java.io.File(args[5]);
            if (!inputFile.exists()) {
                error("File not found : " + inputFile.getPath());
                System.exit(1);
            }
            ModuleType t = 
                ModuleType.getModuleType((new Integer(args[6])).intValue());
            File deploymentFile = null;
            if (args.length > 7) {
                deploymentFile = new File(args[7]);
                if (!args[7].equals("null")) {
                    if (!deploymentFile.exists()) {
                        error("Deployment File not found : " + deploymentFile.getPath());
                        System.exit(1);
                    }
                }
            }
            try {
                log("Deploying " + inputFile + " plan: " + deploymentFile);
                if(deployer.deploy(inputFile, deploymentFile,
                                        startByDefault, useStream, t) == null)
                    finalExitValue = -1;
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else if ("list".equals(args[0])) {

            if (args.length != 5) { 
                usage(); 
                System.exit(0);
            }

            try {
                if ("all".equals(args[4])) {
                    deployer.listApplications(ModuleType.EAR, null);
                    deployer.listApplications(ModuleType.WAR, null);
                    deployer.listApplications(ModuleType.EJB, null);
                    deployer.listApplications(ModuleType.CAR, null);
                    deployer.listApplications(ModuleType.RAR, null);
                } else if ("running".equals(args[4])) {
                    deployer.listApplications(ModuleType.EAR, Boolean.TRUE);
                    deployer.listApplications(ModuleType.WAR, Boolean.TRUE);
                    deployer.listApplications(ModuleType.EJB, Boolean.TRUE);
                    deployer.listApplications(ModuleType.CAR, Boolean.TRUE);
                    deployer.listApplications(ModuleType.RAR, Boolean.TRUE);
                } else if ("nonrunning".equals(args[4])) {
                    deployer.listApplications(ModuleType.EAR, Boolean.FALSE);
                    deployer.listApplications(ModuleType.WAR, Boolean.FALSE);
                    deployer.listApplications(ModuleType.EJB, Boolean.FALSE);
                    deployer.listApplications(ModuleType.CAR, Boolean.FALSE);
                    deployer.listApplications(ModuleType.RAR, Boolean.FALSE);
                } else {
                    usage();
                    finalExitValue = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else if ("redeploy".equals(args[0]) || "redeploy-stream".equals(args[0])) {
            if (args.length < 6) { 
                usage(); 
                System.exit(-1);
            }
            
            boolean useStream = "redeploy-stream".equals(args[0]);
            java.io.File inputFile = new java.io.File(args[5]);
            if (!inputFile.exists()) {
                error("File not found : " + inputFile.getPath());
                System.exit(1);
            }
            File deploymentFile = null;
            if (args.length > 6) {
                deploymentFile = new File(args[6]);
                if (!args[6].equals("null")) {
                    if (!deploymentFile.exists()) {
                        error("Deployment File not found : " + deploymentFile.getPath());
                        System.exit(1);
                    }
                }
            }
            try {
                log("Redeploying " + inputFile + " plan: " + deploymentFile);
                if ("all".equals(args[4])) {
                    if(deployer.redeploy(null, inputFile, deploymentFile, useStream) == null)
                        finalExitValue = -1;
                } else {
                    if(deployer.redeploy(args[4], inputFile, deploymentFile, useStream) == null)
                        finalExitValue = -1;
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            if (args.length != 5) { 
                usage(); 
                System.exit(1);
            }

            try {
                if ("all".equals(args[4])) {
                    if ("undeploy".equals(args[0])) {
                        if(deployer.undeploy(null) == null)
                            finalExitValue = -1;
                    } else if ("start".equals(args[0])) {
                        if(deployer.start(null) == null)
                            finalExitValue = -1;
                    } else if ("stop".equals(args[0])) {
                        if(deployer.stop(null) == null)
                            finalExitValue = -1;
                    } else {
                        usage();
                        finalExitValue = 1;
                    }
                } else {
                    if ("undeploy".equals(args[0])) {
                        if(deployer.undeploy(args[4]) == null)
                            finalExitValue = -1;
                    } else if ("start".equals(args[0])) {
                        if(deployer.start(args[4]) == null)
                            finalExitValue = -1;
                    } else if ("stop".equals(args[0])) {
                        if(deployer.stop(args[4]) == null)
                            finalExitValue = -1;
                    } else {
                        usage();
                        finalExitValue = 1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        System.exit(finalExitValue);
    }
    
    /** Invoked when a deployment progress event occurs.
     *
     * @param event the progress status event.
     */
    public void handleProgressEvent(ProgressEvent event) {
        DeploymentStatus ds = event.getDeploymentStatus();
        System.out.println("Received Progress Event state " + ds.getState() + " msg = " + ds.getMessage());
        TargetModuleID tmid = event.getTargetModuleID();
        System.out.println("Received Progress Event target module id " + tmid.getModuleID());
        /*
         *Add this event to the vector collecting them.
         */
        this.receivedEvents.add(event);
    }

    /**
     *Report all the events received by this deployer.
     *
     *@return array of Objects containing the received events
     */
    public ProgressEvent [] getReceivedEvents() {
        ProgressEvent [] answer = new ProgressEvent[this.receivedEvents.size()];
        return (ProgressEvent []) this.receivedEvents.toArray(answer);
    }
    
    /**
     *Clear the collection of received events recorded by this deployer.
     */
    public void clearReceivedEvents() {
        this.receivedEvents.clear();
    }

    public TargetModuleID [] getMostRecentTargetModuleIDs() {
        return this.mostRecentTargetModuleIDs;
    }
    
    public void clearMostRecentTargetModuleIDs() {
        this.mostRecentTargetModuleIDs = null;
    }
    
    private void loadDeploymentFactory() {
        try {
            File file = new File(System.getProperty("com.sun.aas.installRoot")
                + File.separator+ "lib" + File.separator + "deployment"
                + File.separator + "sun-as-jsr88-dm.jar");

            Manifest mf = new java.util.jar.JarFile(file).getManifest();
            String className = mf.getMainAttributes().getValue(J2EE_DEPLOYMENT_MANAGER);
            URL[] urls = new URL[]{file.toURL()};
            URLClassLoader urlClassLoader = new java.net.URLClassLoader(urls, getClass().getClassLoader());

            Class factory = null;
            try {
                factory=urlClassLoader.loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }   

            Object df = null;
            try {   
                df = factory.newInstance();
            } catch (Exception ie) {
                ie.printStackTrace();
            }
            if (df instanceof DeploymentFactory) {
                DeploymentFactoryManager.getInstance().registerDeploymentFactory((DeploymentFactory) df);
            } else {
                System.exit(-1);
            }  

        } catch (Exception ex) {
            log("Failed to load the deployment factory.");
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private static void usage() {
        System.out.println("Usage: command <JSR88.URI> <admin-user> <admin-password> [command-parameters]");
        System.out.println("    where command is one of [deploy<-stream> | redeploy<-stream> | undeploy | start | stop | list]");
        System.out.println("    where <JSR88.URI> is: deployer:Sun:AppServer::${admin.host}:${admin.port} for PE");
        System.out.println("    where <JSR88.URI> is: deployer:Sun:AppServer::${admin.host}:${admin.port}:https for EE");
        System.out.println("    where command-parameters are as follows");
        System.out.println("      deploy: <startByDefault> <archiveFile> [<deploymentFile>]");
        System.out.println("      deploy-stream: <startByDefault> <archiveFile> [<deploymentFile>]");
        System.out.println("      deploy-stream-withtype: <startByDefault> <archiveFile> <moduleType> [<deploymentFile>]");
        System.out.println("      redeploy: <moduleID> <archiveFile> [<deploymentFile>]");
        System.out.println("      redeploy-stream: <moduleID> <archiveFile> [<deploymentFile>]");
        System.out.println("      undeploy: [all | moduleID]");
        System.out.println("      start: [all | moduleID]");
        System.out.println("      stop: [all | moduleID]");
        System.out.println("      list: [all | running | nonrunning]");
    }

    public static void log(String message) {
        System.out.println("[JSR88Deployer]:: " + message);
    }

    public static void error(String message) {
        System.err.println("[JSR88Deployer]:: " + message);
    }
    
    private void loadSystemApps() {
        systemApps.add("MEjbApp");
        systemApps.add("__ejb_container_timer_app");
        systemApps.add("adminapp");
        systemApps.add("admingui");
        systemApps.add("com_sun_web_ui");
    }

    private TargetModuleID[] filterSystemApps(TargetModuleID[] list) {

        //Comment out the following logic as we will move to use the MBean
        //that returns only non-system apps.
/*
        List l = new ArrayList();
        for (int i = 0; i < list.length; i++) {
            String moduleID = list[i].getModuleID();
            log("found module = " + moduleID);
            if (!systemApps.contains(moduleID)) {
                l.add(list[i]);
            }
        }

        TargetModuleID[] ids = new TargetModuleID[l.size()];
        for (int i = 0; i < l.size(); i++) {
            ids[i] = (TargetModuleID) l.get(i);
        }
        return ids;
*/
        return list;
    }
}
