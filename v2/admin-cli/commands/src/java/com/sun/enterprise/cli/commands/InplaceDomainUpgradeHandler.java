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
package com.sun.enterprise.cli.commands;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.InputStream;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/** A class that determines behavior of invoking the In-place upgrade for a given
 *  domain. It revolves around the com.sun.appserv.server.util.Version class and
 *  information returned from there. It matches the information that is returned
 *  by the <em> current version </em> and the expected System ID for the domain.xml.
 *  Whenever a mismatch occurs, an in-place upgrade results.
 *  It is also assumed that in-place upgrade will <i> not </i> be invoked for
 *  the update releases because the DTD version won't change across them. Thus, a
 *  domain created with version 9.1 should work <b>without</b> in-place upgrade using
 *  application server binary version 9.1_01, 9.1_02 etc. The most important thing
 *  to note is that the <b> Version </b> information denotes the binary version
 *  of the application server installation and <b> DTD's System ID </b> denotes
 *  the application server binaries that initially created the domain.
 *  @see com.sun.appserv.server.util.Version
 *  @since GlassFish V2
 */
final class InplaceDomainUpgradeHandler {
    /** The key representing current version of the software binaries */
    private final String versionKey;
    /** The File representing domain.xml */
    private final File domainXmlFile;

    /** The File representing domain's config folder */
    private final File domainConfigFolder;
    
    /** The File whose status determines if upgrade is needed */
    private final File requiredUpgradedToFile;

    private final DomainConfig dc;
    private static final StringManager lsm = StringManager.getManager(InplaceDomainUpgradeHandler.class);
    //this is needed because as it is, refactoring is hard in CLI!
    
    private String mSystemId; //this is the system ID of the document to be parsed (domain.xml)
    
    private static final String FILE_PREFIX = ".upgradedTo";
    private static final Map<String, String> VERSION_DTD_SYS_ID_MAP = new HashMap<String, String>();
    
    /* Implementation note: Every time the DTD version changes, we have got to add
     * a mapping to the following map. Sorry, I could not automate it, it is too
     * tricky to get it right.
     */
    private static final String ONEO = "sun-domain_1_0.dtd";
    private static final String ONE1 = "sun-domain_1_1.dtd";
    private static final String ONE2 = "sun-domain_1_2.dtd";
    private static final String ONE3 = "sun-domain_1_3.dtd";

    static {        
        VERSION_DTD_SYS_ID_MAP.put("80", "http://www.sun.com/software/appserver/dtds/" + ONEO);
        VERSION_DTD_SYS_ID_MAP.put("81", "http://www.sun.com/software/appserver/dtds/" + ONE1);
        VERSION_DTD_SYS_ID_MAP.put("82", "http://www.sun.com/software/appserver/dtds/" + ONE1);
        VERSION_DTD_SYS_ID_MAP.put("90", "http://www.sun.com/software/appserver/dtds/" + ONE2);
        VERSION_DTD_SYS_ID_MAP.put("91", "http://www.sun.com/software/appserver/dtds/" + ONE3);
    }
    
    public InplaceDomainUpgradeHandler(final DomainConfig dc) {
        if (dc == null)
            throw new IllegalArgumentException ("null arguments");
        this.dc                     = dc;
        this.domainXmlFile          = new PEFileLayout(dc).getDomainConfigFile();
        if (! domainXmlFile.exists() || ! domainXmlFile.canRead()) {
            final String msg = lsm.getString("InplaceUpgradeDomainNotReadable", dc.getDomainName(), dc.getDomainRoot());
            throw new IllegalArgumentException(msg);
        }
        this.domainConfigFolder     = domainXmlFile.getParentFile();
        this.versionKey             = Version.getMajorVersion() + Version.getMinorVersion();
        this.requiredUpgradedToFile = new File(domainConfigFolder, FILE_PREFIX + versionKey);
        /* The way the build is set up, the following holds:
         * Release           Major Version      Minor Version     versionKey
         * 8.0 PE/EE          8                 0                 80
         * 8.0 UR1 PE/EE      8                 0_01              8001
         * 8.1 PE/EE          8                 1                 81
         * 8.1 UR2 PE/EE      8                 1_02              8102
         * 8.2 PE/EE          8                 2                 82
         * 9.0 PE             9                 0                 90
         * 9.1                9                 1                 91
         */
    }

    void touchUpgradedToFile() throws IOException {
        this.requiredUpgradedToFile.createNewFile();
    }
    
    boolean needsUpgrade() {
        boolean needed = false;
        if (!this.requiredUpgradedToFile.exists()) {
            final String fromDtd = getSystemIdFromDtd();
            final String fromMap = getSystemIdFromMap();
            if (!fromMap.equals(fromDtd)) {
                needed = true;
                final String msg = lsm.getString("InplaceUpgradeNeeded",
                        dc.getDomainName(), fromDtd, fromMap);
                CLILogger.getInstance().printMessage(msg);
            }
            else { //the system ids do match and no upgrade is necessary
                final String msg = lsm.getString("InplaceUpgradeNotNeededDtdsMatch",
                        fromDtd);
                CLILogger.getInstance().printDebugMessage(msg);
            }
        } else { //upgrade is already taken care of, for this release
            final String msg = lsm.getString("InplaceUpgradeAlreadyDone", 
                    requiredUpgradedToFile.getName());
            CLILogger.getInstance().printDebugMessage(msg);
        }
        return ( needed );
    }
        
    private String getSystemIdFromDtd() {
        //Use Streaming XML parser, returns null in case of parsing error
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(domainXmlFile));
            final XMLReader     xr = XMLReaderFactory.createXMLReader();
            final InputSource   is = new InputSource(bis);
            xr.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(final String pid, final String sid)
                    throws SAXException, IOException {
                        if (sid != null) {
                            mSystemId = sid.trim();
                            final String dtdName = sid.substring(sid.lastIndexOf("/"));
                            final File dtdFile   = new File(new PEFileLayout(dc).getDtdsDir(), dtdName);
                            if (dtdFile.exists()) {
                                return new InputSource(new BufferedInputStream(new FileInputStream(dtdFile)));
                            } //else default resolution
                        } //else default resolution
                        return ( null );
                    }                
            });
            xr.parse(is);
            return ( mSystemId );
        } catch (final Exception ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch(Exception ee) {
                //squelching ee on purpose
            }
        }
    }
    
    private String getSystemIdFromMap() {
        String id = "";
        //this is where the assumption holds -- for update release, the upgrade is not needed
        for (final String key : VERSION_DTD_SYS_ID_MAP.keySet()) {
            if (key.startsWith(versionKey)) {
                id = VERSION_DTD_SYS_ID_MAP.get(key);
            }
        }
        // if there is no such key, return an empty string -- this mostly means something's wrong
        return ( id );
    }
}