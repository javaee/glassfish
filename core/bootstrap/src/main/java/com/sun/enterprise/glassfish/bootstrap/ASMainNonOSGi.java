/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.glassfish.bootstrap;

import static com.sun.enterprise.glassfish.bootstrap.ASMainHelper.deleteRecursive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Base class used for launchers used during non-OSGi mode
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Jerome.Dochez@Sun.COM
 */
public abstract class ASMainNonOSGi extends AbstractMain
{
    protected void setUpCache(File sourceDir, File cacheDir) throws IOException
    {
        // Nothing to do
    }

    protected void flushAndCreate(File cacheDir, long lastModified) throws IOException
    {
        if (cacheDir.exists() && cacheDir.isDirectory())
        {
            // remove this old cache so felix creates a new one.
            getLogger().info("Removing cache dir " + cacheDir + " left from a previous run");
            if (!deleteRecursive(cacheDir))
            {
                getLogger().warning("Not able to delete " + cacheDir);
            }
        }

        if (!createCache(cacheDir))
        {
            throw new IOException("Could not create cache");
        }

        // now record our new LastModified
        try
        {
            saveCacheInformation(cacheDir, cacheDir.toURI().toURL().toString(), lastModified);
        }
        catch (MalformedURLException e)
        {
            getLogger().log(Level.SEVERE, "Could not save cache metadata, cache will be reset at next startup", e);
        }
    }

    private Properties loadCacheInformation(File cacheDir)
    {

        long recordedLastModified = 0;
        Properties persistedInfo = new Properties();
        File lastModifiedFile = new File(cacheDir.getParentFile(), cacheDir.getName() + ".lastmodified");
        if (lastModifiedFile.exists())
        {

            InputStream is = null;
            try
            {
                is = new BufferedInputStream(new FileInputStream(lastModifiedFile));
                persistedInfo.load(is);
                try
                {
                    recordedLastModified = Long.parseLong(persistedInfo.getProperty("LastModified"));
                    // check that we have not moved our domain's directory, felix is sensitive to absolute path
                    String location = persistedInfo.getProperty("Location");
                    if (!cacheDir.toURI().toURL().toString().equals(location))
                    {
                        recordedLastModified = 0;
                    }
                }
                catch (NumberFormatException e)
                {
                    recordedLastModified = 0;
                }
            }
            catch (IOException e)
            {
                getLogger().info("Cannot read recorded lastModified, OSGi cache will be flushed");
            }
            finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                    }
                    ;
                }
            }
        }
        return persistedInfo;
    }

    private long parse(Properties info, String name)
    {

        try
        {
            return Long.parseLong(info.getProperty(name));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private void saveCacheInformation(File cacheDir, String location, long lastModified)
    {
        // now record our new LastModified
        ObjectOutputStream os = null;
        File lastModifiedFile = new File(cacheDir.getParentFile(), cacheDir.getName() + ".lastmodified");
        try
        {
            lastModifiedFile.delete();
            if (!lastModifiedFile.createNewFile())
            {
                getLogger().warning("Cannot create new lastModified file");
                return;
            }
            os = new ObjectOutputStream(new FileOutputStream(lastModifiedFile));
            Properties persistedInfo = new Properties();
            persistedInfo.put("LastModified", (new Long(lastModified).toString()));
            persistedInfo.put("Location", cacheDir.toURI().toURL().toString());
            persistedInfo.store(os, null);

        }
        catch (IOException e)
        {
            getLogger().info("Cannot create record of lastModified file");
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    boolean isCacheOutdated(long lastModified, File cacheDir)
    {
        Properties persistedInfo = loadCacheInformation(cacheDir);
        long recordedLastModified = parse(persistedInfo, "LastModified");
        // check that we have not moved our domain's directory, felix is sensitive to absolute path
        String location = persistedInfo.getProperty("Location");
        try
        {
            if (!cacheDir.toURI().toURL().toString().equals(location))
            {
                recordedLastModified = 0;
            }
        }
        catch (MalformedURLException e)
        {
            getLogger().log(Level.SEVERE, "Could not load cache metadata, cache will be reset", e);
            recordedLastModified = 0;
        }

        // if the recordedLastModified is different than our most recent entry,
        // we flush the felix cache, otherwise we reuse it.
        return (recordedLastModified != lastModified);
    }

    abstract boolean createCache(File cacheDir) throws IOException;


}
