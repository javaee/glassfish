/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakSet;

/** A lookup that implements the JDK1.3 JAR services mechanism and delegates
 * to META-INF/services/name.of.class files.
 * <p>It is not dynamic - so if you need to change the classloader or JARs,
 * wrap it in a ProxyLookup and change the delegate when necessary.
 * Existing instances will be kept if the implementation classes are unchanged,
 * so there is "stability" in doing this provided some parent loaders are the same
 * as the previous ones.
 * <p>If this is to be made public, please move it to the org.openide.util.lookup
 * package; currently used by the core via reflection, until it is needed some
 * other way.
 * @author Jaroslav Tulach, Jesse Glick
 * @see "#14722"
 */
final class MetaInfServicesLookup extends AbstractLookup {

    private static final Logger LOGGER = Logger.getLogger(MetaInfServicesLookup.class.getName());

    private static final Map<Class,Object> knownInstances = new WeakHashMap<Class,Object>();

    /** A set of all requested classes.
     * Note that classes that we actually succeeded on can never be removed
     * from here because we hold a strong reference to the loader.
     * However we also hold classes which are definitely not loadable by
     * our loader.
     */
    private final Set<Class> classes = new WeakSet<Class>(); // Set<Class>

    /** class loader to use */
    private final ClassLoader loader;

    /** Create a lookup reading from the classpath.
     * That is, the same classloader as this class itself.
     */
    public MetaInfServicesLookup() {
        this(MetaInfServicesLookup.class.getClassLoader());
    }

    /** Create a lookup reading from a specified classloader.
     */
    public MetaInfServicesLookup(ClassLoader loader) {
        this.loader = loader;

        LOGGER.log(Level.FINE, "Created: {0}", this);
    }

    public String toString() {
        return "MetaInfServicesLookup[" + loader + "]"; // NOI18N
    }

    /* Tries to load appropriate resources from manifest files.
     */
    protected final void beforeLookup(Lookup.Template t) {
        Class c = t.getType();

        HashSet<AbstractLookup.R> listeners;

        synchronized (this) {
            if (classes.add(c)) {
                // Added new class, search for it.
                LinkedHashSet<AbstractLookup.Pair<?>> arr = getPairsAsLHS();
                search(c, arr);

                // listeners are notified under while holding lock on class c, 
                // let say it is acceptable now
                listeners = setPairsAndCollectListeners(arr);
            } else {
                // ok, nothing needs to be done
                return;
            }
        }

        notifyCollectedListeners(listeners);
    }

    /** Finds all pairs and adds them to the collection.
     *
     * @param clazz class to find
     * @param result collection to add Pair to
     */
    private void search(Class<?> clazz, Collection<AbstractLookup.Pair<?>> result) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Searching for " + clazz.getName() + " in " + clazz.getClassLoader() + " from " + this);
        }

        String res = "META-INF/services/" + clazz.getName(); // NOI18N
        Enumeration<URL> en;

        try {
            en = loader.getResources(res);
        } catch (IOException ioe) {
            // do not use ErrorManager because we are in the startup code
            // and ErrorManager might not be ready
            ioe.printStackTrace();

            return;
        }

        // Do not create multiple instances in case more than one JAR
        // has the same entry in it (and they load to the same class).
        // Probably would not happen, assuming JARs only list classes
        // they own, but just in case...
        List<Item> foundClasses = new ArrayList<Item>();
        Collection<Class> removeClasses = new ArrayList<Class>();

        boolean foundOne = false;

        while (en.hasMoreElements()) {
            if (!foundOne) {
                foundOne = true;

                // Double-check that in fact we can load the *interface* class.
                // For example, say class I is defined in two JARs, J1 and J2.
                // There is also an implementation M1 defined in J1, and another
                // implementation M2 defined in J2.
                // Classloaders C1 and C2 are made from J1 and J2.
                // A MetaInfServicesLookup is made from C1. Then the user asks to
                // lookup I as loaded from C2. J1 has the services line and lists
                // M1, and we can in fact make it. However it is not of the desired
                // type to be looked up. Don't do this check, which could be expensive,
                // unless we expect to be getting some results, however.
                Class realMcCoy = null;

                try {
                    realMcCoy = loader.loadClass(clazz.getName());
                } catch (ClassNotFoundException cnfe) {
                    // our loader does not know about it, OK
                }

                if (realMcCoy != clazz) {
                    // Either the interface class is not available at all in our loader,
                    // or it is not the same version as we expected. Don't provide results.
                    if (LOGGER.isLoggable(Level.FINER)) {
                        if (realMcCoy != null) {
                            LOGGER.log(Level.FINER,
                                clazz.getName() + " is not the real McCoy! Actually found it in " +
                                realMcCoy.getClassLoader()
                            ); // NOI18N
                        } else {
                            LOGGER.log(Level.FINER, clazz.getName() + " could not be found in " + loader); // NOI18N
                        }
                    }

                    return;
                }
            }

            URL url = en.nextElement();
            Item currentItem = null;

            try {
                InputStream is = url.openStream();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N

                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        line = line.trim();

                        // is it position attribute?
                        if (line.startsWith("#position=")) {
                            if (currentItem == null) {
                                LOGGER.log(Level.WARNING, "Found line '{0}' in {1} but there is no item to associate it with", new Object[] {line, url});
                                continue;
                            }

                            try {
                                currentItem.position = Integer.parseInt(line.substring(10));
                            } catch (NumberFormatException e) {
                                // do not use ErrorManager because we are in the startup code
                                // and ErrorManager might not be ready
                                e.printStackTrace();
                            }
                        }

                        if (currentItem != null) {
                            insertItem(currentItem, foundClasses);
                            currentItem = null;
                        }

                        // Ignore blank lines and comments.
                        if (line.length() == 0) {
                            continue;
                        }

                        boolean remove = false;

                        if (line.charAt(0) == '#') {
                            if ((line.length() == 1) || (line.charAt(1) != '-')) {
                                continue;
                            }

                            // line starting with #- is a sign to remove that class from lookup
                            remove = true;
                            line = line.substring(2);
                        }

                        Class inst = null;

                        try {
                            // Most lines are fully-qualified class names.
                            inst = Class.forName(line, false, loader);
                        } catch (ClassNotFoundException cnfe) {
                            if (remove) {
                                // if we are removing somthing and the something
                                // cannot be found it is ok to do nothing
                                continue;
                            } else {
                                // but if we are not removing just rethrow
                                throw cnfe;
                            }
                        }

                        if (!clazz.isAssignableFrom(inst)) {
                            throw new ClassNotFoundException(inst.getName() + " not a subclass of " + clazz.getName()); // NOI18N
                        }

                        if (remove) {
                            removeClasses.add(inst);
                        } else {
                            // create new item here, but do not put it into
                            // foundClasses array yet because following line
                            // might specify its position
                            currentItem = new Item();
                            currentItem.clazz = inst;
                        }
                    }

                    if (currentItem != null) {
                        insertItem(currentItem, foundClasses);
                        currentItem = null;
                    }
                } finally {
                    is.close();
                }
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        LOGGER.log(Level.FINER, "Found impls of {0}: {1} and removed: {2} from: {3}", new Object[] {clazz.getName(), foundClasses, removeClasses, this});

        foundClasses.removeAll(removeClasses);

        for (Item item : foundClasses) {
            if (removeClasses.contains(item.clazz)) {
                continue;
            }

            result.add(new P(item.clazz));
        }
    }

    /**
     * Insert item to the list according to item.position value.
     */
    private void insertItem(Item item, List<Item> list) {
        // no position? -> add it to the end
        if (item.position == -1) {
            list.add(item);

            return;
        }

        int index = -1;
        for (Item i : list) {
            index++;

            if (i.position == -1) {
                list.add(index, item);

                return;
            } else {
                if (i.position > item.position) {
                    list.add(index, item);

                    return;
                }
            }
        }

        list.add(item);
    }

    private static class Item {
        private Class clazz;
        private int position = -1;
        @Override
        public String toString() {
            return "MetaInfServicesLookup.Item[" + clazz.getName() + "]"; // NOI18N
        }
    }

    /** Pair that holds name of a class and maybe the instance.
     */
    private static final class P extends AbstractLookup.Pair<Object> {
        /** May be one of three things:
         * 1. The implementation class which was named in the services file.
         * 2. An instance of it.
         * 3. Null, if creation of the instance resulted in an error.
         */
        private Object object;

        public P(Class<?> clazz) {
            this.object = clazz;
        }

        /** Finds the class.
         */
        private Class<? extends Object> clazz() {
            Object o = object;

            if (o instanceof Class) {
                return (Class<? extends Object>) o;
            } else if (o != null) {
                return o.getClass();
            } else {
                // Broken.
                return Object.class;
            }
        }

        public boolean equals(Object o) {
            if (o instanceof P) {
                return ((P) o).clazz().equals(clazz());
            }

            return false;
        }

        public int hashCode() {
            return clazz().hashCode();
        }

        protected boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(clazz());
        }

        public Class<?> getType() {
            return clazz();
        }

        public Object getInstance() {
            Object o = object; // keeping local copy to avoid another

            // thread to modify it under my hands
            if (o instanceof Class) {
                synchronized (o) { // o is Class and we will not create 
                                   // 2 instances of the same class

                    try {
                        Class<?> c = ((Class) o);

                        synchronized (knownInstances) { // guards only the static cache
                            o = knownInstances.get(c);
                        }

                        if (o == null) {
                            if (SharedClassObject.class.isAssignableFrom(c)) {
                                o = SharedClassObject.findObject(c.asSubclass(SharedClassObject.class), true);
                            } else {
                                o = c.newInstance();
                            }

                            synchronized (knownInstances) { // guards only the static cache
                                knownInstances.put(c, o);
                            }
                        }

                        // Do not assign to instance var unless there is a complete synch
                        // block between the newInstance and this line. Otherwise we could
                        // be assigning a half-constructed instance that another thread
                        // could see and return immediately.
                        object = o;
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                        object = null;
                    }
                }
            }

            return object;
        }

        public String getDisplayName() {
            return clazz().getName();
        }

        public String getId() {
            return clazz().getName();
        }

        protected boolean creatorOf(Object obj) {
            return obj == object;
        }
    }
}
