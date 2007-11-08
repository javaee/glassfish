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
 * DbAccessObjectImpl.java
 *
 * Created on July 11, 2005, 10:40 AM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import com.sun.appserv.management.monitor.CallFlowMonitor;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.Set;
/**
 *
 * @author Harpreet Singh
 */
public class DbAccessObjectImpl implements DbAccessObject {
    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static DbAccessObject _singleton = new DbAccessObjectImpl();
    
    private static final String CALLFLOW_POOL_JNDI_NAME = "jdbc/__CallFlowPool__pm";

    private TableAccessObject reqStart = null;
    private TableAccessObject reqEnd = null;
    private TableAccessObject methStart = null;
    private TableAccessObject methEnd = null;
    private TableAccessObject startTime = null;
    private TableAccessObject endTime = null;
    
    private Connection connection = null;
    private PreparedStatement pstmtRS = null;
    private PreparedStatement pstmtRE = null;
    private PreparedStatement pstmtMS = null;
    private PreparedStatement pstmtME = null;
    private PreparedStatement pstmtST = null;
    private PreparedStatement pstmtET = null;
    
    private String serverName = null;
    private boolean traceOn = false;
    /** Creates a new instance of DbAccessObjectImpl */
    private DbAccessObjectImpl() {
        reqStart = RequestStartAccessObjectImpl.getInstance();
        reqEnd = RequestEndAccessObjectImpl.getInstance();
        methStart = MethodStartAccessObjectImpl.getInstance();
        methEnd = MethodEndAccessObjectImpl.getInstance();
        startTime = StartTimeAccessObjectImpl.getInstance();
        endTime = EndTimeAccessObjectImpl.getInstance();
        traceOn = TraceOnHelper.isTraceOn();        
    }
    private TableAccessObject[] getAccessObjectsAsArray (){
        TableAccessObject[] tao = new TableAccessObject[6];
        tao[0] = reqStart;
        tao[1] = reqEnd;
        tao[2] = methStart;
        tao[3] = methEnd;
        tao[4] = startTime;
        tao[5] = endTime;
        return tao;
    }
    //<editor-fold defaultstate="collapsed" desc="Enable & Disable">

    public boolean enable () {    
        setupConnection();
        boolean result = enable(connection);
        if(result == true){
            try{
                createPreparedStatements();
            } catch (SQLException sqe){
                // log it
                // if preparedStatements are not created. No point doing anything
                // else.
                logger.log(Level.SEVERE, "callflow.enable_failed", sqe);    
                RuntimeException re = 
                        new RuntimeException ("Cannot create SQL Statements" +
                        " to connect to Callflow DB. Is database up?");
                re.initCause(sqe);
                throw re;
            } finally {
                closeConnection();            
            }
        } else if (result == false) { // as of now, we always return a true
            // even if the table creation fails. This should never be exercised
            // Keeping this code here such that if we change the semantics
            // of enable(connection) that would not break the callflow code.
             logger.log(Level.SEVERE, "callflow.enable_failed");
             closeConnection ();
             throw new RuntimeException ("Error creating tables");
        }
        return result;
    }
    private boolean enable (Connection connection) {
        // even if 1 table create fails, go ahead and create the rest
        
        boolean rs = reqStart.createTable(connection); 
        if (!rs)
            return false;
        boolean re = reqEnd.createTable(connection);
        if (!re)
            return false;
        boolean ms = methStart.createTable(connection);
        if (!ms)
            return false;
        boolean me = methEnd.createTable(connection);
        if (!me)
            return false;
        boolean st = startTime.createTable(connection);
        if (!st)
            return false;
        boolean et = endTime.createTable(connection);
        if (!et)
            return false;
        return true;
    }
    public boolean disable() {
        closePreparedStatements();
        closeConnection();
        return true;
    }

//</editor-fold>
    
    /**
     * Factory Methodd to return DbAccessObject 
     * @return DbAccessObject
     */
    public static synchronized DbAccessObject getInstance() {
        return _singleton;
    }
    
    public boolean clearData () {
        if(!setupConnection()) 
            return false;
        boolean result =  clearData (connection);
        closeConnection ();
        return result;
    }
    private boolean clearData(Connection connection) {
        // even if 1 table delete fails, go ahead and delete the rest
        
        boolean rs = reqStart.dropTable(connection);       
        boolean re = reqEnd.dropTable(connection);
        boolean ms = methStart.dropTable(connection);
        boolean me = methEnd.dropTable(connection);
        boolean st = startTime.dropTable(connection);
        boolean et = endTime.dropTable(connection);
        
        if(rs == false || re == false || ms == false || me == false ||
		st == false || et == false)
            return false;
        return true;
        
    }    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Connection and Statement Manipulation">
    
    // this method is temporarily used to setup connection to the database
    // This should be modified to read the connection information from the
    // domain.xml
    private boolean setupConnection (){
      try{
               // TODO code application logic here
          boolean standaloneDb =
                  Boolean.valueOf(System.getProperty("callflow.db.standalone"));

          if (!standaloneDb) {

              InitialContext ic = new InitialContext ();
              DataSource ds = (DataSource)ic.lookup (CALLFLOW_POOL_JNDI_NAME);
              connection = ds.getConnection();
          } else {
            // TODO code application logic here
            String url="jdbc:derby://localhost:1527/sun-callflow;retrieveMessagesFromServerOnGetMessage=true;create=true;";            
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            connection = DriverManager.getConnection(url, "APP", "APP");         
          }
        } catch (Exception e){
            logger.log(Level.INFO, "callflow.connection_obtain_failed", e);
            logger.log(Level.SEVERE, "callflow.enable_failed");            
            RuntimeException re = 
                    new RuntimeException ("Error obtaining connection to " +
                                    "callflow database. " +
                    "Is database started?. Refer logs for exact cause.", e.getCause());
            throw re;
        }        
        return true;    
    }
    private void closeConnection (){
        try{
            if(connection != null){
                connection.close();
            }
        } catch (Exception e){
            logger.log(Level.WARNING, "Cannot close connection to CallFlow DB", e); 
        } finally {
            connection = null;
        }
    }

    private void createPreparedStatements () throws SQLException {
        if(connection == null)
            if(!setupConnection())
                return;

        pstmtRS = connection.prepareStatement (reqStart.getInsertSQL());
        pstmtRE = connection.prepareStatement(reqEnd.getInsertSQL());
        pstmtMS = connection.prepareStatement(methStart.getInsertSQL());
        pstmtME = connection.prepareStatement(methEnd.getInsertSQL());
        pstmtST = connection.prepareStatement(startTime.getInsertSQL());
        pstmtET = connection.prepareStatement(endTime.getInsertSQL());

    }
    private void closePreparedStatements () {
        try{
            if(pstmtRS != null)
                pstmtRS.close();
        } catch (SQLException s){
            // log it to fine.
            logger.log(Level.FINE, "Could not close RequestStart SQL Statement", s);            
        }finally{
            pstmtRS = null;
        }
        try{
            if(pstmtRE != null)
                pstmtRE.close();
        } catch (SQLException e){
            // log it to fine
            logger.log(Level.FINE, "Could not close RequestEnd SQL Statement", e);            
        }finally{
            pstmtRE = null;
        }
        try{
            if(pstmtMS != null)
                pstmtMS.close();
        } catch (SQLException se){
            // log it to fine
            logger.log(Level.FINE, "Could not close MethodStart SQL Statement", se);                        
        } finally{
            pstmtMS = null;
        }
        try{
            if(pstmtME != null)
                pstmtME.close();
        } catch(SQLException sqe){
            //log it to fine.
            logger.log(Level.FINE, "Could not close MethodEnd SQL Statement", sqe);                        
        }finally{
            pstmtME = null;
        }        
        try{
            if(pstmtST != null)
                pstmtST.close();
        } catch(SQLException sqe){
            //log it to fine.
            logger.log(Level.FINE, "Could not close StartTime SQL Statement", sqe);                        
        }finally{
            pstmtST = null;
        }        
        try{
            if(pstmtET != null)
                pstmtET.close();
        } catch(SQLException sqe){
            //log it to fine.
            logger.log(Level.FINE, "Could not close EndTime SQL Statement", sqe);                                    
        }finally{
            pstmtET = null;
        }        
        
    }
    
//</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="getRequestInformation Query">   
    public java.util.List<java.util.Map<String, String>> getRequestInformation() {
        if(connection == null)
            setupConnection();
        if (traceOn){
            logger.log(Level.INFO, "Callflow: getRequestInfo");
        }
        
        assert connection != null;
        List<Map<String, String>> list  = null;
        Statement stmt = null;
        try{
            stmt = connection.createStatement();
            String query = generateQuerySQL(TableInfo.GET_REQUEST_INFORMATION_SQL);
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            list = new ArrayList<Map<String, String>>();
            while(rs.next()){
                Map<String, String> map = new HashMap<String, String> ();
                
                String request_id = rs.getString(TableInfo.REQUEST_ID);
                map.put (CallFlowMonitor.REQUEST_ID_KEY, request_id);
                // no need to return time stamp in nano.
                long time_stamp = rs.getLong(TableInfo.TIME_STAMP_MILLIS);
                map.put (CallFlowMonitor.TIME_STAMP_MILLIS_KEY, String.valueOf(time_stamp));

                String ip_address = rs.getString(TableInfo.IP_ADDRESS);
                map.put (CallFlowMonitor.CLIENT_HOST_KEY, ip_address);
                
                String request_type = rs.getString(TableInfo.REQUEST_TYPE);
                map.put (CallFlowMonitor.REQUEST_TYPE_KEY, request_type);

                String method_name = rs.getString(TableInfo.METHOD_NAME);
                map.put (CallFlowMonitor.METHOD_NAME_KEY, method_name);
                
                String app_name = rs.getString(TableInfo.APP_NAME);
                map.put (CallFlowMonitor.APPLICATION_NAME_KEY, app_name);
                
                String security_id = rs.getString (TableInfo.SECURITY_ID);                
                map.put (CallFlowMonitor.USER_KEY, security_id);
                
                String exception_name = rs.getString(TableInfo.EXCEPTION_NAME);
                map.put (CallFlowMonitor.EXCEPTION_KEY, exception_name);
                
                long time_taken = rs.getLong(10);
                map.put (CallFlowMonitor.RESPONSE_TIME_KEY, String.valueOf(time_taken));
                if (traceOn){
                    logger.log(Level.INFO, "Callflow: getRequestInfo: ReqId ="+
                            request_id + " , appname = "+ app_name);
                }
                
                list.add(map);
            }
            
        } catch (SQLException se){
            // log it
            logger.log(Level.WARNING, "callflow.error_get_request_info");                                    
            logger.log(Level.FINE, "callflow.error_sql_execute", se);
        }
        closeConnection();
        return list;
    }
 //</editor-fold> 
    
    private String generateQuerySQL (String sql){
        // append RS, RE , MS, ME table Name with __server name        
       String newsql = new String(sql);
       // replace RS Table with RS__server
       String table = TableInfo.REQUEST_START_TABLE_NAME;
       String tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);
       // RE
       table = TableInfo.REQUEST_END_TABLE_NAME;
       tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);
       
       // MS
       table = TableInfo.METHOD_START_TABLE_NAME;
       tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);

       // RE
       table = TableInfo.METHOD_END_TABLE_NAME;
       tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);
       if (traceOn){
           logger.log(Level.INFO, "Callflow: Query = \n[ "+newsql+" ]");
       }
       
       return newsql;               
    }
    
    private String getTableWithServerName (String oldTableName) {
        if (serverName == null)
            serverName = reqStart.getServerInstanceName ();
        
        return oldTableName + serverName;
    }
   
        
    //<editor-fold defaultstate="collapsed" desc="GetCallStack Query">       
    public List<Map<String, String>> getCallStackInformation (String requestId) {
       if(connection == null)
            if(!setupConnection())
                return null;
        assert connection != null;
        List list  = null;
        PreparedStatement stmt = null;
        try{
            String sql = generateQuerySQL (TableInfo.GET_CALLSTACK_INFORMATION_SQL);
            stmt = connection.prepareStatement(sql);
            
            stmt.setString(1, requestId);
            stmt.setString(2, requestId);
            stmt.setString(3, requestId);
            stmt.setString(4, requestId);
            ResultSet rs = stmt.executeQuery();
            list = new ArrayList<Map<String, String>>();
            while(rs.next()){
                Map<String, String> map = new HashMap<String, String> ();

                String table_type = rs.getString(TABLE_TYPE_INDEX_CSI);
                table_type = table_type.trim();
                map.put(CallFlowMonitor.CALL_STACK_ROW_TYPE_KEY, table_type);
                if (CallFlowMonitor.CALL_STACK_REQUEST_START.equals(table_type)){
                    map = getCallStackRequestStartInformation(rs, map);
                } else if (CallFlowMonitor.CALL_STACK_REQUEST_END.equals(table_type)) {
                    map = getCallStackRequestEndInformation(rs, map);
                } else if (CallFlowMonitor.CALL_STACK_METHOD_END.equals(table_type)){
                    map =  getCallStackMethodEndInformation(rs, map);
                } else if (CallFlowMonitor.CALL_STACK_METHOD_START.equals(table_type)){
                    map = getCallStackMethodStartInformation(rs, map);
                }
                list.add(map);
            }          
            stmt.close();
        } catch (SQLException se){
            // log it
            logger.log(Level.FINE, "callflow.error_get_callstack_info", se);   
        }
        closeConnection();
        return list;        
    }
    private Map<String, String> getCallStackCommonInformation (ResultSet rs, 
            Map<String, String> map) throws SQLException
    {
        
        // get columns common across all tables
        String request_id = rs.getString(TableInfo.REQUEST_ID);
        map.put(CallFlowMonitor.REQUEST_ID_KEY, request_id);

        long time_stamp = rs.getLong(TIMESTAMP_INDEX_CSI);
        map.put(CallFlowMonitor.TIME_STAMP_KEY, String.valueOf(time_stamp));

        long time_stamp_millis = rs.getLong (TIMESTAMP_MILLIS_INDEX_CSI);
        map.put (CallFlowMonitor.TIME_STAMP_MILLIS_KEY, String.valueOf(time_stamp_millis));

        return map;
    }
    
    private Map<String, String> getCallStackRequestEndInformation (ResultSet rs, 
            Map<String, String> map) throws SQLException
    {
        map = getCallStackCommonInformation(rs, map);
        return map;
    }
 
    private Map<String, String> getCallStackRequestStartInformation (ResultSet rs, 
            Map<String, String> map) throws SQLException{
        map = getCallStackCommonInformation(rs, map);
        String request_type = rs.getString(REQUEST_TYPE_INDEX_CSI);
        map.put (CallFlowMonitor.REQUEST_TYPE_KEY, request_type);        
        return map;
    }    
    
   private Map<String, String> getCallStackMethodEndInformation (ResultSet rs, 
            Map<String, String> map) throws SQLException{
        map = getCallStackCommonInformation(rs, map);
        String exception_name = rs.getString(EXCEPTION_NAME_INDEX_CSI);
        map.put(CallFlowMonitor.EXCEPTION_KEY, exception_name);
        String status =
                (exception_name == null)? String.valueOf(Boolean.TRUE) :
                    String.valueOf(Boolean.FALSE);        
        map.put(CallFlowMonitor.STATUS_KEY, status);
        return map;
   
   }    
   
    private Map<String, String> getCallStackMethodStartInformation (ResultSet rs, 
            Map<String, String> map) throws SQLException{
        map = getCallStackCommonInformation(rs, map);
        String container_type = rs.getString(CONTAINER_TYPE_INDEX_CSI);
        map.put(CallFlowMonitor.CONTAINER_TYPE_KEY, container_type);
        
        String component_name = rs.getString(COMPONENT_NAME_INDEX_CSI);
        map.put(CallFlowMonitor.COMPONENT_NAME_KEY, component_name);
        
        String app_name = rs.getString(APP_NAME_INDEX_CSI);
        map.put (CallFlowMonitor.APPLICATION_NAME_KEY, app_name);
        
        String method_name = rs.getString(METHOD_NAME_INDEX_CSI);
        map.put (CallFlowMonitor.METHOD_NAME_KEY, method_name);

        String module_name = rs.getString(MODULE_NAME_INDEX_CSI);
        map.put(CallFlowMonitor.MODULE_NAME_KEY, module_name);
        
        String thread_id = rs.getString(THREAD_ID_INDEX_CSI);
        map = getCallStackCommonInformation(rs, map);
        String exception_name = rs.getString(EXCEPTION_NAME_INDEX_CSI);
        map.put(CallFlowMonitor.EXCEPTION_KEY, exception_name);
        String status =
                (exception_name == null)? String.valueOf(Boolean.TRUE) :
                    String.valueOf(Boolean.FALSE);        
        map.put(CallFlowMonitor.STATUS_KEY, status);
        return map;
   
   }    
//</editor-fold>    

    //<editor-fold defaultstate="collapsed"  desc="Insert method"> 
    public boolean insert(TransferObject[] transferObject) {
        boolean result = false;
    
        if (transferObject.length == 0) //sanity
            return true; 
        
        if (transferObject[0] instanceof RequestStartTO){
            result =  
             (pstmtRS == null)? false: reqStart.insert(pstmtRS, transferObject);
        } else if (transferObject[0] instanceof RequestEndTO){
            result = 
              (pstmtRE == null)? false : reqEnd.insert(pstmtRE, transferObject);
        } else  if (transferObject[0] instanceof MethodStartTO){
           result = 
           (pstmtMS == null)? false : methStart.insert(pstmtMS, transferObject);
        } else if (transferObject[0] instanceof MethodEndTO){
            result =
             (pstmtME == null)? false : methEnd.insert(pstmtME, transferObject);
        } else if (transferObject[0] instanceof StartTimeTO){
	    result = 
		(pstmtST == null)? false : startTime.insert(pstmtST, transferObject);
	} else if (transferObject[0] instanceof EndTimeTO){
	    result = 
		(pstmtET == null)? false : endTime.insert(pstmtET, transferObject);
	}
        return result;
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed"  desc="getPieInformation query">
    public Map <String, String> getPieInformation(String requestId) {
        closeConnection();
        connection = null;
        if(connection == null)
            if(!setupConnection())
                return null;
        Map<String, String> mapST  = null;
        Map<String, String> mapET = null;
        Map<String, String> retMap = null;
        PreparedStatement st = null;
        PreparedStatement et = null;
        try{
            String startSql = 
                generateQuerySQLForStartTimeAndEndTime (TableInfo.GET_PIE_INFORMATION_START_TIME_SQL);
            st = connection.prepareStatement(startSql);
            st.setString (1, requestId);
            ResultSet rs = st.executeQuery();
            mapST = new HashMap<String, String>();
            while(rs.next()){                             
                String container_type = rs.getString(1);
                long time_taken = rs.getLong(2);
                mapST.put(container_type, String.valueOf(time_taken));                
            }
            st.close();
            String endSql = 
                generateQuerySQLForStartTimeAndEndTime (TableInfo.GET_PIE_INFORMATION_END_TIME_SQL);
            et = connection.prepareStatement(endSql);
            et.setString (1, requestId);
            rs = et.executeQuery();
            mapET = new HashMap<String, String>();
            while(rs.next()){                             
                String container_type = rs.getString(1);
                long time_taken = rs.getLong(2);
                mapET.put(container_type, String.valueOf(time_taken));                
            }
            et.close();
            retMap = new HashMap <String, String> ();
            
            for (String key : mapST.keySet()){
                String stime = mapST.get(key);
                if (stime == null)
                    continue;
                long startTime = Long.valueOf(stime);
                String etime = mapET.get(key);
                if (etime == null )
                    continue;
                long endTime = Long.valueOf(etime);
                long time_taken = endTime - startTime;
                retMap.put (key, String.valueOf (time_taken));
            }
            
        } catch (SQLException se){
            // log it
            logger.log(Level.FINE, "callflow.error_get_pie_info", se);             
        }
        closeConnection();
        return retMap;
    }
    private String generateQuerySQLForStartTimeAndEndTime (String sql){
        // append ST, ET table Name with __server name        
       String newsql = new String(sql);
       // replace ST Table with ST__server
       String table = TableInfo.START_TIME_TABLE_NAME;
       String tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);
       // ET
       table = TableInfo.END_TIME_TABLE_NAME;
       tableWithServerName = getTableWithServerName (table);
       newsql = newsql.replaceAll(table, tableWithServerName);
       return newsql;               
    }
	
   //</editor-fold> 

    //<editor-fold defaultstate="collapsed"  desc="References into Columns for getCallStackInformation query">
    /**
     * The following variables are used to reference into the columns that 
     * are returned by the getCallStackInformation Query
     */ 
    private int SECURITY_ID_INDEX_CSI = 13;

    private int TRANSACTION_ID_INDEX_CSI = 12;

    private int THREAD_ID_INDEX_CSI = 11;

    private int MODULE_NAME_INDEX_CSI = 10;

    private int METHOD_NAME_INDEX_CSI = 9;

    private int APP_NAME_INDEX_CSI = 8;

    private int COMPONENT_NAME_INDEX_CSI = 7;

    private int CONTAINER_TYPE_INDEX_CSI = 6;

    private int EXCEPTION_NAME_INDEX_CSI = 5;

    private int REQUEST_TYPE_INDEX_CSI = 4;

    private int TIMESTAMP_INDEX_CSI = 3;
    
    private int TABLE_TYPE_INDEX_CSI = 1;
    
    private int TIMESTAMP_MILLIS_INDEX_CSI = 14;
//</editor-fold>    
    
    public boolean deleteRequestIds (String[] requestIds) {
        if (requestIds.length <=0 )
            return true;
        
        boolean resultRS = false;
        boolean resultRE = false;
        boolean resultMS = false;
        boolean resultME = false;
        boolean resultCS = false;
        boolean resultCE = false;
        
        PreparedStatement rs = null;
        PreparedStatement re = null;
        PreparedStatement ms = null;
        PreparedStatement me = null;
        PreparedStatement st = null;
        PreparedStatement et = null;
        
        
        if (connection == null)
            if (!setupConnection())
                return false;
        try {
            rs = connection.prepareStatement(reqStart.getDeleteSQL());
            resultRS = (rs == null)? false : reqStart.delete(rs, requestIds);
            if (!resultRS)
                 logger.log (Level.FINE, "Error deleting requests from Request Start Table");
        } catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from Request Start Table", se);        
        }finally {
            if (rs != null){
                try{
                    rs.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    rs = null;
                }
            }
        }
        try{
            re = connection.prepareStatement(reqEnd.getDeleteSQL());
            resultRE = (re == null)? false : reqEnd.delete(re, requestIds);
            if (!resultRE)
                logger.log (Level.FINE, "Error deleting requests from Request End Table");
        } catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from Request End Table", se);        
        } finally {
            if (re != null){
                try{
                    re.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    re = null;
                }
            }
        }
        try{
            ms = connection.prepareStatement(methStart.getDeleteSQL());
            resultMS = (ms == null)? false : methStart.delete(ms, requestIds);
            if (!resultMS)
                logger.log (Level.FINE, "Error deleting requests from Method Start Table");
        } catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from Method Start Table", se);        
        }finally {
            if (ms != null){
                try{
                    ms.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    ms = null;
                }
            }
        }
        
        try{                   
            me = connection.prepareStatement(methEnd.getDeleteSQL());
            resultME = (me == null)? false : methEnd.delete(me, requestIds);
            if (!resultME)
                logger.log (Level.FINE, "Error deleting requests from Method End Table");
        }  catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from MethodEnd Table", se);        
        }finally {
            if (me != null){
                try{
                    me.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    me = null;
                }
            }
        }
        
        try{
            st = connection.prepareStatement(startTime.getDeleteSQL());
            resultCS = (st == null)? false : startTime.delete(st, requestIds);                
            if (!resultCS)
                logger.log (Level.FINE, "Error deleting requests from Container Start Table");
        } catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from Container Start Table", se);        
        }finally {
            if (st != null){
                try{
                    st.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    st = null;
                }
            }
        }
        try{
            et =connection.prepareStatement(endTime.getDeleteSQL());
            resultCE = (et == null)? false : endTime.delete(et, requestIds);
            if (!resultCE)
                logger.log (Level.FINE, "Error deleting requests from Container End Table");
        } catch (SQLException se){
            logger.log (Level.FINE, "Error deleting requests from End time Table", se);        
        }finally {
            if (et != null){
                try{
                    et.close();
                } catch (SQLException se){
                    // ignore
                } finally {
                    et = null;
                }
            }
        }
        if (resultRS && resultRE && resultMS && resultME && resultCS && resultCE)
            return true;
        return false;
    }
    
    public Map getNumOfRequestsProcessed () {
        Map<String, Long> map = new HashMap <String, Long> ();
        TableAccessObject[] tao = this.getAccessObjectsAsArray();
        for (int i = 0; i < tao.length; i++){
            if(tao[i] == null)
                break;
            else {
                map.put(tao[i].getName (), tao[i].getTotalEntriesProcessed());
            }
        }   
        return map;
    }
    public String getNumOfRequestsProcessedAsString (){
        Map<String, Long> map = getNumOfRequestsProcessed ();
        Set<String> keys = map.keySet();
        StringBuffer sb = new StringBuffer ();
        for (String key: keys){
            sb.append(" " + key+" : "+ map.get(key));
        }
        return sb.toString();
    }    
}
