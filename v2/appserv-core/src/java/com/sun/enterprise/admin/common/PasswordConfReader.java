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

package com.sun.enterprise.admin.common;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Provides api to read passwords from password conf file.
 */
public final class PasswordConfReader
{
    public static final String KEY_STORE_ALIAS   = "KeyStore";

    public static final String TRUST_STORE_ALIAS = "TrustStore";

    public static final String PASSWORD_FILE_PROPERTY =
        "com.sun.aas.ssl.passwordfile";

    static final String CONFIG          = "config";

    static final String PASSWORD_CONF   = "password.conf";

    private static final String INSTANCE_ROOT =
        System.getProperty("com.sun.aas.instanceRoot");

    private static final String SERVER_NAME_PROPERTY = "com.sun.aas.instanceName";

    private static PasswordFile     pf;
    private static Hashtable        entries;

    /* Constructs new PasswordConfReader object */
    private PasswordConfReader()
    {
    }

    /**
     */
    public static String getKeyStorePassword() throws IOException
    {
        return getPassword(KEY_STORE_ALIAS);
    }

    /**
     */
    public static String getTrustStorePassword() throws IOException
    {
        return getPassword(TRUST_STORE_ALIAS);
    }

    /**
     */
    public static String getPassword(String alias) throws IOException
    {
        if (!isInSync()) { sync(); }
        return get(alias);
    }

    public static Enumeration listAliases() throws IOException
    {
        if (!isInSync()) { sync(); }
        return entries.keys();
    }

    private static boolean isInSync() throws IOException
    {
        return getPasswordFile().equals(pf);
    }

    private static void sync() throws IOException
    {
        synchronized (PasswordConfReader.class)
        {
            pf = getPasswordFile();
        }
        loadEntries();
    }

    private static PasswordFile getPasswordFile() throws IOException
    {
        final String prop = System.getProperty(PASSWORD_FILE_PROPERTY);
        PasswordFile f;
        if ((prop != null) && (prop.length() > 0))
        {
            f = new PasswordFile(prop);
        }
        else
        {
            f = new PasswordFile(getDefaultPasswordConf());
        }
        return f;
    }

    private static String getDefaultPasswordConf()
    {
        String defaultConf = null;
        if (INSTANCE_ROOT != null)
        {
            defaultConf =   INSTANCE_ROOT + File.separator
                            + File.separator + CONFIG + File.separator
                            + PASSWORD_CONF;
        }
        return defaultConf;
    }

    private static synchronized void loadEntries() throws IOException
    {
        if (entries == null)
        {
            entries = new Hashtable();
        }
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(pf.getPath()));
            String str;
            while ((str = reader.readLine()) != null)
            {
                int index = str.indexOf(':');
                if (index > 0)
                {
                    entries.put(str.substring(0, index),
                                str.substring(index+1));
                }
            }
        }
        finally
        {
            if (reader != null) { reader.close(); }
        }
    }

    private static synchronized String get(String key) throws IOException
    {
        final String password = (String)entries.get(key);
        if (password == null)
        {
            throw new IOException("No entry found for " + key);
        }
        return password;
    }

    private static final class PasswordFile
    {
        private String      path;
        private long        lastModified;

        private PasswordFile(String path) throws IOException
        {
            final File f = new File(path).getCanonicalFile();
            if (!f.exists())
            {
                throw new IOException("Password file does not exist. "
                                      + f.getAbsolutePath());
            }
            this.path = f.getAbsolutePath();
            lastModified = f.lastModified();
        }

        public String getPath()
        {
            return path;
        }

        public boolean equals(Object o)
        {
            if (o == null) { return false; }
            if (o == this) { return true; }
            PasswordFile that = (PasswordFile)o;
            return (path.equals(that.path) &&
                    (lastModified == that.lastModified));
        }

        public String toString()
        {
            return "Path = " + getPath() + ' ' + "lastModified = " + lastModified;
        }
    }
}
