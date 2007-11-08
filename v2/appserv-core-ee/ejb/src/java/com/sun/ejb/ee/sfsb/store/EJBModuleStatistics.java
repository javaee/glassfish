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

package com.sun.ejb.ee.sfsb.store;

import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import java.util.logging.Level;
import com.sun.ejb.spi.monitorable.sfsb.MonitorableSFSBStore;

public class EJBModuleStatistics {

    private long saveLow;
    private long saveHigh;
    private long saveAverage;
    private long totalSaves;
    private long totalSaveTime;

    private long checkpointSaveLow;
    private long checkpointSaveHigh;
    private long checkpointSaveAverage;
    private long totalCheckpointSaveTime;
    private long totalCheckpointSaves;

    private long passivateSaveLow;
    private long passivateSaveHigh;
    private long passivateSaveAverage;
    private long totalPassivateSaveTime;
    private long totalPassivateSaves;

    private long beanSizeAverage;
    private long beanSizeHigh;
    private long beanSizeLow;
    private long totalBeanSize;
    private long totalStoredBeans;

    private long executeStatementAverage;
    private long executeStatementHigh;
    private long executeStatementLow;
    private long totalExecuteStatementTime;
    private long totalExecuteStatements;

    private long getConnectionAverage;
    private long getConnectionHigh;
    private long getConnectionLow;
    private long totalGetConnectionTime;
    private long totalGetConnections;

    private long statementPrepAverage;
    private long statementPrepHigh;
    private long statementPrepLow;
    private long totalStatementPrepTime;
    private long totalStatementPreps;

    public EJBModuleStatistics() {

    }

    /**
    * process the statistics for save based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processSave(long elapsedTime) {
        totalSaves = totalCheckpointSaves + totalPassivateSaves;
        totalSaveTime = totalCheckpointSaveTime + totalPassivateSaveTime; 
        saveLow = getSaveLow();
        saveHigh = getSaveHigh();
        saveAverage = getSaveAverage();
    }

    /**
    * reset the save statistics
    */
    void resetSaveStats() {
        totalSaves = 0;
        totalSaveTime = 0;
        saveAverage = 0;
        saveLow = 0;
        saveHigh = 0;
    }

    /**
    * get saveLow
    */
    long getSaveLow() {
        return Math.min(checkpointSaveLow, passivateSaveLow);
    }

    /**
    * get saveHigh
    */
    long getSaveHigh() {
        return Math.max(checkpointSaveHigh, passivateSaveHigh);
    }

    /**
    * get saveAverage
    */
    long getSaveAverage() {
        if (totalCheckpointSaves + totalPassivateSaves > 0) 
            return (totalCheckpointSaveTime + totalPassivateSaveTime)/(totalCheckpointSaves + totalPassivateSaves);
        else
            return 0;
    }

  /*** Inserted for checkpointSave ****/

    /**
    * process the statistics for checkpoint save 
    * based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processCheckpointSave(long elapsedTime) {
        totalCheckpointSaves++;
        totalCheckpointSaveTime += elapsedTime;
        if (totalCheckpointSaves > 0) 
            checkpointSaveAverage = totalCheckpointSaveTime / totalCheckpointSaves;

        if ( (checkpointSaveLow == 0L) | (elapsedTime < checkpointSaveLow)) {
            checkpointSaveLow = elapsedTime;
        }
        if (elapsedTime > checkpointSaveHigh) {
            checkpointSaveHigh = elapsedTime;
        }
    }

    /**
    * reset the checkpoint save statistics
    */
    void resetCheckpointSaveStats() {
        totalCheckpointSaves = 0;
        totalCheckpointSaveTime = 0;
        checkpointSaveAverage = 0;
        checkpointSaveLow = 0;
        checkpointSaveHigh = 0;
    }

    /**
    * get checkpointSaveLow
    */
    long getCheckpointSaveLow() {
        return checkpointSaveLow;
    }

    /**
    * get checkpointSaveHigh
    */
    long getCheckpointSaveHigh() {
        return checkpointSaveHigh;
    }

    /**
    * get checkpointSaveAverage
    */
    long getCheckpointSaveAverage() {
        return checkpointSaveAverage;
    }

  /*** done ***/

    /**
    * process the statistics for passivate save
    * based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processPassivateSave(long elapsedTime) {
        totalPassivateSaves++;
        totalPassivateSaveTime += elapsedTime;
        if (totalPassivateSaves > 0)
            passivateSaveAverage = totalPassivateSaveTime / totalPassivateSaves;

        if ( (passivateSaveLow == 0L) | (elapsedTime < passivateSaveLow)) {
            passivateSaveLow = elapsedTime;
        }
        if (elapsedTime > passivateSaveHigh) {
            passivateSaveHigh = elapsedTime;
        }
    }

    /**
    * reset the passivate save statistics
    */
    void resetPassivateSaveStats() {
        totalPassivateSaves = 0;
        totalPassivateSaveTime = 0;
        passivateSaveAverage = 0;
        passivateSaveLow = 0;
        passivateSaveHigh = 0;
    }

    /**
    * get passivateSaveLow
    */
    long getPassivateSaveLow() {
        return passivateSaveLow;
    }

    /**
    * get passivateSaveHigh
    */
    long getPassivateSaveHigh() {
        return passivateSaveHigh;
    }

    /**
    * get passivateSaveAverage
    */
    long getPassivateSaveAverage() {
        return passivateSaveAverage;
    }

    /**
    * process the statistics for bean size
    * based on the given beanSize
    * @param beanSize
    */
    synchronized void processBeanSize(long beanSize) {
        totalStoredBeans++;
        totalBeanSize += beanSize;
        if (totalStoredBeans > 0)
            beanSizeAverage = totalBeanSize / totalStoredBeans;

        if ( (beanSizeLow == 0L) | (beanSize < beanSizeLow)) {
            beanSizeLow = beanSize;
        }
        if (beanSize > beanSizeHigh) {
            beanSizeHigh = beanSize;
        }
    }

    /**
    * reset the bean size statistics
    */
    void resetBeanSizeStats() {
        totalStoredBeans = 0;
        totalBeanSize = 0;
        beanSizeAverage = 0;
        beanSizeLow = 0;
        beanSizeHigh = 0;
    }

    /**
    * get beanSizeLow
    */
    long getBeanSizeLow() {
        return beanSizeLow;
    }

    /**
    * get beanSizeHigh
    */
    long getBeanSizeHigh() {
        return beanSizeHigh;
    }

    /**
    * get beanSizeAverage
    */
    long getBeanSizeAverage() {
        return beanSizeAverage;
    }

    /**
    * process the statistics for execute statements
    * based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processExecuteStatement(long elapsedTime) {
        totalExecuteStatements++;
        totalExecuteStatementTime += elapsedTime;
        if (totalExecuteStatements > 0)
        executeStatementAverage = totalExecuteStatementTime / totalExecuteStatements;

        if ( (executeStatementLow == 0L) | (elapsedTime < executeStatementLow)) {
            executeStatementLow = elapsedTime;
        }
        if (elapsedTime > executeStatementHigh) {
            executeStatementHigh = elapsedTime;
        }
    }

    /**
    * reset the execute statement statistics
    */
    void resetExecuteStatementStats() {
        totalExecuteStatements = 0;
        totalExecuteStatementTime = 0;
        executeStatementLow = 0;
        executeStatementHigh = 0;
        executeStatementAverage = 0;
    }

    /**
    * get executeStatementLow
    */
    long getExecuteStatementLow() {
        return executeStatementLow;
    }

    /**
    * get executeStatementHigh
    */
    long getExecuteStatementHigh() {
        return executeStatementHigh;
    }

    /**
    * get executeStatementAverage
    */
    long getExecuteStatementAverage() {
        return executeStatementAverage;
    }

    /**
    * process the statistics for getting connection from pool
    * based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processGetConnectionFromPool(long elapsedTime) {
        totalGetConnections++;
        totalGetConnectionTime += elapsedTime;
        if (totalGetConnections > 0)
            getConnectionAverage = totalGetConnectionTime / totalGetConnections;

        if ( (getConnectionLow == 0L) | (elapsedTime < getConnectionLow)) {
            getConnectionLow = elapsedTime;
        }
        if (elapsedTime > getConnectionHigh) {
            getConnectionHigh = elapsedTime;
        }
    }

    /**
    * get getConnectionLow
    */
    long getGetConnectionLow() {
        return getConnectionLow;
    }

    /**
    * get getConnectionHigh
    */
    long getGetConnectionHigh() {
        return getConnectionHigh;
    }

    /**
    * get getConnectionAverage
    */
    long getGetConnectionAverage() {
        return getConnectionAverage;
    }

    /**
    * reset the get connection statistics
    */
    void resetGetConnectionStats() {
        totalGetConnections = 0;
        totalGetConnectionTime = 0;
        getConnectionLow = 0;
        getConnectionHigh = 0;
        getConnectionAverage = 0;
    }

    /**
    * process the statistics for each 'block' of statement preps
    * based on the given elapsedTime
    * @param elapsedTime
    */
    synchronized void processStatementPrepBlock(long elapsedTime) {
        totalStatementPreps++;
        totalStatementPrepTime += elapsedTime;
        if (totalStatementPreps > 0)
            statementPrepAverage = totalStatementPrepTime / totalStatementPreps;

        if ( (statementPrepLow == 0L) | (elapsedTime < statementPrepLow)) {
            statementPrepLow = elapsedTime;
        }
        if (elapsedTime > statementPrepHigh) {
            statementPrepHigh = elapsedTime;
        }
    }

    /**
    * reset the statement prep statistics
    */
    void resetStatementPrepStats() {
        totalStatementPreps = 0;
        totalStatementPrepTime = 0;
        statementPrepLow = 0;
        statementPrepHigh = 0;
        statementPrepAverage = 0;
    }
    
    /**
    * reset all the statistics
    */
    synchronized void resetStats() {
        this.resetSaveStats();
        this.resetCheckpointSaveStats();
        this.resetPassivateSaveStats();
        this.resetBeanSizeStats();
        this.resetExecuteStatementStats();
        this.resetGetConnectionStats();
        this.resetStatementPrepStats();
        //add more here
    }

    /**
    * get statementPrepLow
    */
    long getStatementPrepLow() {
        return statementPrepLow;
    }

    /**
    * get statementPrepHigh
    */
    long getStatementPrepHigh() {
        return statementPrepHigh;
    }

    /**
    * get statementPrepAverage
    */
    long getStatementPrepAverage() {
        return statementPrepAverage;
    }

    /**
    * append the debug monitor statistics to the buffer
    */     
    public void appendStats(StringBuffer sb) {
    }
    
}
