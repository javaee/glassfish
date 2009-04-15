/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.persistence.jpa;


import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.util.Enumeration;
import java.io.IOException;


/**
 * Implementation of the Sniffer for JPA.
 *
 * @author Mitesh Meswani
 */
@Service(name="jpaSniffer")
@Scoped(Singleton.class)
public class JPASniffer  extends GenericSniffer implements Sniffer {

    private static final String[] containers = { "org.glassfish.persistence.jpa.JPAContainer" };

    public JPASniffer() {
        // We do not haGenericSniffer(String containerName, String appStigma, String urlPattern
        super("jpa", null /* appStigma */, null /* urlPattern */);
    }

    private static char SEPERATOR_CHAR = '/';
    private static final String WEB_INF                  = "WEB-INF";
    private static final String LIB                      = "lib";
    private static final String WEB_INF_LIb              = WEB_INF + SEPERATOR_CHAR + LIB;
    private static final String WEB_INF_CLASSSES         = WEB_INF + SEPERATOR_CHAR + "classes";
    private static final String META_INF_PERSISTENCE_XML = "META-INF" + SEPERATOR_CHAR + "persistence.xml";
    private static final String WEB_INF_CLASSSES_META_INF_PERSISTENCE_XML = WEB_INF_CLASSSES + SEPERATOR_CHAR + META_INF_PERSISTENCE_XML;
    private static final String JAR_SUFFIX = ".jar";
    /**
     * Returns true if the archive contains persistence.xml as defined by packaging rules of JPA
     * Tries to getResource("META-INF/persitsence.xml") on curernt classLoader. If it succeeds, currrent archive is a pu
     * root.
     * This method will be called for each bundle inside an application which would include
     * .war (the resource can be present in WEB-INF/classes or WEB-INF/lib/pu.jar),
     * ejb.jar (the resource can be present in root of the jar),
     * .ear (the resource can be present in lib dir of the ear)
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
            boolean isJPAArchive = false;

            // scan for persistence.xml in expected locations. If at least one is found, this is
            // a jpa archive

            //Scan for the war case
            if(isEntryPresent(location, WEB_INF)) {
                // First check for  "WEB-INF/classes/META-INF/persistence.xml"
                isJPAArchive = isEntryPresent(location, WEB_INF_CLASSSES_META_INF_PERSISTENCE_XML);
                if (!isJPAArchive) {
                    // Check in WEB-INF/lib dir
                    if (isEntryPresent(location, WEB_INF_LIb)) {
                        Enumeration<String> entries = location.entries(WEB_INF_LIb);
                        isJPAArchive = scanForPURootsInLibDir(location, entries);
                    } // if (isEntryPresent(location, WEB_INF_LIb))
                } // if (!isJPAArchive)
            } else {
                // This can be ejb jar or ear

                //Check for ejb jar case
                isJPAArchive = isEntryPresent(location, META_INF_PERSISTENCE_XML);

                // ear case, scan in lib of ear
                // TODO we are hardcoding scanning in "lib" dir for now. Check from Hong how can get user specified "lib" dir for an ear from dol
                if (isEntryPresent(location, LIB)) {
                    Enumeration<String> entries = location.entries(LIB);
                    isJPAArchive = scanForPURootsInLibDir(location, entries);
                } // if (isEntryPresent(location, WEB_INF_LIb))
            }
            return isJPAArchive;
        }

    private boolean scanForPURootsInLibDir(ReadableArchive parentArchive, Enumeration<String> entries) {
        boolean puRootPresent = false;
        while (entries.hasMoreElements() && !puRootPresent) {
            String entryName = entries.nextElement();
            if (entryName.endsWith(JAR_SUFFIX) && // a jar in lib dir
                    entryName.indexOf(SEPERATOR_CHAR, WEB_INF_LIb.length() + 1 ) == -1 ) { // && not WEB-INf/lib/foo/bar.jar
                try {
                    ReadableArchive jarInLib = parentArchive.getSubArchive(entryName);
                    puRootPresent = isEntryPresent(jarInLib, META_INF_PERSISTENCE_XML);
                    jarInLib.close();
                } catch (IOException e) {
                    // Something went wrong while reading the jar. Do not attempt to scan it
                } // catch
            } // if (entryName.endsWith(JAR_SUFFIX))
        } // while
        return puRootPresent;
    }

    private boolean isEntryPresent(ReadableArchive location, String entry) {
            boolean entryPresent = false;
            try {
                entryPresent = location.exists(entry);
            } catch (IOException e) {
                // ignore
            }
            return entryPresent;
        }

    public String[] getContainersNames() {
        return containers;
    }
}

