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
package com.sun.enterprise.appclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/*
 **************************************************************************
 Uncomment the following imports once compilation is always using Java SE 6.
 */
//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineFactory;
//import javax.script.ScriptEngineManager;
/*
 **************************************************************************
 */

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Supports script execution from within the ACC.
 * <p>
 * This class and its inner classes encapsulate some of the
 * details of scripting support so that the GlassFish ACC can support scripting.
 * Note that only Java SE 6 supports scripting.  The implementation below
 * allows GlassFish to be built under Java SE 5 and still support scripting
 * when it is running under Java SE 6.
 * <p>
 * Once GlassFish is always compiled under Java SE 6, the commented imports above
 * can be uncommented and the inner classes and interfaces below can be removed.
 * <p>
 * In this implementation, the caller should:
 * <nl>
 * <le>Instantiate ScriptingSupport.
 * <le>Invoke startScript.  If the returned value is true then the script has
 * been started; false means the script was not found or no suitable script
 * engine for that script was found.
 * <le>Only during rundown, invoke close.  This closes the input stream that
 * was provided to the script engine.  If the close method is invoked too soon
 * the script engine might not have completely read the script, in which case 
 * it reports an end-of-stream error.
 * </nl>
 * <p>
 * ScriptingSupport can be used in two ways.  In the first, the caller provides
 * the prefix for the script name.  ScriptingSupport then checks all available
 * scripting engines, gets the list of file types each supports, and looks for
 * a script with the supplied prefix and that particular file type.  If if finds
 * such a script, then that script is submitted to that engine for execution.
 * <p>
 * In the other style of usage, the caller provides the prefix and file type for
 * the script to be run.  ScriptingSupport searches for a scripting engine that 
 * supports the supplied file type and, if found and if the script exists, submits
 * the script to that engine.  
 */
public class ScriptingSupport {

    /** class loader to be used by the scripting factory and engine */
    private ClassLoader classLoader;
    
    /** name of the script being run */
    private String scriptName = null;

    /** the script engine manager used to get the script engine factory */
    private ScriptEngineManager scriptEngineManager;

    /** the InputStream for the selected script */
    private InputStream scriptStream = null;
    
    /** optional Logger passed to the constructor */
    private Logger logger;
    
    /* records whether detailed logging should be done */
    private boolean isDetailed;

    /**
     * Creates a new instance of the ScriptingSupport class.
     * @param classLoader the class loader the scripting engine should use
     */
    public ScriptingSupport(ClassLoader classLoader) {
        this(classLoader, null);
    }
    
    /**
     * Creates a new instance of the ScriptingSupport class.
     * @param classLoader the class loader the scripting engine should use
     * @param logger the Logger to which messages can be written
     */
    public ScriptingSupport(ClassLoader classLoader, Logger logger) {
        this.classLoader = classLoader;
        scriptEngineManager = createScriptEngineManager(classLoader);
        this.logger = logger;
        isDetailed = (logger != null && logger.isLoggable(Level.FINE));
    }

    /**
     * Start running the script with the specified prefix (name) and type.
     * <p>
     * Upon return from this method, the script will have been started if it
     * exists and a suitable script engine for it was found.
     * @param scriptPrefix the name part of the script name to run
     * @param scriptType the script type (file type) of the script to run
     * @param args command-line arguments passed to the client
     * @return true if the script has been started; false if the script was not
     * found or if no suitable script engine for the script type was found
     */
    public boolean startScript(
            String scriptPrefix, 
            String scriptType, 
            Collection<String> args) {
        /*
         Make sure the script engine manager is non-null.
         */
        if (scriptEngineManager == null) {
            return false;
        }
        
        /*
         Check each available engine factory's candidate extensions for one
         that matches the requested script type.
         */
        List<ScriptEngineFactory> candidateEngineFactories = scriptEngineManager.getEngineFactories();
        for (ScriptEngineFactory candidateEngineFactory : candidateEngineFactories) {
            List<String> candidateExtensions = candidateEngineFactory.getExtensions();
            if (isDetailed) {
                logger.fine("Checking script engine factory " + 
                        candidateEngineFactory.getEngineName() + 
                        " (file type(s): " + candidateExtensions.toString() +
                        ")");
            }
            if (candidateExtensions.contains(scriptType)) {
                /*
                 This script engine handles this script type.
                 */
                if (isDetailed) {
                    logger.fine("Script engine supports script type " + scriptType);
                }

                /*
                 Try to locate the requested script.
                 */
                String candidateResourceName = computeResourceName(scriptPrefix, scriptType);
                InputStream is = findScript(candidateResourceName);
                if (is != null) {
                    if (isDetailed) {
                        logger.fine("Starting script " + candidateResourceName);
                    }
                    /*
                     Found an engine that supports the file type and found the
                     script. Submit the script to the engine.
                     */
                    startExecution
                            (candidateResourceName, 
                             candidateEngineFactory.getScriptEngine(), 
                             is,
                             args);
                    return true;
                } else {
                    /*
                     The specified script is not there
                     */
                    if (isDetailed) {
                        logger.fine("Could not find requested script " + candidateResourceName);
                    }
                    return false;
                }
            }
            /*
             This candidate script engine factory does not support the requested
             file type.  Continue with the next factory.
             */
        }
        /*
         No available scripting engine supports the requested file type.
         */
        if (isDetailed) {
            logger.fine("No available scripting engine supports script type " + scriptType);
        }
        return false;
    }
    
    /**
     * Selects the first script with a file type that matches one of the
     * extensions supported by the available script engines and returns an
     * InputStream for that script.
     * <p>
     * If a script is found that can be run by an available script engine, the
     * script's full name and the supporting script engine are recorded internally
     * in the object's private variables as side-effects.
     * @param scriptPrefix the beginning part of the script name.  Various
     * candidate file types are appended to the prefix to create script names
     * to search for.
     * @param args command-line arguments passed to the client
     * @return an InputStream for the selected script
     */
    public boolean startScript(String scriptPrefix, Collection<String> args) {
        /*
         Make sure the script engine manager is non-null.
         */
        if (scriptEngineManager == null) {
            return false;
        }
        
        /*
         Scan the available script engine factories.  For each, get the file
         types it handles and for each such file type try to locate a script
         of that type.
         */
        List<ScriptEngineFactory> candidateEngineFactories = scriptEngineManager.getEngineFactories();
        for (ScriptEngineFactory candidateEngineFactory : candidateEngineFactories) {
            List<String> extensions = candidateEngineFactory.getExtensions();
            if (isDetailed) {
                logger.fine("Checking script engine factory " + candidateEngineFactory.getEngineName() + " (file type(s): " + extensions.toString() + ")");
            }
            for (String candidateScriptType : extensions) {
                String candidateResourceName = computeResourceName(scriptPrefix, candidateScriptType);
                InputStream is = findScript(candidateResourceName);
                if (is != null) {
                    /*
                     Found a script with the supplied prefix and one of this 
                     factory's supported extensions.  Submit this script to the
                     engine.
                     */
                    if (isDetailed) {
                        logger.fine("Starting script " + candidateResourceName + " in this script engine");
                    }
                    startExecution
                            (candidateResourceName, 
                             candidateEngineFactory.getScriptEngine(),
                             is,
                             args);
                    return true;
                }
                /*
                 There is no script with the supplied prefix and this file type.
                 Continue looking for a script using the next file type that
                 this factory supports.
                 */
            }
            /*
             Checked all extensions supported by this engine factory.  Continue
             checking the next engine factory.
             */
            if (isDetailed) {
                logger.fine("No script found that this engine could run");
            }
        }
        if (isDetailed) {
            if (candidateEngineFactories.size() == 0) {
                logger.fine("No script engine factories were found; no scripting support is available");
            } else {
                logger.fine("No script found that is runnable by any available script engine");
            }
        }
        return false;
    }

    /**
     * Convert the script prefix and type into a resource name.
     * @param scriptPrefix the name part of the script to convert
     * @param scriptType the file type (extension) part of the script name
     * @return a String containing the resource path to the script
     */
    private String computeResourceName(String scriptPrefix, String scriptType) {
        String resourcePrefix = scriptPrefix.replace('.', '/');
        String candidateScript = resourcePrefix + "." + scriptType;
        return candidateScript;
    }
    
    /**
     * Searches for a script of the specified path, returning an InputStream
     * to the script if found and null otherwise.
     * @param candidateScript the resource path of a potential script
     * @return an InputStream to the script if found; null otherwise
     */
    private InputStream findScript(String candidateScript) {
        InputStream result = null;
        if (isDetailed) {
           logger.fine("Looking for script " + candidateScript);
        }
        result = classLoader.getResourceAsStream(candidateScript);
        if (result != null) {
            /*
             The script exists.
             */
            if (isDetailed) {
                logger.fine("Found matching script " + candidateScript);
            }
        } else {
            if (isDetailed) {
                logger.fine("Script " + candidateScript + " not found");
            }
        }
        return result;
    }
       
    /**
     * Starts running the specified script in the given script engine.
     * <p>
     * Also records the name and input stream of the script in instance 
     * variables for later use.
     * @param resourceName the resource path to the script
     * @param scriptEngine the ScriptEngine to execute the script
     * @param scriptStream the InputStream (already opened) to the script
     */
    private void startExecution(
            String resourceName,
            final ScriptEngine scriptEngine, 
            final InputStream scriptStream,
            Collection<String> args) {
        this.scriptName = resourceName;
        this.scriptStream = scriptStream;
        
        
        final InputStreamReader isr = new InputStreamReader(scriptStream);
        final Bindings bindings = scriptEngine.createBindings();
        bindings.put("arguments:java.util.Collection", args);
        ScriptContext context = new SimpleScriptContext();
        // Bug workaround
        context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptEngine.setContext(context);
        
        /*
         Process the script on the event dispatcher thread.  Otherwise some
         strange race conditions can arise.
         */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    scriptEngine.eval(isr);
                } catch (Throwable thr) {
                    throw new RuntimeException(thr);
                }
            }
            });
    }

    /**
     * Returns the name of the script being executed by this instance of 
     * ScriptingSupport.
     * @return the name of the script
     */
    public String getScriptName() {
        return scriptName;
    }
    
    /**
     * Closes the ScriptingSupport object, cleaning up any resources.
     */
    public void close() throws IOException {
        if (scriptStream != null) {
            scriptStream.close();
            scriptStream = null;
        }
    }
    
    /**
     * Creates a new script engine manager using the supplied class loder
     * @param classLoader the ClassLoader the script engine should use
     * @return the ScriptEngineManager created; null if the class is not found
     */
    private ScriptEngineManager createScriptEngineManager(ClassLoader classLoader) {
        ScriptEngineManager result = null;
        try {
            result = new ScriptEngineManager(classLoader);
        } catch (Throwable t) {
            if (isDetailed) {
                logger.fine("Could not locate script engine manager; assuming not present");
            }
        }
        return result;
    }
    
    /**********************************************************************/
    /*
     Comment out or remove the following inner class and interface declarastions
     once GlassFish is being built and run exclusively under Java SE 6.
     
     These declarations are stand-ins for the scripting-related classes and
     interfaces available beginning in Java SE 6.  They use reflection to 
     find those classes and interface and invoke constructors and methods on
     the related objects.
     
     They are intended to be completely compatible with the SE-provided classes
     and interfaces of the same names.
     */
    
    private class ScriptEngineManager {
        
        private static final String SCRIPT_ENGINE_MANAGER_TYPE_NAME = "javax.script.ScriptEngineManager";
        private static final String GET_ENGINE_FACTORIES_METHOD_NAME = "getEngineFactories";
        private static final String SCRIPT_ENGINE_FACTORY_TYPE_NAME = "javax.script.ScriptEngineFactory";
        
        private Object scriptEngineManager;
        
        public ScriptEngineManager(ClassLoader classLoader) 
                throws ClassNotFoundException, NoSuchMethodException, 
                    InstantiationException, IllegalAccessException, 
                    IllegalArgumentException, InvocationTargetException {
            scriptEngineManager = createScriptEngineManager(classLoader);
        }
        
        private Object createScriptEngineManager(ClassLoader classLoader) 
                throws ClassNotFoundException, NoSuchMethodException, 
                        InstantiationException, IllegalAccessException, 
                        IllegalArgumentException, InvocationTargetException {
            Class<?> scriptEngineManagerClass = Class.forName(SCRIPT_ENGINE_MANAGER_TYPE_NAME);
            Constructor scriptEngineManagerConstructor = scriptEngineManagerClass.getConstructor(ClassLoader.class);

            Object result = scriptEngineManagerConstructor.newInstance(classLoader);

            return result;
            
        }
        
        public List<ScriptEngineFactory> getEngineFactories() {
            List<ScriptEngineFactory> result = new ArrayList<ScriptEngineFactory>();
            try {
                Class<?> scriptEngineFactoryType = Class.forName
                        (SCRIPT_ENGINE_FACTORY_TYPE_NAME, 
                         true, 
                         scriptEngineManager.getClass().getClassLoader());
                Method getEngineFactoriesMethod = scriptEngineManager.getClass().getMethod(GET_ENGINE_FACTORIES_METHOD_NAME);
                Object returnVal = getEngineFactoriesMethod.invoke(scriptEngineManager);
                if (returnVal instanceof List<?>) {
                    List<?> returnList = (List<?>) returnVal;
                    for (Object o : returnList) {
                        if (scriptEngineFactoryType.isAssignableFrom(o.getClass())) {
                            result.add(new ScriptEngineFactoryImpl(o));
                        }
                    }
                }
                return result;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
    
    private interface ScriptEngineFactory {
        public List<String> getExtensions();
        
        public ScriptEngine getScriptEngine();
        
        public String getEngineName();
    }
    
    private class ScriptEngineFactoryImpl implements ScriptEngineFactory {
        
        private static final String GET_SCRIPT_ENGINE_METHOD_NAME = "getScriptEngine";

        private static final String GET_EXTENSIONS_METHOD_NAME = "getExtensions";

        private static final String GET_ENGINE_NAME_METHOD_NAME = "getEngineName";
        
        private Object scriptEngineFactory;
        
        public ScriptEngineFactoryImpl(Object o) {
            scriptEngineFactory = o;
        }
        
        public List<String> getExtensions() {
            List<String> result = new ArrayList<String>();
            try {
                Method getExtensionsMethod = scriptEngineFactory.getClass().getMethod(GET_EXTENSIONS_METHOD_NAME);
                Object extensions = getExtensionsMethod.invoke(scriptEngineFactory);
                if (extensions instanceof List<?>) {
                    List<?> extensionsList = (List<?>) extensions;
                    for (Object o : extensionsList) {
                        if (o instanceof String) {
                            result.add((String) o);
                        }
                    }
                }
                return result;
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }

        public ScriptEngine getScriptEngine() {
            try {
                Method getScriptEngineMethod = scriptEngineFactory.getClass().getMethod(GET_SCRIPT_ENGINE_METHOD_NAME);
                Object o = getScriptEngineMethod.invoke(scriptEngineFactory);
                return new ScriptEngine(o);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
        
        public String getEngineName() {
            try {
                Method getEngineNameMethod = scriptEngineFactory.getClass().getMethod(GET_ENGINE_NAME_METHOD_NAME);
                Object o = getEngineNameMethod.invoke(scriptEngineFactory);
                if (o instanceof String) {
                    return (String) o;
                } else {
                    return o.toString();
                }
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
    }
    
    private class ScriptEngine {
        
        private static final String EVAL_METHOD_NAME = "eval";
        private static final String CREATE_BINDINGS_METHOD_NAME = "createBindings";
        private static final String SET_CONTEXT_METHOD_NAME = "setContext";
        
        public Object scriptEngine;
        
        public ScriptEngine(Object o) {
            scriptEngine = o;
        }
        
        public void eval(InputStreamReader reader) {
            try {
                Method evalMethod = scriptEngine.getClass().getMethod(EVAL_METHOD_NAME, Reader.class);
                evalMethod.invoke(scriptEngine, reader);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
        
        public Bindings createBindings() {
            try {
                Method createBindingsMethod = scriptEngine.getClass().getMethod(CREATE_BINDINGS_METHOD_NAME);
                Object bindings = createBindingsMethod.invoke(scriptEngine);
                return new Bindings(bindings);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
        
        public void setContext(ScriptContext ctx) {
            try {
                Method setContextMethod = scriptEngine.getClass().getMethod(
                        SET_CONTEXT_METHOD_NAME,
                        Class.forName("javax.script.ScriptContext"));
                setContextMethod.invoke(scriptEngine, ctx.getObject());
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
    }
    
    private class Bindings {
        private final static String PUT_METHOD_NAME = "put";
        private Object bindings;
        
        public Bindings(Object bindings) {
            this.bindings = bindings;
        }
        
        public Object put(String name, Object value) {
            try {
                Method putMethod = bindings.getClass().getMethod(
                        PUT_METHOD_NAME, 
                        String.class, 
                        Object.class);
                return putMethod.invoke(bindings, name, value);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }
        
        protected Object getObject() {
            return bindings;
        }
    }
    
    private interface ScriptContext {
        public void setBindings(Bindings bindings, int scope);
        public Object getObject();
        public static final int GLOBAL_SCOPE = 200;
        public static final int ENGINE_SCOPE = 100;
    }
    
    private class SimpleScriptContext implements ScriptContext {
        private static final String SIMPLE_SCRIPT_CONTEXT_TYPE_NAME = "javax.script.SimpleScriptContext";
        private static final String SET_BINDINGS_METHOD_NAME = "setBindings";
        
        private Object simpleScriptContext;
        
        public SimpleScriptContext() {
            try {
                Class sscClass = Class.forName(SIMPLE_SCRIPT_CONTEXT_TYPE_NAME);
                @SuppressWarnings("unchecked")
                Constructor c = sscClass.getConstructor();
                simpleScriptContext = c.newInstance();
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
        }

        public Object getObject() {
            return simpleScriptContext;
        }
        
        public void setBindings(Bindings bindings, int scope) {
            try {
                Method setBindingsMethod =
                    simpleScriptContext.getClass().getMethod(
                    SET_BINDINGS_METHOD_NAME, 
                    Class.forName("javax.script.Bindings"),
                    int.class);
                setBindingsMethod.invoke(
                        simpleScriptContext, 
                        bindings.getObject(), 
                        scope);
            } catch (Throwable thr) {
                throw new RuntimeException(thr);
            }
            
        }
    }
}

