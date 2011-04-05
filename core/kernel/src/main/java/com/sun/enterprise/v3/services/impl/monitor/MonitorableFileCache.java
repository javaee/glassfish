/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.stats.FileCacheStatsProvider;
import com.sun.grizzly.http.FileCache;

/**
 * Monitoring aware {@link FileCache} implementation.
 *
 * @author Alexey Stashok
 */
public class MonitorableFileCache extends FileCache {
    // The GrizzlyMonitoring objects, which encapsulates Grizzly probe emitters

    private final GrizzlyMonitoring grizzlyMonitoring;
    private final String monitoringId;

    public MonitorableFileCache(GrizzlyMonitoring grizzlyMonitoring, String monitoringId) {
        this.grizzlyMonitoring = grizzlyMonitoring;
        this.monitoringId = monitoringId;
        if (grizzlyMonitoring != null) {
            final FileCacheStatsProvider statsProvider =
                    grizzlyMonitoring.getFileCacheStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(this);
            }
        }
    }

    @Override
    protected void recalcCacheStatsIfMonitoring(FileCacheEntry entry) {
        recalcCacheStats(entry);
    }
    
    @Override
    protected void countHit() {
        super.countHit();
        grizzlyMonitoring.getFileCacheProbeProvider().countHitEvent(monitoringId);
    }

    @Override
    protected void countMiss() {
        super.countMiss();
        grizzlyMonitoring.getFileCacheProbeProvider().countMissEvent(monitoringId);
    }

    @Override
    protected void countInfoHit() {
        super.countInfoHit();
        grizzlyMonitoring.getFileCacheProbeProvider().countInfoHitEvent(monitoringId);
    }

    @Override
    protected void countInfoMiss() {
        super.countInfoMiss();
        grizzlyMonitoring.getFileCacheProbeProvider().countInfoMissEvent(monitoringId);
    }

    @Override
    protected void countContentHit() {
        super.countContentHit();
        grizzlyMonitoring.getFileCacheProbeProvider().countContentHitEvent(monitoringId);
    }

    @Override
    protected void countContentMiss() {
        super.countContentMiss();
        grizzlyMonitoring.getFileCacheProbeProvider().countContentMissEvent(monitoringId);
    }

    @Override
    protected void incOpenCacheEntries() {
        super.incOpenCacheEntries();
        grizzlyMonitoring.getFileCacheProbeProvider().incOpenCacheEntriesEvent(
                monitoringId);
    }

    @Override
    protected void decOpenCacheEntries() {
        super.decOpenCacheEntries();
        grizzlyMonitoring.getFileCacheProbeProvider().decOpenCacheEntriesEvent(
                monitoringId);
    }

    @Override
    protected void addHeapSize(long size) {
        super.addHeapSize(size);
        grizzlyMonitoring.getFileCacheProbeProvider().addHeapSizeEvent(
                monitoringId, size);
    }

    @Override
    protected void subHeapSize(long size) {
        super.subHeapSize(size);
        grizzlyMonitoring.getFileCacheProbeProvider().subHeapSizeEvent(
                monitoringId, size);
    }

    @Override
    protected void addMappedMemorySize(long size) {
        super.addMappedMemorySize(size);
        grizzlyMonitoring.getFileCacheProbeProvider().addMappedMemorySizeEvent(
                monitoringId, size);
    }

    @Override
    protected void subMappedMemorySize(long size) {
        super.subMappedMemorySize(size);
        grizzlyMonitoring.getFileCacheProbeProvider().subMappedMemorySizeEvent(
                monitoringId, size);
    }
}
