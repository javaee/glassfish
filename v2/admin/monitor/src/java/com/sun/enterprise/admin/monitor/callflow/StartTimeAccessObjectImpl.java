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
 * StartTimeAccessObjectImpl.java
 *
 * Created on August 3, 2005, 11:48 AM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import com.sun.enterprise.admin.monitor.callflow.TableInfo;
import com.sun.enterprise.admin.monitor.callflow.AbstractTableAccessObject;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
/**
 * Access Object to access START_TIME_TBL.
 * Table is created to store the timing information whenever a
 * container transitions from another container or the application code
 * Table is used to calculate the Pie Chart information for time
 * spent in individual containers as well as time spent in an end
 * user application code.
 * @author Harpreet Singh
 */
public class StartTimeAccessObjectImpl extends AbstractTableAccessObject{
    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static StartTimeAccessObjectImpl _singletonST =
    		new StartTimeAccessObjectImpl(); ;    
    
    /** Creates a new instance of StartTimeAccessObjectImpl */
    private StartTimeAccessObjectImpl() {
        String serverName = super.getServerInstanceName();
        super.tableName = TableInfo.START_TIME_TABLE_NAME +
                serverName.toUpperCase();
        super.setName ("StartTime");        
    }

    public static synchronized TableAccessObject getInstance() {
        return _singletonST;
    }
    public boolean insert(java.sql.PreparedStatement pstmt, TransferObject[] transferObject) {
       // sanity
        if (pstmt == null)
            return false;
        boolean result = false;
        try{
            
            for (int i = 0 ; i<transferObject.length; i++) {
                StartTimeTO startTimeTO = (StartTimeTO)transferObject[i];
                pstmt.setString(1, startTimeTO.getRequestId());
                pstmt.setLong(2, startTimeTO.getTimeStamp());
                pstmt.setString(
                        3, startTimeTO.getContainerTypeOrApplicationType());
                pstmt.addBatch();
            }
            int[] updated = pstmt.executeBatch();
            result =  (updated.length == transferObject.length)? true : false;
            addTotalEntriesProcessed(updated.length);            
            if (super.isTraceOn()){
                logger.log(Level.INFO, "Callflow: StartTimeAccessObjectImpl " +
                        " Attempting to Insert : " + transferObject.length +
                        " Inserted "+updated.length+ " rows."+ 
                        " Total Entries written so far: "+
                        getTotalEntriesProcessed());
            }
            
        }  catch(BatchUpdateException bue) {
            // log it
            logger.log(Level.FINE, "Error inserting data into CallFlow tables", bue);
            result = false;
        }catch (SQLException se) {
            // log it
            logger.log(Level.FINE, "Error inserting data into CallFlow tables", se);
            result = false;
        }
        return result;
    }

    public boolean dropTable(java.sql.Connection connection) {
        super.con = connection;
        return super.createStatmentAndExecuteUpdate(
                TableInfo.DROP_TABLE_START_TIME_SQL,
                TableInfo.START_TIME_TABLE_NAME);        
    }

    public boolean createTable(java.sql.Connection connection) {
        super.con = connection;
        return super.createTable(
                TableInfo.CREATE_TABLE_START_TIME_SQL,
                TableInfo.START_TIME_TABLE_NAME);        
    }

    public String getInsertSQL() {
        String newsql = super.updateSqlWithTableName(
                TableInfo.INSERT_INTO_TABLE_START_TIME_SQL, 
                TableInfo.START_TIME_TABLE_NAME);
        return newsql;            
    }

    public String getDeleteSQL () {
        String newsql = super.updateSqlWithTableName (
                TableInfo.DELETE_FROM_TABLE_START_TIME_SQL,
                TableInfo.START_TIME_TABLE_NAME);
        return newsql;
    }
    
}
