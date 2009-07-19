package com.sun.enterprise.admin.cli;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.io.File;
import java.io.FileFilter;

/**
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
}
