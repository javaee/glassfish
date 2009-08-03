package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.security.store.PasswordAdapter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Set;

/**  A class that's supposed to capture all the behavior common to operation on a "local" domain.
 *  It's supposed to act as the abstract base class that provides more functionality to the
 *  commands that operate on a local domain.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
public abstract class LocalDomainCommand extends CLICommand {

    protected File   domainsDir;
    protected File   domainRootDir;
    protected String domainName;
    
    private static final LocalStringsImpl strings =
            new LocalStringsImpl(LocalDomainCommand.class);
    /**
     * Constructor used by subclasses to save the name, program options,
     * and environment information into corresponding protected fields.
     * Finally, this constructor calls the initializeLogger method.
     */
    protected LocalDomainCommand(String name, ProgramOptions programOpts, Environment env) {
        super(name, programOpts, env);
    }

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        super.validate();
        initDomain();
    }
    
    protected void initDomain() throws CommandValidationException {
        if (!operands.isEmpty()) {
            domainName = operands.get(0);
        }

        // get domainsDir
        String domaindir = getOption("domaindir");

        if (ok(domaindir)) {
            domainsDir = new File(domaindir);
            if (!domainsDir.isDirectory()) {
                throw new CommandValidationException(
                        strings.get("StopDomain.badDomainsDir", domainsDir));
            }
        }
        if (domainsDir == null) {
            domainsDir = new File(getSystemProperty(
                            SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));
        }

        if (!domainsDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainsDir", domainsDir));
        }

        if (domainName != null) {
            domainRootDir = new File(domainsDir, domainName);
        } else {
            domainRootDir = getTheOneAndOnlyDomain(domainsDir);
            domainName    = domainRootDir.getName();
        }

        if (!domainRootDir.isDirectory()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.badDomainDir", domainRootDir));
        }
        domainRootDir = SmartFile.sanitize(domainRootDir);
        domainsDir    = SmartFile.sanitize(domainsDir);
    }
    
    private File getTheOneAndOnlyDomain(File parent)
            throws CommandValidationException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new CommandValidationException(
                    strings.get("noDomainDirs", parent));
        }

        if (files.length > 1) {
            throw new CommandValidationException(
                    strings.get("StopDomain.tooManyDomainDirs", parent));
        }
        return files[0];
    }
    
    protected File getDomainXml() throws CommandValidationException {
        // root-dir/config/domain.xml
        File domainXml = new File(domainRootDir, "config/domain.xml");

        if (!domainXml.canRead()) {
            throw new CommandValidationException(
                    strings.get("StopDomain.noDomainXml", domainXml));
        }
        return domainXml;
    }

    protected File getMasterPasswordFile() {
        File mp = new File(domainRootDir, "master-password");
        if (!mp.canRead())
            return null;
        return mp;
    }
    
    protected File getJKS() {
        File mp = new File(domainRootDir, "config/keystore.jks");
        if (!mp.canRead())
            return null;
        return mp;
    }

    protected boolean verifyMasterPassword(String mpv) {
        //only tries to open the keystore
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(getJKS());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, mpv.toCharArray());
            return true;
        } catch (Exception e) {
            logger.printDebugMessage(e.getMessage());
            return false;
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch(IOException ioe) {
                //ignore, I know ...
            }
        }
    }

    /** Checks if the create-domain was created using --savemasterpassword flag which obtains security
     *  by obfuscation! Returns null in case of failure of any kind.
     * @return String representing the password from the JCEKS store named master-password in domain folder
     */
    protected String checkMasterPasswordFile() {
        File mpf = getMasterPasswordFile();
        if (mpf == null)
            return null;   //no master password  saved
        try {
            PasswordAdapter pw = new PasswordAdapter(mpf.getAbsolutePath(), "master-password".toCharArray()); //fixed key
            return pw.getPasswordForAlias("master-password");
        } catch (Exception e) {
            logger.printDebugMessage("master password file reading error: " + e.getMessage());
            return null;
        }
    }

    /** Returns the admin port of the local domain. Note that this method should be called only
     *  when you own the domain that is available on accessible file system.
     *
     * @return an integer that represents admin port
     * @throws CommandException in case of parsing errors
     * @throws CommandValidationException in case of parsing errors 
     */
    protected int getAdminPort() throws CommandValidationException, CommandException {
        Integer[] ports;

        try {
            MiniXmlParser parser = new MiniXmlParser(this.getDomainXml());
            Set<Integer> portsSet = parser.getAdminPorts();
            ports = portsSet.toArray(new Integer[portsSet.size()]);
            return ports[0];
        } catch (MiniXmlParserException ex) {
            throw new CommandException("admin port not found", ex);
        }
    }

    /** There is sometimes a need for subclasses to know if a <code> local domain </code> is
     *  running. An example of such a command is change-master-password command. The stop-domain
     *  command also needs to know if a domain is running <i> without </i> having to provide user
     *  name and password on command line (this is the case when I own a domain that has non-default
     *  admin user and password) and want to stop it without providing it.
     *  <p>
     *  In such cases, we need to know if the domain is running and this method provides a way to do that.
     *
     *
     * @return boolean indicating whether the server is running
     * @throws UnsupportedOperationException for now (Kedar - 24 Jul 2009) ...
     */
    protected boolean isRunning() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();    
    }
}
