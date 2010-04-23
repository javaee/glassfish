/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.io;

import java.io.File;
import java.io.IOException;

/**
 * A class for keeping track of the directories that a domain lives in and under.
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */

public final class DomainDirs {

    /**
     * This constructor is used when both the name of the domain is known and
     * the domains-dir is known.
     */
    public DomainDirs(File domainsDir, String domainName) throws IOException {
        dirs = new ServerDirs(new File(domainsDir, domainName));
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

    public final String getDomainName() {
        return dirs.getServerName();
    }

    public final File getDomainDir() {
        return dirs.getServerDir();
    }

    public final File getDomainsDir() {
        return dirs.getServerParentDir();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private final ServerDirs dirs;
}
