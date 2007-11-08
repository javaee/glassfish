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

/*
 * WebModuleStatistics.java
 *
 * Created on January 15, 2003, 4:38 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import javax.management.j2ee.statistics.Statistic;
import com.sun.enterprise.admin.monitor.stats.AverageRangeStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableAverageRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;

/**
 *
 * @author  lwhite
 */
public class WebModuleStatistics {
    
    /** DEFAULT_UPPER_BOUND is maximum value Long can attain */
    public static final long DEFAULT_MAX_BOUND = java.lang.Long.MAX_VALUE;
    
    public static final String SESSION_SIZE = "SessionSize";
    public static final String SESSION_PERSIST_TIME = "SessionPersistTime";
    public static final String CONTAINER_LATENCY = "ContainerLatency";
    
    /** Creates a new instance of WebModuleStatistics */
    public WebModuleStatistics() {
        sessionSizeStat = 
            createDefaultStat(SESSION_SIZE, "byte", "Low/High/Average Session");
        //sessionSizeStat = new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl("session_size", "byte", 0L, 0L, 0L));
        valveSaveStat =    
            createDefaultStat(SESSION_PERSIST_TIME, "millisecond", "Low/High/Average Session Persist Time");
        pipelineStat =
            createDefaultStat(CONTAINER_LATENCY, "millisecond", "Low/High/Average Container Latency");
    }
    
    /** Constructs an instance of MutableAverageRangeStatisticImpl
     * 
     * @param name the name of the stat
     * @param unit the unit of the stat
     * @param description the description of the stat
     * Note: the lowMark is deliberately set extremely high so that
     * incoming lower values will take effect
     */    
    public static MutableAverageRangeStatisticImpl createDefaultStat(String name, String unit, String description) {
        return new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl(
             0L, 0L, DEFAULT_MAX_BOUND,
             0L, 0L, name,
             unit, description, System.currentTimeMillis(),
             System.currentTimeMillis()));        
    }    
    
    /**
     * reset all the statistics
     */       
    synchronized void resetStats() {
        this.resetValveSaveStats();
        this.resetBackgroundSaveStats();
        this.resetPipelineStats();
        this.resetSessionSizeStats();
        this.resetCacheStats();
        this.resetExecuteStatementStats();
        this.resetCommitStats();
        this.resetGetConnectionStats();
        this.resetPutConnectionStats();
        this.resetStatementPrepStats();
        //add more here
        this.resetAverageStats();
    }
    
    void resetAverageStats() {    
        sessionSizeStat.reset();
        pipelineStat.reset();
        valveSaveStat.reset();
    }

    /**
     * process the statistics for valve save
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processValveSave(long elapsedTime) {
        _totalValveSaves++;
        _totalValveSaveTime += elapsedTime;
        _valveSaveAverage = _totalValveSaveTime / _totalValveSaves;

        if((_valveSaveLow == 0L) | (elapsedTime < _valveSaveLow))
            _valveSaveLow = elapsedTime;
        if(elapsedTime > _valveSaveHigh)
            _valveSaveHigh = elapsedTime;
        
        //FIXME added stuff
        valveSaveStat.setCount(elapsedTime);        
    }
    
    /**
     * reset the valveSave statistics
     */    
    void resetValveSaveStats() {
        _totalValveSaves = 0;
        _totalValveSaveTime = 0;
        _valveSaveAverage = 0;
        _valveSaveLow = 0;
        _valveSaveHigh = 0;
    }
    
    /**
     * get valveSaveLow
     */    
    public long getValveSaveLow() {
        return _valveSaveLow;
    }

    /**
     * get valveSaveHigh
     */      
    public long getValveSaveHigh() {
        return _valveSaveHigh;
    }  

    /**
     * get valveSaveAverage
     */    
    public long getValveSaveAverage() {
        return _valveSaveAverage;
    }
    
    /**
     * process the statistics for background save
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processBackgroundSave(long elapsedTime) {
        _totalBackgroundSaves++;
        _totalBackgroundSaveTime += elapsedTime;
        _backgroundSaveAverage = _totalBackgroundSaveTime / _totalBackgroundSaves;

        if((_backgroundSaveLow == 0L) | (elapsedTime < _backgroundSaveLow))
            _backgroundSaveLow = elapsedTime;
        if(elapsedTime > _backgroundSaveHigh)
            _backgroundSaveHigh = elapsedTime;        
    }    
    
    /**
     * reset the background save statistics
     */    
    void resetBackgroundSaveStats() {
        _totalBackgroundSaves = 0;
        _totalBackgroundSaveTime = 0;
        _backgroundSaveAverage = 0;
        _backgroundSaveLow = 0;
        _backgroundSaveHigh = 0;
    }    
    
    /**
     * get backgroundSaveLow
     */    
    long getBackgroundSaveLow() {
        return _backgroundSaveLow;
    }
    
    /**
     * get backgroundSaveHigh
     */      
    long getBackgroundSaveHigh() {
        return _backgroundSaveHigh;
    }  
    
    /**
     * get backgroundSaveAverage
     */      
    long getBackgroundSaveAverage() {
        return _backgroundSaveAverage;
    }    
    
    /**
     * process the statistics for total pipeline time
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    public synchronized void processPipeline(long elapsedTime) {
        _totalPipelines++;
        _totalPipelineTime += elapsedTime;
        _pipelineAverage = _totalPipelineTime / _totalPipelines;

        if((_pipelineLow == 0L) | (elapsedTime < _pipelineLow))
            _pipelineLow = elapsedTime;
        if(elapsedTime > _pipelineHigh)
            _pipelineHigh = elapsedTime;
        
        //FIXME added stuff
        pipelineStat.setCount(elapsedTime);        
    }    
    
    /**
     * reset the pipeline statistics
     */    
    void resetPipelineStats() {
        _totalPipelines = 0;
        _totalPipelineTime = 0;
        _pipelineAverage = 0;
        _pipelineLow = 0;
        _pipelineHigh = 0;
    }    
    
    /**
     * get pipelineLow
     */      
    public long getPipelineLow() {
        return _pipelineLow;
    }

    /**
     * get pipelineHigh
     */     
    public long getPipelineHigh() {
        return _pipelineHigh;
    }  
    
    /**
     * get pipelineAverage
     */     
    public long getPipelineAverage() {
        return _pipelineAverage;
    }
    
    /**
     * process the statistics for session size
     * based on the given sessionSize
     * @param sessionSize
     */     
    synchronized void processSessionSize(long sessionSize) {
        _totalStoredSessions++;
        _totalSessionSize += sessionSize;
        _sessionSizeAverage = _totalSessionSize / _totalStoredSessions;

        if((_sessionSizeLow == 0L) | (sessionSize < _sessionSizeLow))
            _sessionSizeLow = sessionSize;
        if(sessionSize > _sessionSizeHigh)
            _sessionSizeHigh = sessionSize;
        
        //FIXME added stuff
        sessionSizeStat.setCount(sessionSize);
    }    
    
    /**
     * reset the session size statistics
     */    
    void resetSessionSizeStats() {
        _totalStoredSessions = 0;
        _totalSessionSize = 0;
        _sessionSizeAverage = 0;
        _sessionSizeLow = 0;
        _sessionSizeHigh = 0;
    }     
    
    /**
     * get sessionSizeLow
     */     
    public long getSessionSizeLow() {
        return _sessionSizeLow;
    }
    
    /**
     * get sessionSizeHigh
     */       
    public long getSessionSizeHigh() {
        return _sessionSizeHigh;
    }  
    
    /**
     * get sessionSizeAverage
     */       
    public long getSessionSizeAverage() {
        return _sessionSizeAverage;
    }
    
    /**
     * process the statistics for cache hit ratio
     * based on the given cacheWasHit boolean
     * @param cacheWasHit
     */      
    public synchronized void processCacheHit(boolean cacheWasHit) {
        if(cacheWasHit)
            _cacheHits++;
    }      
    
    /**
     * reset the cache hit statistics
     */    
    void resetCacheStats() {
        _cacheHits = 0;
        _cacheMisses = 0;
    }     
    
    /**
     * get cacheHits
     */       
    long getCacheHits() {
        return _cacheHits;
    } 
    
    /**
     * get cacheMisses
     */      
    long getCacheMisses() {
        return _cacheMisses;
    }
    
    /**
     * process the statistics for execute statements
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processExecuteStatement(long elapsedTime) {
        _totalExecuteStatements++;
        _totalExecuteStatementTime += elapsedTime;
        _executeStatementAverage = _totalExecuteStatementTime / _totalExecuteStatements;

        if((_executeStatementLow == 0L) | (elapsedTime < _executeStatementLow))
            _executeStatementLow = elapsedTime;
        if(elapsedTime > _executeStatementHigh)
            _executeStatementHigh = elapsedTime;        
    }    
    
    /**
     * reset the execute statement statistics
     */    
    void resetExecuteStatementStats() {
        _totalExecuteStatements = 0;
        _totalExecuteStatementTime = 0;        
        _executeStatementLow = 0;
        _executeStatementHigh = 0;
        _executeStatementAverage = 0;
    }     
    
    /**
     * get executeStatementLow
     */       
    long getExecuteStatementLow() {
        return _executeStatementLow;
    } 
    
    /**
     * get executeStatementHigh
     */      
    long getExecuteStatementHigh() {
        return _executeStatementHigh;
    } 
    
    /**
     * get executeStatementAverage
     */      
    long getExecuteStatementAverage() {
        return _executeStatementAverage;
    }
    
    /**
     * process the statistics for commit
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processCommit(long elapsedTime) {
        _totalCommits++;
        _totalCommitTime += elapsedTime;
        _commitAverage = _totalCommitTime / _totalCommits;

        if((_commitLow == 0L) | (elapsedTime < _commitLow))
            _commitLow = elapsedTime;
        if(elapsedTime > _commitHigh)
            _commitHigh = elapsedTime;        
    }    
    
    /**
     * reset the commit statistics
     */    
    void resetCommitStats() {
        _totalCommits = 0;
        _totalCommitTime = 0;        
        _commitLow = 0;
        _commitHigh = 0;
        _commitAverage = 0;
    }    
    
    /**
     * get commitLow
     */       
    long getCommitLow() {
        return _commitLow;
    } 
    
    /**
     * get commitHigh
     */      
    long getCommitHigh() {
        return _commitHigh;
    } 
    
    /**
     * get commitAverage
     */      
    long getCommitAverage() {
        return _commitAverage;
    }     
    
    /**
     * process the statistics for getting connection from pool
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processGetConnectionFromPool(long elapsedTime) {
        _totalGetConnections++;
        _totalGetConnectionTime += elapsedTime;
        _getConnectionAverage = _totalGetConnectionTime / _totalGetConnections;

        if((_getConnectionLow == 0L) | (elapsedTime < _getConnectionLow))
            _getConnectionLow = elapsedTime;
        if(elapsedTime > _getConnectionHigh)
            _getConnectionHigh = elapsedTime;        
    }    
    
    /**
     * reset the get connection statistics
     */    
    void resetGetConnectionStats() {
        _totalGetConnections = 0;
        _totalGetConnectionTime = 0;        
        _getConnectionLow = 0;
        _getConnectionHigh = 0;
        _getConnectionAverage = 0;
    }     
    
    /**
     * get getConnectionLow
     */       
    long getGetConnectionLow() {
        return _getConnectionLow;
    } 
    
    /**
     * get getConnectionHigh
     */      
    long getGetConnectionHigh() {
        return _getConnectionHigh;
    } 
    
    /**
     * get getConnectionAverage
     */      
    long getGetConnectionAverage() {
        return _getConnectionAverage;
    }
    
    /**
     * process the statistics for putting connection into pool
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processPutConnectionIntoPool(long elapsedTime) {
        _totalPutConnections++;
        _totalPutConnectionTime += elapsedTime;
        _putConnectionAverage = _totalPutConnectionTime / _totalPutConnections;

        if((_putConnectionLow == 0L) | (elapsedTime < _putConnectionLow))
            _putConnectionLow = elapsedTime;
        if(elapsedTime > _putConnectionHigh)
            _putConnectionHigh = elapsedTime;        
    } 
    
    /**
     * reset the put connection statistics
     */    
    void resetPutConnectionStats() {
        _totalPutConnections = 0;
        _totalPutConnectionTime = 0;        
        _putConnectionLow = 0;
        _putConnectionHigh = 0;
        _putConnectionAverage = 0;
    }
    
    /**
     * get putConnectionLow
     */       
    long getPutConnectionLow() {
        return _putConnectionLow;
    } 
    
    /**
     * get putConnectionHigh
     */      
    long getPutConnectionHigh() {
        return _putConnectionHigh;
    } 
    
    /**
     * get putConnectionAverage
     */      
    long getPutConnectionAverage() {
        return _putConnectionAverage;
    }    
    
    /**
     * process the statistics for each 'block' of statement preps
     * based on the given elapsedTime
     * @param elapsedTime
     */     
    synchronized void processStatementPrepBlock(long elapsedTime) {
        _totalStatementPreps++;
        _totalStatementPrepTime += elapsedTime;
        _statementPrepAverage = _totalStatementPrepTime / _totalStatementPreps;

        if((_statementPrepLow == 0L) | (elapsedTime < _statementPrepLow))
            _statementPrepLow = elapsedTime;
        if(elapsedTime > _statementPrepHigh)
            _statementPrepHigh = elapsedTime;        
    }    
    
    /**
     * reset the statement prep statistics
     */    
    void resetStatementPrepStats() {
        _totalStatementPreps = 0;
        _totalStatementPrepTime = 0;        
        _statementPrepLow = 0;
        _statementPrepHigh = 0;
        _statementPrepAverage = 0;
    }     
    
    /**
     * get statementPrepLow
     */       
    long getStatementPrepLow() {
        return _statementPrepLow;
    } 
    
    /**
     * get statementPrepHigh
     */      
    long getStatementPrepHigh() {
        return _statementPrepHigh;
    } 
    
    /**
     * get statementPrepAverage
     */      
    long getStatementPrepAverage() {
        return _statementPrepAverage;
    }    
        
    long _valveSaveLow = 0L;    
    long _valveSaveHigh = 0L;
    long _valveSaveAverage = 0L;
    long _totalValveSaves = 0L;
    long _totalValveSaveTime = 0L;
    
    long _backgroundSaveLow = 0L;    
    long _backgroundSaveHigh = 0L;
    long _backgroundSaveAverage = 0L;
    long _totalBackgroundSaves = 0L;
    long _totalBackgroundSaveTime = 0L;    
    
    long _pipelineLow = 0L;    
    long _pipelineHigh = 0L;
    long _pipelineAverage = 0L;
    long _totalPipelines = 0L;
    long _totalPipelineTime = 0L; 
    
    long _sessionSizeLow = 0L;
    long _sessionSizeHigh = 0L;
    long _sessionSizeAverage = 0L;
    long _totalStoredSessions = 0L;
    long _totalSessionSize = 0L;
    
    long _cacheHits = 0L;
    long _cacheMisses = 0L;
    
    long _executeStatementLow = 0L;    
    long _executeStatementHigh = 0L;
    long _executeStatementAverage = 0L; 
    long _totalExecuteStatements = 0L;
    long _totalExecuteStatementTime = 0L; 
    
    long _commitLow = 0L;    
    long _commitHigh = 0L;
    long _commitAverage = 0L; 
    long _totalCommits = 0L;
    long _totalCommitTime = 0L;     
    
    long _getConnectionLow = 0L;    
    long _getConnectionHigh = 0L;
    long _getConnectionAverage = 0L; 
    long _totalGetConnections = 0L;
    long _totalGetConnectionTime = 0L; 
    
    long _putConnectionLow = 0L;    
    long _putConnectionHigh = 0L;
    long _putConnectionAverage = 0L; 
    long _totalPutConnections = 0L;
    long _totalPutConnectionTime = 0L;    
    
    long _statementPrepLow = 0L;    
    long _statementPrepHigh = 0L;
    long _statementPrepAverage = 0L; 
    long _totalStatementPreps = 0L;
    long _totalStatementPrepTime = 0L; 
    
    MutableAverageRangeStatisticImpl sessionSizeStat = null;
    MutableAverageRangeStatisticImpl pipelineStat = null;
    MutableAverageRangeStatisticImpl valveSaveStat = null;    
    
    public AverageRangeStatistic getSessionSizeStat() {
        //return (AverageRangeStatistic) sessionSizeStat.unmodifiableView();
        return sessionSizeStat;
    }
    
    public AverageRangeStatistic getPipelineStat() {
        //return (AverageRangeStatistic) pipelineStat.unmodifiableView();
        return pipelineStat;
    }
    
    public AverageRangeStatistic getValveSaveStat() {
        //return (AverageRangeStatistic) valveSaveStat.unmodifiableView();
        return valveSaveStat;
    }    

}
    