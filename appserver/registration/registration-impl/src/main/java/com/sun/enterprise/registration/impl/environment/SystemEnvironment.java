/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.registration.impl.environment;

// The Service Tags team maintains the latest version of the implementation
// for system environment data collection.  JDK will include a copy of
// the most recent released version for a JDK release.	We rename
// the package to com.sun.servicetag so that the Sun Connection
// product always uses the latest version from the com.sun.scn.servicetags
// package. JDK and users of the com.sun.servicetag API
// (e.g. NetBeans and SunStudio) will use the version in JDK.

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
/**
 * SystemEnvironment class collects the environment data with the
 * best effort from the underlying platform.
 */
public class SystemEnvironment {
    private String hostname;
    private String hostId;
    private String osName;
    private String osVersion;
    private String osArchitecture;
    private String systemModel;
    private String systemManufacturer;
    private String cpuManufacturer;
    private String serialNumber;
    private String physmem;
    private String sockets;
    private String cores;
    private String virtcpus;
    private String cpuname;
    private String clockrate;
    private static SystemEnvironment sysEnv = null;

    public static synchronized SystemEnvironment getSystemEnvironment() {
        if (sysEnv == null) {
            String os = System.getProperty("os.name");
            if (os.equals("SunOS")) {
                sysEnv = new SolarisSystemEnvironment();
            } else if (os.equals("Linux")) {
                sysEnv = new LinuxSystemEnvironment();
            } else if (os.startsWith("Windows")) {
                sysEnv = new WindowsSystemEnvironment();
            } else {
                sysEnv = new SystemEnvironment();
            }
        }
        return sysEnv;
    }

    // package-private
    SystemEnvironment() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            this.hostname = "Unknown host";
        }
        this.hostId = "";
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.osArchitecture = System.getProperty("os.arch");
        this.systemModel = "";
        this.systemManufacturer = "";
        this.cpuManufacturer = "";
        this.serialNumber = "";
        this.physmem = "";
        this.sockets = "";
        this.cores = "";
        this.virtcpus = "";
        this.cpuname = "";
        this.clockrate = "";
    }


    /**
     * Sets the hostname.
     * @param hostname The hostname to set.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Sets the OS name.
     * @param osName The osName to set.
     */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * Sets the OS version.
     * @param osVersion The osVersion to set.
     */
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * Sets the OS architecture.
     * @param osArchitecture The osArchitecture to set.
     */
    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    /**
     * Sets the system model.
     * @param systemModel The systemModel to set.
     */
    public void setSystemModel(String systemModel) {
        this.systemModel = systemModel;
    }

    /**
     * Sets the system manufacturer.
     * @param systemManufacturer The systemManufacturer to set.
     */
    public void setSystemManufacturer(String systemManufacturer) {
        this.systemManufacturer = systemManufacturer;
    }

    /**
     * Sets the cpu manufacturer.
     * @param cpuManufacturer The cpuManufacturer to set.
     */
    public void setCpuManufacturer(String cpuManufacturer) {
        this.cpuManufacturer = cpuManufacturer;
    }

    /**
     * Sets the serial number.
     * @param serialNumber The serialNumber to set.
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Sets the physmem
     * @param physmem The physmem to set.
     */
    public void setPhysMem(String physmem) {
        this.physmem = physmem;
    }

    /**
     * Sets the sockets
     * @param sockets The sockets to set.
     */
    public void setSockets(String sockets) {
        this.sockets = sockets;
    }

    /**
     * Sets the cores
     * @param cores The cores to set.
     */
    public void setCores(String cores) {
        this.cores = cores;
    }

    /**
     * Sets the virtcpus
     * @param virtcpus The virtcpus to set.
     */
    public void setVirtCpus(String virtcpus) {
        this.virtcpus = virtcpus;
    }

    /**
     * Sets the cpuname
     * @param cpuname The cpuname to set.
     */
    public void setCpuName(String cpuname) {
        this.cpuname = cpuname;
    }

    /**
     * Sets the clockrate
     * @param clockrate The clockrate to set.
     */
    public void setClockRate(String clockrate) {
        this.clockrate = clockrate;
    }

    /**
     * Sets the hostid.  Truncates to a max length of 16 chars.
     * @param hostId The hostid to set.
     */
    public void setHostId(String hostId) {
        if (hostId == null || hostId.equals("null")) {
            hostId = "";
        }
        if (hostId.length() > 16) {
            hostId = hostId.substring(0,16);
        }
        this.hostId = hostId;
    }

    /**
     * Returns the hostname.
     * @return The hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the osName.
     * @return The osName.
     */
    public String getOsName() {
        return osName;
    }

    /**
     * Returns the osVersion.
     * @return The osVersion.
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * Returns the osArchitecture.
     * @return The osArchitecture.
     */
    public String getOsArchitecture() {
        return osArchitecture;
    }

    /**
     * Returns the systemModel.
     * @return The systemModel.
     */
    public String getSystemModel() {
        return systemModel;
    }

    /**
     * Returns the systemManufacturer.
     * @return The systemManufacturer.
     */
    public String getSystemManufacturer() {
        return systemManufacturer;
    }

    /**
     * Returns the serialNumber.
     * @return The serialNumber.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    public String getPhysMem() {
        return physmem;
    }

    public String getSockets() {
        return sockets;
    }

    public String getCores() {
        return cores;
    }

    public String getVirtCpus() {
        return virtcpus;
    }

    public String getCpuName() {
        return cpuname;
    }

    public String getClockRate() {
        return clockrate;
    }

    /**
     * Returns the hostId.
     * @return The hostId.
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * Returns the cpuManufacturer.
     * @return The cpuManufacturer.
     */
    public String getCpuManufacturer() {
        return cpuManufacturer;
    }

    protected String getCommandOutput(String... command) {

        Process p = null;
        ProcessStreamDrainer psd = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            p = pb.start();
            psd = ProcessStreamDrainer.save("RegEnvCommandProcess", p);
            return psd.getOutString();
        } catch (Exception e) {
        // ignore exception
            return "";
        } finally {
            if (p != null) {
            try {
                p.getErrorStream().close();
            } catch (IOException e) {
            // ignore
            }
            try {
                p.getInputStream().close();
            } catch (IOException e) {
                        // ignore
            }
                p = null;
            }
        }
    }


    protected String getFileContent(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            // ignore exception
            return "";
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                } 
            }
        }
    }
}
