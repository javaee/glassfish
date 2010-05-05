/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.io.File;
import com.sun.enterprise.util.io.ServerDirs;
import java.security.KeyStore;
import java.util.Set;
import org.glassfish.api.admin.CommandException;

/**
 * A class that's supposed to capture all the behavior common to operation
 * on a "local" server.
 * @author Byron Nevins
 */
public abstract class LocalServerCommand extends CLICommand {

    /**
     * Returns the admin port of the local domain. Note that this method should
     * be called only when you own the domain that is available on accessible
     * file system.
     *
     * @return an integer that represents admin port
     * @throws CommandException in case of parsing errors
     */
    protected int getAdminPort()
            throws CommandException {

        try {
            MiniXmlParser parser = new MiniXmlParser(getDomainXml());
            Set<Integer> portsSet = parser.getAdminPorts();

            if(portsSet.size() > 0)
                return portsSet.iterator().next();
            else
                throw new CommandException("admin port not found");
        }
        catch (MiniXmlParserException ex) {
            throw new CommandException("admin port not found", ex);
        }
    }

    protected final void setServerDirs(ServerDirs sd) {
        serverDirs = sd;
    }

    protected final void resetServerDirs() throws IOException {
        serverDirs = serverDirs.refresh();
    }

    protected final ServerDirs getServerDirs() {
        return serverDirs;
    }

    protected File getDomainXml() {
        return serverDirs.getDomainXml();
    }
    
    private final File getMasterPasswordFile() {

        if(serverDirs == null)
            return null;

        File mp = new File(serverDirs.getServerDir(), "master-password");
        if(!mp.canRead())
            return null;

        return mp;
    }

    /**
     * Checks if the create-domain was created using --savemasterpassword flag
     * which obtains security by obfuscation! Returns null in case of failure
     * of any kind.
     * @return String representing the password from the JCEKS store named
     *          master-password in domain folder
     */
    protected final  String readFromMasterPasswordFile() {
        File mpf = getMasterPasswordFile();
        if(mpf == null)
            return null;   // no master password  saved
        try {
            PasswordAdapter pw = new PasswordAdapter(mpf.getAbsolutePath(),
                    "master-password".toCharArray()); // fixed key
            return pw.getPasswordForAlias("master-password");
        }
        catch (Exception e) {
            logger.printDebugMessage("master password file reading error: "
                    + e.getMessage());
            return null;
        }
    }

    protected final  boolean verifyMasterPassword(String mpv) {
        // only tries to open the keystore
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getJKS());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, mpv.toCharArray());
            return true;
        }
        catch (Exception e) {
            logger.printDebugMessage(e.getMessage());
            return false;
        }
        finally {
            try {
                if(fis != null)
                    fis.close();
            }
            catch (IOException ioe) {
                // ignore, I know ...
            }
        }
    }

    private final File getJKS() {
        if(serverDirs == null)
            return null;

        File mp = new File(new File(serverDirs.getServerDir(), "config"), "keystore.jks");
        if (!mp.canRead())
            return null;
        return mp;
    }

    /**
     * Get the master password, either from a password file or
     * by asking the user.
     */
    protected final String getMasterPassword() throws CommandException {
        // Sets the password into the launcher info.
        // Yes, returning master password as a string is not right ...
        final int RETRIES = 3;
        long t0 = System.currentTimeMillis();
        String mpv  = passwords.get(CLIConstants.MASTER_PASSWORD);
        if (mpv == null) { //not specified in the password file
            mpv = "changeit";  //optimization for the default case -- see 9592
            if (!verifyMasterPassword(mpv)) {
                mpv = readFromMasterPasswordFile();
                if (!verifyMasterPassword(mpv)) {
                    mpv = retry(RETRIES);
                }
            }
        } else { // the passwordfile contains AS_ADMIN_MASTERPASSWORD, use it
            if (!verifyMasterPassword(mpv))
                mpv = retry(RETRIES);
        }
        long t1 = System.currentTimeMillis();
        logger.printDebugMessage("Time spent in master password extraction: " +
                                    (t1-t0) + " msec");       //TODO
        return mpv;
    }


    private String retry(int times) throws CommandException {
        String mpv;
        // prompt times times
        for (int i = 0 ; i < times; i++) {
            // XXX - I18N
            String prompt = strings.get("mp.prompt", (times-i));
            mpv = super.readPassword(prompt);
            if (mpv == null)
                throw new CommandException(strings.get("no.console"));
                // ignore retries :)
            if (verifyMasterPassword(mpv))
                return mpv;
            if (i < (times-1))
                logger.printMessage(strings.get("retry.mp"));
            // make them pay for typos?
            //Thread.currentThread().sleep((i+1)*10000);
        }
        throw new CommandException(strings.get("mp.giveup", times));
    }

    protected final LocalStringsImpl getStrings() {
        return strings;
    }

    /////////////////////// private variables ////////////////////

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(LocalDomainCommand.class);

    private ServerDirs serverDirs;


}


