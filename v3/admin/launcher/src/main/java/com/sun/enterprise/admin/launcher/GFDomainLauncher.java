/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.sun.enterprise.glassfish.bootstrap.GlassFish;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.*;

/**
 * GFDomainLauncher
 * This class is a package-private subclass of GFLauncher designed for
 * domain launching
 * @author bnevins
 */
class GFDomainLauncher extends GFLauncher {

    GFDomainLauncher(GFLauncherInfo info) {
        super(info);
    }

    void internalLaunch() throws GFLauncherException {
        try {
            if (getInfo().isEmbedded()) {
                launchEmbedded();
            }
            else {
                launchInstance();
            }
        }
        catch (GFLauncherException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new GFLauncherException(ex);
        }
    }

    private void launchEmbedded() throws GFLauncherException, BootException {
        GlassFish main = new GlassFish();
        main.start(getInfo().getArgsAsStringArray());
        GFLauncherLogger.info("finishedEmbedded", getInfo().getDomainName());
    }

    List<File> getMainClasspath() throws GFLauncherException {
        List<File> list = new ArrayList<File>();
        File f = new File(getEnvProps().get(INSTALL_ROOT_PROPERTY));
        f = new File(f, BOOTSTRAP_JAR_RELATIVE_PATH);

        if (!f.exists() && !isFakeLaunch())
            throw new GFLauncherException("nobootjar", f.getPath());

        list.add(SmartFile.sanitize(f));
        return list;
    }

    String getMainClass() throws GFLauncherException {
        return MAIN_CLASS;
    }
    
    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.GlassFish";
    private static final String BOOTSTRAP_JAR_RELATIVE_PATH = "modules/glassfish-10.0-SNAPSHOT.jar";
}

/* sample profiler config
 * 
       <java-config classpath-suffix="" debug-enabled="false" debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009" env-classpath-ignored="true" java-home="${com.sun.aas.javaRoot}" javac-options="-g" rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g" system-classpath="">
        <profiler classpath="c:/dev/elf/dist/elf.jar" enabled="false" name="MyProfiler" native-library-path="c:/bin">
          <jvm-options>-Dprofiler3=foo3</jvm-options>
          <jvm-options>-Dprofiler2=foo2</jvm-options>
          <jvm-options>-Dprofiler1=foof</jvm-options>
        </profiler>
 */
