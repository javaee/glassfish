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


package org.glassfish.osgiweb;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import static org.glassfish.osgiweb.Constants.FILE_PROTOCOL;
import static org.glassfish.osgiweb.Constants.REFERENCE_PROTOCOL;
import org.osgi.framework.Bundle;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Adapts a {@link Bundle} to {@link Archive}.
 * It uses JAR File space of the bundle (via getEntry and getEntryPaths APIs),
 * so a bundle does not have to be in resolved state.
 * Since it represents JAR File space of the bundle, it does not
 * consider resources from any fragments.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiBundleArchive implements ReadableArchive
{
    private Bundle b;

    private String name;

    private URI uri;

    public OSGiBundleArchive(Bundle b)
    {
        this.b = b;
        init();
    }

    /**
     * This method initializes {@link #uri} and {@link #name}
     */
    private void init() {
        // The only time we can rely on a bundle's location is when the
        // location string begins with reference: scheme, as both Felix and
        // equinox assumes that the rest of the location is a file.
        // In no other case, we can use rely on bundle.getLocation()
        // to arrive at the URI of the underlying archive.
        // e.g., user can install like this:
        // bundleContext.install ("file:/a/b.jar", new URL("file:/c/d.jar").openStream));
        // In the above case, although location returns a.jar, the actual archive
        // is read from d.jar.
        // So, we return a valid URI only for reference: scheme and in all
        // cases, we prefer to return null as opposed to throwing an exception
        // to keep the behavior same as MemoryMappedArchive.
        String location = b.getLocation();
        if (location != null && location.startsWith(REFERENCE_PROTOCOL))
        {
            location = location.substring(REFERENCE_PROTOCOL.length());

            // We only know how to handle reference:file: type urls.
            if (location.startsWith(FILE_PROTOCOL))
            {

                // Decode any URL escaped sequences.
                location = URLDecoder.decode(location);

                // Return iff referenced file exists.
                File file = new File(location.substring(FILE_PROTOCOL.length()));
                if (file.exists())
                {
                    uri = file.toURI();
                }
            }
        }

        // See issue #10536. We can't use the same policy for obtaining
        // the name as OSGi container does.
//        if (uri != null) {
//            name = Util.getURIName(uri);
//        } else {
            // See if there is a symbolic name & version. Use them,
            // else use location. Either symbolic name or location must exist
            // in a bundle.
            String symName = b.getSymbolicName();
            String version = (String)b.getHeaders().get(BUNDLE_VERSION);
            if (symName != null) {
                name = version== null ?
                        symName : symName.concat("_").concat(version);
            } else {
                name = location;
            }
//        }
    }

    public void close() throws IOException
    {
    }

    public Enumeration<String> entries()
    {
        return Collections.enumeration(entries(false)); // return only file entries as per contract of this method
    }

    public Collection<String> getDirectories() throws IOException
    {
        return entries(true);
    }

    private Collection<String> entries(boolean directories)
    {
        ArrayList<String> entries = new ArrayList<String>();
        getEntryPaths(entries, "/");
        ListIterator<String> entriesIter = entries.listIterator();
        while (entriesIter.hasNext())
        {
            String next = entriesIter.next();
            if (next.endsWith("/"))
            {
                // return only file entries as per the conract of this method
                entriesIter.remove();
            }
        }
        return entries;
    }

    private void getEntryPaths(Collection<String> entries, String path)
    {
//        BECAUSE OF A BUG IN FELIX, THE CODE BELOW DOES NOT WORK
//        WHEN THERE ARE NO DIRECTORY ENTRIES IN THE JAR FILE.
//        SO, WE ARE USING findEntries WHICH HAS AN UNDESIRED SIDE EFFECT
//        OF RETURNING ENTRIES FROM FRAGMENTS. SO, AS SOON AS THE BUG
//        IS FIXED IN FELIX, WE SHOULD SWITCH TO THE FOLLOWING CODE.
//        Enumeration<String> subPaths = b.getEntryPaths(path);
//        if (subPaths != null)
//        {
//            while (subPaths.hasMoreElements())
//            {
//                String next = subPaths.nextElement();
//                entries.add(next);
//                getEntryPaths(entries, next);
//            }
//        }
        getEntryPaths2(entries, path); // call the new implementation
    }

    private void getEntryPaths2(Collection<String> entries, String path) {
        // findEntries expect the path to begin with "/"
        Enumeration e = b.findEntries(
                path.startsWith("/") ? path : path.concat("/"), "*", true);
         if (e != null) {
             while (e.hasMoreElements()) {
                 URL next = (URL)e.nextElement();
                 String nextPath = next.getPath();
                 // As per the OSGi R4 spec,
                 // "The getPath method for a bundle entry URL must return
                 // an absolute path (a path that starts with '/') to a resource
                 // or entry in a bundle. For example, the URL returned from
                 // getEntry("myimages/test .gif ") must have a path of
                 // /myimages/test.gif.
                 entries.add(nextPath.substring(1)); // remove the leading "/"
             }
         }
    }

    public Enumeration<String> entries(String prefix)
    {
        Collection<String> entries = new ArrayList<String>();
        getEntryPaths(entries, prefix);
        return Collections.enumeration(entries);
    }

    public boolean isDirectory(String name)
    {
        return b.getEntryPaths(name) != null;
    }

    public Manifest getManifest() throws IOException
    {
        URL url = b.getEntry(JarFile.MANIFEST_NAME);
        if (url != null)
        {
            InputStream is = url.openStream();
            try
            {
                return new Manifest(is);
            }
            finally
            {
                is.close();
            }

        }
        return null;
    }

    /**
     * It returns URI for the underlying file if it can locate such a file.
     * Else, it returns null.
     * @return
     */
    public URI getURI()
    {
        return uri;
    }

    public long getArchiveSize() throws SecurityException
    {
        return -1; // Don't know how to calculate the size.
    }

    public String getName()
    {
        return name;
    }

    public InputStream getEntry(String name) throws IOException
    {
        URL entry = b.getEntry(name);
        return entry != null ? entry.openStream() : null;
    }

    public boolean exists(String name) throws IOException
    {
        return b.getEntry(name)!=null;
    }

    public long getEntrySize(String name)
    {
        return 0;
    }

    public void open(URI uri) throws IOException
    {
        throw new UnsupportedOperationException("Not supported");
    }

    public ReadableArchive getSubArchive(String name) throws IOException
    {
        return null;
    }

    public boolean exists()
    {
        return true;
    }

    public boolean delete()
    {
        return false;
    }

    public boolean renameTo(String name)
    {
        return false;
    }

    public void setParentArchive(ReadableArchive parentArchive)
    {
        // Not needed until we support ear file containing bundles.
        throw new UnsupportedOperationException("Not supported");
    }

    public ReadableArchive getParentArchive()
    {
        return null;
    }
}
