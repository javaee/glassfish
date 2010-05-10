/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.io;

import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * A class for keeping track of the directories that a domain lives in and under.
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public final class DomainDirs {
    /**
     * This convenience constructor is used when nothing is known about the
     * domain-dir
     */
    public DomainDirs() throws IOException {
        this(null, null);
    }

    /**
     * This constructor is used when both the name of the domain is known and
     * the domains-dir is known.
     */
    public DomainDirs(File domainsDir, String domainName) throws IOException {
        Map<String, String> systemProps = new ASenvPropertyReader().getProps();

        if(domainsDir == null || !domainsDir.isDirectory()) {
            String defDomains =
                systemProps.get(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);

            if(defDomains == null)
                throw new IOException(strings.get("Domain.noDomainsDir",
                        SystemPropertyConstants.DOMAINS_ROOT_PROPERTY));

            domainsDir = new File(defDomains);
        }

        if(!domainsDir.isDirectory()) {
            throw new IOException(strings.get("Domain.badDomainsDir", domainsDir));
        }

        File domainDir;

        if(domainName != null) {
            domainDir = new File(domainsDir, domainName);
        }
        else {
            domainDir = getTheOneAndOnlyDir(domainsDir);
        }

        if(!domainDir.isDirectory()) {
            throw new IOException(strings.get("Domain.badDomainDir", domainDir));
        }

        dirs = new ServerDirs(domainDir);
    }

    /**
     * This constructor is used when the path of the domain-directory is known.
     * @param domainsDir
     * @param domainName
     * @throws IOException
     */
    public DomainDirs(File domainDir) throws IOException {
        dirs = new ServerDirs(domainDir);
    }

    /**
     * Create a DomainDir from the more general ServerDirs instance.
     * along with getServerDirs() you can convert freely back and forth
     *
     * @param aServerDir
     */
    public DomainDirs(ServerDirs sd) {
        dirs = sd;
    }

    @Override
    public String toString() {
        return dirs.toString();
    }

    public final String getDomainName() {
        return dirs.getServerName();
    }

    public final File getDomainDir() {
        return dirs.getServerDir();
    }

    public final File getDomainsDir() {
        return dirs.getServerParentDir();
    }

    public final ServerDirs getServerDirs() {
        return dirs;
    }

    public final boolean isValid() {
        try {
            return dirs.isValid();
        }
        catch(Exception e) {
            return false;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private File getTheOneAndOnlyDir(File parent) throws IOException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if(files == null || files.length == 0)
            throw new IOException(strings.get("Domain.noDomainDirs", parent));

        if(files.length > 1) {
            String names = "";
            
            for(int i = 0 ; i < files.length; i++) {
                if(i > 0)
                    names += ", ";
                names += files[i].getName();
            }
            
            throw new IOException(strings.get("Domain.tooManyDomainDirs", names));
        }

        return files[0];
    }

    private final ServerDirs dirs;
    private final static LocalStringsImpl strings = new LocalStringsImpl(DomainDirs.class);
}
