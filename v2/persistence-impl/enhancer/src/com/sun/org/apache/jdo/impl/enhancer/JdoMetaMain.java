/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer;

import java.io.PrintWriter;
import java.io.IOException;

import java.util.Properties;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.model.EnhancerMetaDataJDOModelImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.prop.EnhancerMetaDataPropertyImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataTimer;

/**
 * Base class for JDO command line enhancer and tests.
 *
 * @author Martin Zaun
 */
public class JdoMetaMain
    extends ClassArgMain
{
    /**
     *  The options and arguments.
     */
    protected JdoMetaOptions options;

    /**
     *  The metadata.
     */
    protected EnhancerMetaData jdoMeta;

    /**
     * Creates an instance.
     */
    public JdoMetaMain(PrintWriter out,
                       PrintWriter err)
    {
        this(out, err, new JdoMetaOptions(out, err));
    }

    /**
     * Creates an instance.
     */
    public JdoMetaMain(PrintWriter out,
                       PrintWriter err,
                       JdoMetaOptions options)
    {
        super(out, err, options);
        this.options = options;
    }

    // ----------------------------------------------------------------------

    /**
     * Initializes the jdo metadata component.
     */
    protected void initJdoMetaData()
        throws EnhancerMetaDataFatalError
    {
        final boolean verbose = options.verbose.value;
        final String path = options.jdoPath.value;
        final String jdoPropsFile = options.jdoPropertiesFile.value;

        if (jdoPropsFile != null && jdoPropsFile.length() > 0) {
            // read JDO metadata from properties file
            if (path != null && path.length() > 0) {
                // load the properties file using the path specified with
                // -j (if available)
                try {
                    final Properties props = new Properties();
                    props.load(classes.getInputStreamForResource(jdoPropsFile));
                    jdoMeta = new EnhancerMetaDataPropertyImpl(out, 
                                                               verbose, 
                                                               props);
                } catch (IOException ex) {
                    throw new EnhancerMetaDataFatalError(ex);
                }  
            } else {
                // no -j option => take the properties file name as it is
                jdoMeta = new EnhancerMetaDataPropertyImpl(out, 
                                                           verbose, 
                                                           jdoPropsFile);
            }
        } else {
            //^olsen: simplify interface; just append archives to jdo-path
            jdoMeta = new EnhancerMetaDataJDOModelImpl(
                out, verbose,
                null,
                options.archiveFileNames,
                path);
        }

//^olsen: add archives to path locator...
/*
            // create resource locator for specified zip files
            if (archiveFileNames != null && !archiveFileNames.isEmpty()) {
                final StringBuffer s = new StringBuffer();
                final Iterator i = archiveFileNames.iterator();
                s.append(i.next());
                while (i.hasNext()) {
                    s.append(File.pathSeparator + i.next());
                }
                final ResourceLocator zips
                    = new PathResourceLocator(out, verbose, s.toString());
                printMessage(getI18N("enhancer.using_zip_files",
                                     s.toString()));
                locators.add(zips);
            }
*/

        // wrap with timing meta data object
        if (options.doTiming.value) {
            jdoMeta = new EnhancerMetaDataTimer(jdoMeta);
        }
    }

    /**
     * Initializes all components.
     */
    protected void init()
        throws EnhancerFatalError, EnhancerUserException
    {
        super.init();
        try {
            initJdoMetaData();
        } catch (Exception ex) {
            throw new EnhancerFatalError(ex);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Runs this class
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> JdoMetaMain.main()");
        final JdoMetaMain main = new JdoMetaMain(out, out);
        int res = main.run(args);
        out.println("<-- JdoMetaMain.main(): exit = " + res);
        System.exit(res);
    }
}
