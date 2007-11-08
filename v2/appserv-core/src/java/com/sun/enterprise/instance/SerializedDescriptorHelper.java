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

package com.sun.enterprise.instance;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the serialization and deserialization of descriptor object graphs.
 * <p>
 * The static "store" method is used from the deployment code to create the
 * initial serialized data file for an application.  In EE environments this
 * file will be copied to the relevant instances during synchronization.
 * <p>
 * The static "load" method is used from the various subclasses of BaseManager
 * to attempt to load the serialized object graph and then return an instance 
 * of SerializedDescriptorHelper.Loader.  This object not only loads the
 * serialized descriptor (if possible) but also allows
 * the manager to get the loaded application object.  If that object is null,
 * the manager loads the application from the XML descriptors and should try to 
 * rewrite the file using the new application object by invoking the "store" 
 * method on the returned Loader object.
 * 
 * @author tjquinn
 */
public class SerializedDescriptorHelper {
    
    /** file name used for serializing (and deserializing) the DOL descriptor */
    private static final String SERIALIZED_DESCRIPTOR_FILE_NAME = "appDescr.dat";

    /** property that allows control of serialization */
    private static final String SERIALIZATION_ENABLED_PROPERTY = 
            "com.sun.aas.deployment.serializeDescriptors";

    /** default value for using descriptor serialization */
    private static final String SERIALIZATION_ENABLED_DEFAULT = "true";
    
    private static final boolean isSerializedDescriptorIOEnabled = 
            Boolean.parseBoolean(System.getProperty(
                SERIALIZATION_ENABLED_PROPERTY, 
                SERIALIZATION_ENABLED_DEFAULT));
    
    private static final Logger logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /**
     *Attempts to load the serialized object graph from the appropriate file.
     *<p>
     *Whether the attempt succeeds or not, the method returns an instance of
     *Loader that the caller can use to retrieve the loaded application and, if 
     *appropriate, attempt to write the application object graph it built from
     *the XML descriptors. 
     *<p>
     *Because failures to load the serialized data are not fatal errors, 
     *this method throws no exceptions.  Instead the loader's internal application
     *object will be null.  The caller should check for this and then load the
     *application from the XML descriptors instead.
     *
     *@param moduleID the module ID of the application to load
     *@param manager the manager for this type of module
     *@return the SerializedDescriptorHelper.Loader the caller can use to 
     *retrieve the application and, if appropriate, rewrite the file
     */
    public static Loader load(
            String moduleID, 
            BaseManager manager) {
        Loader loader = new Loader(moduleID, manager);
        loader.loadSerializedDescriptor();
        return loader;
    }
    
    /**
     * Attempts to write the application object graph to the corresponding
     * file.
     * <p>
     * Because failures to serialized the object graph are not fatal, the method 
     * throws no exceptions.  
     * 
     * @param moduleID the module being serialized
     * @param manager the manager in charge of this type of application
     * @param application the object graph to storeSerializedDescriptor
     */
    public static void store(
            String moduleID,
            BaseManager manager,
            Application application) {
        File file = getSerializedDescriptorFile(moduleID, manager);
        store(moduleID, manager, application, file);
    }
    
    /**
     *Writes the specified application to the correct location for the module.
     *
     *@param moduleID the unique identifier of the module
     *@param manager the BaseManager instance for this type of module
     *@param application the Application object graph to be written
     *@param serializedFile the File object that refers to the serialized file for this app
     */
    private static void store(
            String moduleID,
            BaseManager manager,
            Application application,
            File serializedFile) {
        if (isSerializedDescriptorIOEnabled()) {
            /*
             *Try to write the application object graph to the file.
             */
            if ( ! storeSerializedDescriptor(
                    application, 
                    serializedFile, 
                    moduleID)) {

                /*
                 *In case of any error serializing the descriptors, store an
                 *Exception object instead.  This will save the server from
                 *future attempts to write the serialized descriptors that we 
                 *expect will continue to fail.
                 */
                storeSerializedDescriptor(
                        new Exception(getCurrentSoftwareVersion()), 
                        serializedFile, 
                        moduleID);
            }
        }
    }
    
    /**
     *Returns whether serialized descriptor processing is turned on or not.
     *@return true if serialized reading and writing should take place
     */
    private static boolean isSerializedDescriptorIOEnabled() {
        return isSerializedDescriptorIOEnabled;
    }
    
    /**
     *Returns an expression containing the major and minor version strings and
     *the build ID of the currently-running software.
     */
    private static String getCurrentSoftwareVersion() {
        return Version.getMajorVersion() + "." + 
                Version.getMinorVersion() + "-" + 
                Version.getBuildVersion();
    }
    
    /**
     *Returns a File object for the serialized descriptor file for the
     *specified module overseen by the specified manager.
     *@param moduleID the unique ID of the module
     *@param manager the BaseManager concrete instance that manages apps of this type
     *@return a File for the serialized descriptor file for this app
     */
    private static File getSerializedDescriptorFile(String moduleID, BaseManager manager) {
        return new File(
                manager.getGeneratedXMLLocation(moduleID), 
                SERIALIZED_DESCRIPTOR_FILE_NAME);
    }
    
    /**
     *Logs a message, looking up the message key and substituting arguments and
     *including a Throwable indicating an error.
     *@param level the logging level at which to log this message
     *@param messageKey the key to the message
     *@param t the Throwable to be recorded with the log message
     *@param args... the arguments (if any) to be substituted into the looked-up message
     */
    private static void log(Level level, String messageKey, Throwable t, Object... args) {
        String msg = logger.getResourceBundle().getString(messageKey);
        String formattedMsg = MessageFormat.format(msg, args);
        logger.log(level, formattedMsg, t);
    }
    
    /*
     *Logs a message, looking up the message key and substituting arguments.
     *@param level the logging level at which to log this message
     *@param messageKey the key to the message
     *@param args... the arguments (if any) to be substituted into the looked-up message
     */
    private static void log(Level level, String messageKey, Object... args) {
        logger.log(level, messageKey, args);
    }
    
    /**
     *Attempts to store the specified object in serialized form in the indicated
     *file.  The module ID is used in logging.
     *@param obj the Object to be serialized
     *@param file the File into which the object graph should be written
     *@param moduleID the unique identifier for the module (used in logging)
     *@return true if the object graph was written to the file successfully; false otherwise
     */
    private static boolean storeSerializedDescriptor(Object obj, File file, String moduleID) {
        if ( ! isSerializedDescriptorIOEnabled()) {
            return false;
        }
        /*
         *Predeployed system apps will not have generated/xml directories.
         */
        if ( ! file.getParentFile().exists()) {
            return false;
        }
        ObjectOutputStream oos = null;
        boolean result;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            result = true;
            if(logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                        "Serialized application " + moduleID);
            }

        } catch (Throwable t) {
            result = false;
            log(Level.WARNING, "core.error_ser_descr", t, moduleID);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ioe) {
                    log(Level.WARNING, "core.error_ser_descr", ioe, moduleID);
                }
            }
        }
        return result;
    }
    
    /**
     *Encapsulates information about a attempt to read an application object 
     *graph read from a serialized file.
     */
    public static class Loader {

        /*
         * At most one of the next two instance variables will have meaning.  
         * At most one was read from the serialized file, since the file cannot
         * contain more than one root for the serialized object graph.
         */
        /** the app read from the file, if any */
        private Application deserializedApplication;
        
        /** the exception read from the file, if any */
        private Exception deserializedException;
        
        /** the unique module ID of the application being loaded or stored */
        private String moduleID;
    
        /** the manager responsible for handling this type of module */
        private BaseManager manager;
        
        /** the serialized descriptor file */
        private File file;
        
        /*
         *New instances are manufactured only by the static load method.
         */
        private Loader(String moduleID, BaseManager manager) {
            this.moduleID = moduleID;
            this.manager = manager;
            file = getSerializedDescriptorFile(moduleID, manager);
        }
        
        /**
         *Attempts to write the specified application to this app's serialized
         *file.
         *<p>
         *Typically used by the deployment logic after it has built the 
         *application object graph from XML descriptors having seen that the 
         *serialized file could not be read.
         *
         *@param application the app's descriptor object graph
         */
        public void store(Application application) {
            /*
             *Write out the application object graph into the file only if
             *this loader's deserializedApplication is a different object from
             *the application passed in.  If they are the same object, then
             *the manager is using the deserialized app that this loader read
             *in before, and there is no point in rewriting the same data.
             */
            if (application == deserializedApplication) {
                return;
            }
            /*
             *If the file contained a serialized exception, do not try to
             *write the file if that exception's message is the same as the 
             *current software's version.  This is because we assume that the
             *same software will not be able to write the object graph now given
             *that it could not do so before when it wrote the exception instead.
             *
             *But there is a chance that a later version of the software will be
             *able to write the graph even though an earlier version of the
             *software could not.  So if the current software is not 
             *the same as the string in the serialized exception's message, try 
             *to write the object graph again.
             */
            boolean tryToWrite = (deserializedException == null);
            if ((deserializedException != null) && 
                ( ! deserializedException.getMessage().equals(getCurrentSoftwareVersion()))
               ) {  
                tryToWrite = true;
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, 
                            "Trying to serialize an application that could not be serialized by " + 
                            deserializedException.getMessage());
                }
            }
            if (tryToWrite) {
                SerializedDescriptorHelper.store(moduleID, manager, application, file);
            }
        }
        
        /**
         *Returns the loader's application.
         *@return the application loaded from the serialized file; null if the
         *file is absent or its contents could not be read.
         */
        public Application getApplication() {
            return deserializedApplication ;
        }
        
         /**
         *Reads in the application descriptor object graph from a previously-
         *serialized copy of it in a file if the serialized file is found.  The
         *private "application" variable is set to the deserialized application.
         *In case of an error reading the file, application is left as null.
         *<p>
         *The method does not even try to load the serialized object graph if
         *the timestamp on the XML descriptor is more recent than the timestamp
         *on the serialized file.
         */
        private void loadSerializedDescriptor() {
            if ( ! isSerializedDescriptorIOEnabled()) {
                return;
            }

            deserializedApplication = null;
            deserializedException = null;

            ObjectInputStream ois = null;
            boolean fine = logger.isLoggable(Level.FINE);
            try {
                if ( ! file.exists()) {
                    if (fine) {
                        logger.fine("Serialized descriptor for " + 
                                moduleID + 
                                " not found.  Will not try to read serialized file.");
                    }
                    return;
                } 

                ois = new ObjectInputStream(new FileInputStream(file));
                Object o = ois.readObject();
                if (o instanceof Application) {
                    deserializedApplication = (Application) o;
                    if (fine) {
                        logger.fine("Serialized descriptor for " + 
                                moduleID + 
                                " loaded from " + 
                                file.getAbsolutePath());
                    }
                } else if (o instanceof Exception) {
                    /*
                     *The serialized file exists and contains another type of
                     *object, which should be an Exception because an earlier
                     *attempt at writing the file failed, most likely due to 
                     *a NonSerializableException.  Leave the application as null
                     *and save the exception.
                     */
                    deserializedException = (Exception) o;
                    if (fine) {
                        logger.fine("Serialized descriptor file contained an exception " +
                                "with message \"" + deserializedException.getMessage() + "\" " +
                                "because a previous attempt to write it failed.");
                    }
                } else {
                    /*
                     *The previously serialized object is of an unexpected type.
                     *Log a warning and continue.
                     */
                    log(Level.WARNING, "core.unexp_deser_type", moduleID, o.getClass().getName());
                }
            } catch (Throwable thr) {
                /*
                 *Any error is logged only.  This allows the caller to proceed
                 *without requiring any special exception handling. The application
                 *instance variable remains null so the caller will see that.
                 */
                log(Level.WARNING, "core.error_deser_descr", moduleID, thr.getLocalizedMessage());
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException ioe) {
                        log(Level.WARNING, "core.error_deser_descr", moduleID, ioe.getLocalizedMessage(), ioe);
                    }
                }
            }
        }
    }
}
