/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ha.store.api;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Mahesh Kannan
 */
public class BackingStoreConfiguration<K extends Serializable, V extends Serializable> {

    public static final String BASE_DIRECTORY_NAME = "base.directory.name";

    public static final String NO_OP_PERSISTENCE_TYPE = "noop";

    public static final String START_GMS = "start.gms";

    private String clusterName;

    private String instanceName;

    private String storeName;

    private String shortUniqueName;

    private String storeType;

    private long maxIdleTimeInSeconds = -1;

    private String relaxVersionCheck;

    private long maxLoadWaitTimeInSeconds;

    private File baseDirectory;

    private Class<K> keyClazz;

    private Class<V> valueClazz;

    private boolean synchronousSave;

    private long typicalPayloadSizeInKiloBytes;

    private Logger logger;

    private Map<String, Object> vendorSpecificSettings = new HashMap<String, Object>();

    private ClassLoader classLoader;

    private boolean startGroupService;

    public String getClusterName() {
        return clusterName;
    }

    public BackingStoreConfiguration<K, V> setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public BackingStoreConfiguration<K, V> setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getStoreName() {
        return storeName;
    }

    public BackingStoreConfiguration<K, V> setStoreName(String storeName) {
        this.storeName = storeName;
        return this;
    }

    public String getShortUniqueName() {
        return shortUniqueName;
    }

    public BackingStoreConfiguration<K, V> setShortUniqueName(String shortUniqueName) {
        this.shortUniqueName = shortUniqueName;
        return this;
    }

    public String getStoreType() {
        return storeType;
    }

    public BackingStoreConfiguration<K, V> setStoreType(String storeType) {
        this.storeType = storeType;
        return this;
    }

    public long getMaxIdleTimeInSeconds() {
        return maxIdleTimeInSeconds;
    }

    public BackingStoreConfiguration<K, V> setMaxIdleTimeInSeconds(long maxIdleTimeInSeconds) {
        this.maxIdleTimeInSeconds = maxIdleTimeInSeconds;
        return this;
    }

    public String getRelaxVersionCheck() {
        return relaxVersionCheck;
    }

    public BackingStoreConfiguration<K, V> setRelaxVersionCheck(String relaxVersionCheck) {
        this.relaxVersionCheck = relaxVersionCheck;
        return this;
    }

    public long getMaxLoadWaitTimeInSeconds() {
        return maxLoadWaitTimeInSeconds;
    }

    public BackingStoreConfiguration<K, V> setMaxLoadWaitTimeInSeconds(long maxLoadWaitTimeInSeconds) {
        this.maxLoadWaitTimeInSeconds = maxLoadWaitTimeInSeconds;
        return this;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public BackingStoreConfiguration<K, V> setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
        return this;
    }

    public Class<K> getKeyClazz() {
        return keyClazz;
    }

    public BackingStoreConfiguration<K, V> setKeyClazz(Class<K> kClazz) {
        this.keyClazz = kClazz;
        return this;
    }

    public Class<V> getValueClazz() {
        return valueClazz;
    }

    public BackingStoreConfiguration<K, V> setValueClazz(Class<V> vClazz) {
        this.valueClazz = vClazz;
        return this;
    }

    public boolean isSynchronousSave() {
        return synchronousSave;
    }

    public BackingStoreConfiguration<K, V> setSynchronousSave(boolean synchronousSave) {
        this.synchronousSave = synchronousSave;
        return this;
    }

    public long getTypicalPayloadSizeInKiloBytes() {
        return typicalPayloadSizeInKiloBytes;
    }

    public BackingStoreConfiguration<K, V> setTypicalPayloadSizeInKiloBytes(long typicalPayloadSizeInKiloBytes) {
        this.typicalPayloadSizeInKiloBytes = typicalPayloadSizeInKiloBytes;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public BackingStoreConfiguration<K, V> setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public Map<String, Object> getVendorSpecificSettings() {
        return vendorSpecificSettings;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public BackingStoreConfiguration<K, V> setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public boolean getStartGroupService() {
        return startGroupService;
    }

    public BackingStoreConfiguration<K, V> setStartGroupService(boolean startGroupService) {
        this.startGroupService = startGroupService;
        return this;
    }

    @Override
    public String toString() {
        return "BackingStoreConfiguration{" +
                "clusterName='" + clusterName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", storeName='" + storeName + '\'' +
                ", shortUniqueName='" + shortUniqueName + '\'' +
                ", storeType='" + storeType + '\'' +
                ", maxIdleTimeInSeconds=" + maxIdleTimeInSeconds +
                ", relaxVersionCheck='" + relaxVersionCheck + '\'' +
                ", maxLoadWaitTimeInSeconds=" + maxLoadWaitTimeInSeconds +
                ", baseDirectoryName='" + baseDirectory + '\'' +
                ", keyClazz=" + keyClazz +
                ", valueClazz=" + valueClazz +
                ", synchronousSave=" + synchronousSave +
                ", typicalPayloadSizeInKiloBytes=" + typicalPayloadSizeInKiloBytes +
                ", vendorSpecificSettings=" + vendorSpecificSettings +
                '}';
    }
}
