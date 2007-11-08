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
 * TableInfo.java
 *
 * Created on July 13, 2005, 3:54 PM
 */

package com.sun.enterprise.admin.monitor.callflow;
import com.sun.appserv.management.monitor.CallFlowMonitor;
/**
 *
 * @author Harpreet Singh
 */
public interface TableInfo {
    
    /** Columns common across Tables */
    public static final String REQUEST_ID = "REQUEST_ID";
    public static final String REQUEST_ID_TYPE = " VARCHAR(255) ";

    public static final String TIME_STAMP = "TIME_STAMP";
    public static final String TIME_STAMP_TYPE = " BIGINT ";

    //<editor-fold defaultstate="collapsed" desc="SQL Statements Keywords">    
    /**
     * SQL Statements keywords
     */
    public static final String CREATE_TABLE = " create table ";
    public static final String NOT_NULL = " NOT NULL ";
    public static final String COMMA = " , ";
    public static final String PRIMARY_KEY = " primary key ";
    public static final String DROP_TABLE = "drop table ";
    public static final String UPDATE = " UPDATE ";
    public static final String INSERT_INTO = " INSERT INTO ";
    public static final String VALUES =  " VALUES ";
    public static final String DOT = ".";
    public static final String EQUALS = " = ";
    public static final String HAVING = " HAVING ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String MIN = " MIN " ;
    public static final String MAX = " MAX ";
    public static final String SELECT = " SELECT ";
    public static final String FROM = " FROM ";
    public static final String AS = " AS ";
    public static final String UNION = " UNION ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String SUM = " SUM ";
    public static final String DELETE = " DELETE ";
//</editor-fold>    
    
    //<editor-fold defaultstate="collapsed" desc="Request Start Table Info">
    /**
     * RequestStart Table Information
     */
    public static final String REQUEST_START_TABLE_NAME = "REQUEST_START_TBL";
    
    public static final String REQUEST_TYPE = "REQUEST_TYPE";
    public static final String REQUEST_TYPE_TYPE = " VARCHAR(25) ";
    
    public static final String TIME_STAMP_MILLIS = "TIME_STAMP_MILLIS";
    
    public static final String TIME_STAMP_MILLIS_TYPE = " BIGINT ";
    
    public static final String IP_ADDRESS = "IP_ADDRESS";
    public static final String IP_ADDRESS_TYPE = " VARCHAR (15) ";
    /*
     * Create Statement to create RequestStart Table
     * This does the following
     * create table RequestStart (REQUEST_ID VARCHAR(255) NOT NULL,
     * TIME_STAMP BIGINT, TIME_STAMP_MILLIS BIGINT, IP_ADDRESS VARCHAR (15), 
     * primary key (REQUEST_ID))
     */
    public static final String CREATE_TABLE_REQUEST_START_SQL =
            CREATE_TABLE + REQUEST_START_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA + TIME_STAMP +TIME_STAMP_TYPE +
            COMMA + TIME_STAMP_MILLIS + TIME_STAMP_MILLIS_TYPE + 
            COMMA + REQUEST_TYPE + REQUEST_TYPE_TYPE + 
            COMMA + IP_ADDRESS + IP_ADDRESS_TYPE + COMMA +
            PRIMARY_KEY + "(" + REQUEST_ID + ")" +")";
    
    /**
     * Drop table Request Start SQL
     */
    public static final String DROP_TABLE_REQUEST_START_SQL =
            DROP_TABLE + REQUEST_START_TABLE_NAME;
    
    /**
     * Update table Request Start SQL
     */
    public static final String INSERT_INTO_TABLE_REQUEST_START_SQL =
            INSERT_INTO + REQUEST_START_TABLE_NAME +VALUES + " ( "+
            " ?, ? , ? , ? , ?" + " ) ";
    /**
     * delete row from request start SQL     
     */
    public static final String DELETE_FROM_TABLE_REQUEST_START_SQL = 
            DELETE + FROM + REQUEST_START_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Request End Table Info">
    /**
     * RequestEnd Table Information
     */
    public static final String REQUEST_END_TABLE_NAME = "REQUEST_END_TBL";
    
    /*
     * Create Statement to create RequestEnd Table
     * This does the following
     * create table RequestEnd (REQUEST_ID VARCHAR(255) NOT NULL,
     * TIME_STAMP BIGINT, primary key (REQUEST_ID))
     */
    public static final String CREATE_TABLE_REQUEST_END_SQL =
            CREATE_TABLE + REQUEST_END_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA + TIME_STAMP +TIME_STAMP_TYPE +
            COMMA + PRIMARY_KEY + "(" + REQUEST_ID + ")" +")";
    
    /**
     * Drop table Request End SQL
     */
    public static final String DROP_TABLE_REQUEST_END_SQL =
            DROP_TABLE + REQUEST_END_TABLE_NAME;
    
    /**
     * Update table Request End SQL
     */
    public static final String INSERT_INTO_TABLE_REQUEST_END_SQL =
            INSERT_INTO + REQUEST_END_TABLE_NAME +VALUES + " ( "+
            " ?, ?  " + " ) ";
    
   public static final String DELETE_FROM_TABLE_REQUEST_END_SQL = 
            DELETE + FROM + REQUEST_END_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Method Start Table Info">
    /**
     * Method Start Table Information
     */
    public static final String METHOD_START_TABLE_NAME = "METHOD_START_TBL";
    
    /**
     * Column Names unique to Method Start Table
     */
    public static final String COMPONENT_TYPE = "COMPONENT_TYPE";
    public static final String COMPONENT_TYPE_TYPE = " VARCHAR(30) ";
    
    public static final String COMPONENT_NAME = "COMPONENT_NAME";
    public static final String COMPONENT_NAME_TYPE = " VARCHAR(255) ";
    
    public static final String APP_NAME = "APP_NAME";
    public static final String APP_NAME_TYPE = " VARCHAR(255) ";
    
    public static final String METHOD_NAME = "METHOD_NAME";
    public static final String METHOD_NAME_TYPE = " VARCHAR(255) ";
    
    public static final String MODULE_NAME = "MODULE_NAME";
    public static final String MODULE_NAME_TYPE = " VARCHAR(255) ";
    
    public static final String THREAD_ID = "THREAD_ID";
    public static final String THREAD_ID_TYPE = " VARCHAR(255) ";
    
    public static final String TRANSACTION_ID = "TRANSACTION_ID";
    public static final String TRANSACTION_ID_TYPE = " VARCHAR(255) ";
    
    public static final String SECURITY_ID = "SECURITY_ID";
    public static final String SECURITY_ID_TYPE = " VARCHAR(255) ";
    public static final String MINUS = " - ";
    /**
     * Create Statement for Method Start Table
     *
     * create table MethodStart ( REQUEST_ID VARCHAR(255) NOT_NULL, TIME_STAMP BIGINT NOT_NULL,
     * COMPONENT_TYPE VARCHAR(20), COMPONENT_NAME VARCHAR(255),
     * APP_NAME VARCHAR(255), METHOD_NAME VARCHAR(255), MODULE_NAME VARCHAR (255),
     * THREAD_ID VARCHAR(255), TRANSACTION_ID VARCHAR (255), SECURITY_ID VARCHAR(255),
     * primary key (REQUEST_ID, TIME_STAMP))
     *
     */
    public static final String CREATE_TABLE_METHOD_START_SQL =
            CREATE_TABLE + METHOD_START_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA +
            TIME_STAMP + TIME_STAMP_TYPE + NOT_NULL + COMMA +
            COMPONENT_TYPE + COMPONENT_TYPE_TYPE + COMMA +
            COMPONENT_NAME + COMPONENT_NAME_TYPE + COMMA +
            APP_NAME + APP_NAME_TYPE + COMMA +
            METHOD_NAME + METHOD_NAME_TYPE + COMMA +
            MODULE_NAME + MODULE_NAME_TYPE + COMMA +
            THREAD_ID + THREAD_ID_TYPE + COMMA +
            TRANSACTION_ID + TRANSACTION_ID_TYPE + COMMA +
            SECURITY_ID + SECURITY_ID_TYPE + COMMA +
            PRIMARY_KEY + " ( " + REQUEST_ID + COMMA + TIME_STAMP + ")" + ")";
    
    public static final String DROP_TABLE_METHOD_START_SQL =
            DROP_TABLE + METHOD_START_TABLE_NAME;
    
    /**
     * Update table Method Start SQL
     */
    public static final String INSERT_INTO_TABLE_METHOD_START_SQL =
            INSERT_INTO + METHOD_START_TABLE_NAME +VALUES + " ( "+
            " ?, ?, ?,  ?, ?, ?,  ?, ?, ? ,?" + " ) ";

   public static final String DELETE_FROM_TABLE_METHOD_START_SQL = 
            DELETE + FROM + METHOD_START_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Method End Table Info">
    /**
     * Method End Table Information
     */
    public static final String METHOD_END_TABLE_NAME = "METHOD_END_TBL";
    
    /**
     * Column Names unique to Method End Table
     */
    public static final String EXCEPTION_NAME = "EXCEPTION_NAME";
    public static final String EXCEPTION_NAME_TYPE = " VARCHAR(4096) ";
    
    /*
     * Create Statement to create MethodEnd Table
     * This does the following
     * create table MethodEnd (REQUEST_ID VARCHAR(255) NOT NULL,
     * TIME_STAMP BIGINT, EXCEPTION VARCHAR(4096), primary key (REQUEST_ID, TIME_STAMP))
     */
    public static final String CREATE_TABLE_METHOD_END_SQL =
            CREATE_TABLE + METHOD_END_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA + TIME_STAMP +TIME_STAMP_TYPE +
            NOT_NULL +
            COMMA + EXCEPTION_NAME + EXCEPTION_NAME_TYPE + COMMA +
            PRIMARY_KEY + "(" + REQUEST_ID + COMMA + TIME_STAMP + ")" +")";
    
    /**
     * Drop table Request End SQL
     */
    public static final String DROP_TABLE_METHOD_END_SQL =
            DROP_TABLE + METHOD_END_TABLE_NAME;
    /**
     * Update table Method End SQL
     */
    public static final String INSERT_INTO_TABLE_METHOD_END_SQL =
            INSERT_INTO + METHOD_END_TABLE_NAME +VALUES + " ( "+
            " ?, ?, ?  " + " ) ";
    
   public static final String DELETE_FROM_TABLE_METHOD_END_SQL = 
            DELETE + FROM + METHOD_END_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Start Time Table Info">
    /**
     * Start Time Table Information
     */
    public static final String START_TIME_TABLE_NAME = "START_TIME_TBL";
    
    /**
     * Column Names unique to Start Time Table
     */
    public static final String CONTAINER_TYPE = "CONT_TYPE_OR_APP_TYPE";
    public static final String CONTAINER_TYPE_TYPE = " VARCHAR(20) ";
    
    /*
     * Create Statement to create MethodEnd Table
     * This does the following
     * create table START_TIME (REQUEST_ID VARCHAR(255) NOT NULL,
     * TIME_STAMP BIGINT, CONTAINER_TYPE VARCHAR(20), primary key (REQUEST_ID, TIME_STAMP))
     */
    public static final String CREATE_TABLE_START_TIME_SQL =
            CREATE_TABLE + START_TIME_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA + TIME_STAMP +TIME_STAMP_TYPE +
            NOT_NULL +
            COMMA + CONTAINER_TYPE + CONTAINER_TYPE_TYPE + NOT_NULL + COMMA +
            PRIMARY_KEY + "(" + REQUEST_ID + COMMA + TIME_STAMP + COMMA + CONTAINER_TYPE + ")" +")";
    
    /**
     * Drop table start time SQL
     */
    public static final String DROP_TABLE_START_TIME_SQL =
            DROP_TABLE + START_TIME_TABLE_NAME;
    /**
     * Update table Start Time SQL
     */
    public static final String INSERT_INTO_TABLE_START_TIME_SQL =
            INSERT_INTO + START_TIME_TABLE_NAME +VALUES + " ( "+
            " ?, ?, ?  " + " ) ";
    
   public static final String DELETE_FROM_TABLE_START_TIME_SQL = 
            DELETE + FROM + START_TIME_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";
//</editor-fold>    
    
    //<editor-fold defaultstate="collapsed" desc="End Time Table Info">
    /**
     * End Time Table Information
     */
    public static final String END_TIME_TABLE_NAME = "END_TIME_TBL";    
    /*
     * Create Statement to create MethodEnd Table
     * This does the following
     * create table END_TIME (REQUEST_ID VARCHAR(255) NOT NULL,
     * TIME_STAMP BIGINT, CONTAINER_TYPE VARCHAR(20), primary key (REQUEST_ID, TIME_STAMP))
     */
    public static final String CREATE_TABLE_END_TIME_SQL =
            CREATE_TABLE + END_TIME_TABLE_NAME + " ( " + REQUEST_ID +
            REQUEST_ID_TYPE + NOT_NULL + COMMA + TIME_STAMP +TIME_STAMP_TYPE +
            NOT_NULL +
            COMMA + CONTAINER_TYPE + CONTAINER_TYPE_TYPE + NOT_NULL + COMMA +
            PRIMARY_KEY + "(" + REQUEST_ID + COMMA + TIME_STAMP + COMMA + CONTAINER_TYPE +")" +")";
    
    /**
     * Drop table Request End SQL
     */
    public static final String DROP_TABLE_END_TIME_SQL =
            DROP_TABLE + END_TIME_TABLE_NAME;
    /**
     * Update table Start Time SQL
     */
    public static final String INSERT_INTO_TABLE_END_TIME_SQL =
            INSERT_INTO + END_TIME_TABLE_NAME +VALUES + " ( "+
            " ?, ?, ?  " + " ) ";
    
   public static final String DELETE_FROM_TABLE_END_TIME_SQL = 
            DELETE + FROM + END_TIME_TABLE_NAME + WHERE + 
            REQUEST_ID + EQUALS + " ? ";
//</editor-fold>        
    
    //<editor-fold defaultstate="collapsed" desc="Queries: getRequestInformation">
/**
 * select 
 *          REQUEST_START_TBL.REQUEST_ID, REQUEST_START_TBL.TIME_STAMP,
 *          REQUEST_START_TBL.TIME_STAMP_MILLIS, 
 *          REQUEST_START_TBL.REQUEST_TYPE,  
 *          REQUEST_START_TBL.IP_ADDRESS,  
 *          METHOD_START_TBL.METHOD_NAME, METHOD_START_TBL.APP_NAME, 
 *          METHOD_START_TBL.SECURITY_ID, METHOD_END_TBL.EXCEPTION_NAME,
 *          REQUEST_END_TBL.TIME_STAMP -  REQUEST_START_TBL.TIME_STAMP 
 * 
 * from  
 *          "APP"."REQUEST_START_TBL", "APP"."REQUEST_END_TBL", 
 *          "APP"."METHOD_START_TBL", "APP"."METHOD_END_TBL"
 *
 * WHERE
 *          REQUEST_START_TBL.REQUEST_ID=REQUEST_END_TBL.REQUEST_ID
 *      AND  
 *          REQUEST_START_TBL.REQUEST_ID=METHOD_START_TBL.REQUEST_ID 
 *      AND
 *          REQUEST_START_TBL.REQUEST_ID=METHOD_END_TBL.REQUEST_ID
 * 
 * GROUP BY 
 *          REQUEST_START_TBL.REQUEST_ID, REQUEST_START_TBL.TIME_STAMP,           
 *          REQUEST_START_TBL.REQUEST_TYPE, METHOD_START_TBL.TIME_STAMP,
 *          REQUEST_START_TBL.TIME_STAMP_MILLIS, REQUEST_START_TBL.IP_ADDRESS, 
 *          METHOD_START_TBL.METHOD_NAME, METHOD_START_TBL.APP_NAME,
 *          METHOD_START_TBL.SECURITY_ID, METHOD_END_TBL.EXCEPTION_NAME,
 *          REQUEST_END_TBL.TIME_STAMP, METHOD_END_TBL.TIME_STAMP
 * 
 * HAVING
 *          METHOD_START_TBL.TIME_STAMP = 
 *              ( SELECT MIN (METHOD_START_TBL.TIME_STAMP) 
 *                  FROM METHOD_START_TBL 
 *                  WHERE 
 *                      METHOD_START_TBL.REQUEST_ID = REQUEST_START_TBL.REQUEST_ID) 
 *      AND
 *          METHOD_END_TBL.TIME_STAMP = 
 *              ( SELECT MAX (METHOD_END_TBL.TIME_STAMP) 
 *                  FROM METHOD_END_TBL 
 *                  WHERE
 *                      METHOD_END_TBL.REQUEST_ID = REQUEST_START_TBL.REQUEST_ID) 
 */

    public static final String COLUMN_NAMES_FOR_GET_REQUEST_INFORMATION = 
            REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP_MILLIS + COMMA +
            REQUEST_START_TABLE_NAME + DOT + IP_ADDRESS + COMMA +
            REQUEST_START_TABLE_NAME + DOT + REQUEST_TYPE + COMMA +
            
            METHOD_START_TABLE_NAME + DOT + METHOD_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + APP_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + SECURITY_ID + COMMA +
            
            METHOD_END_TABLE_NAME   +  DOT +  EXCEPTION_NAME;
    
   public static final String COLUMNS_FOR_GROUP_BY_FOR_GET_REQUEST_INFORMATION =
            REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP + COMMA +
            REQUEST_START_TABLE_NAME + DOT + REQUEST_TYPE + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP_MILLIS + COMMA +
            REQUEST_START_TABLE_NAME + DOT + IP_ADDRESS + COMMA +

            METHOD_START_TABLE_NAME + DOT + TIME_STAMP + COMMA +
            METHOD_START_TABLE_NAME + DOT + METHOD_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + APP_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + SECURITY_ID + COMMA +
            
            METHOD_END_TABLE_NAME  + DOT +  EXCEPTION_NAME + COMMA +
            REQUEST_END_TABLE_NAME + DOT + TIME_STAMP + COMMA +
            METHOD_END_TABLE_NAME  + DOT + TIME_STAMP ;
           ;
           
    public static final String TIME_DIFF_RS_RE =         
            REQUEST_END_TABLE_NAME +  DOT + TIME_STAMP + MINUS +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP;
            ;

    public static final String GET_REQUEST_INFORMATION_SQL =
            SELECT + 
                COLUMN_NAMES_FOR_GET_REQUEST_INFORMATION + COMMA +
                TIME_DIFF_RS_RE +
            FROM +
                REQUEST_START_TABLE_NAME + COMMA + REQUEST_END_TABLE_NAME + COMMA +
                METHOD_START_TABLE_NAME + COMMA + METHOD_END_TABLE_NAME +
            WHERE +
                REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + EQUALS + 
                REQUEST_END_TABLE_NAME   + DOT + REQUEST_ID +
                AND +
                REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + EQUALS + 
                METHOD_START_TABLE_NAME  + DOT + REQUEST_ID +
                AND +
                REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + EQUALS + 
                METHOD_END_TABLE_NAME    + DOT + REQUEST_ID +
            GROUP_BY +
                COLUMNS_FOR_GROUP_BY_FOR_GET_REQUEST_INFORMATION +
            HAVING +
                METHOD_START_TABLE_NAME + DOT +  TIME_STAMP + EQUALS +
                    " ( " + 
                    SELECT + MIN + " ( " + METHOD_START_TABLE_NAME + DOT + TIME_STAMP + " ) "+
                    FROM + METHOD_START_TABLE_NAME +
                    WHERE +
                        METHOD_START_TABLE_NAME + DOT + REQUEST_ID + EQUALS +
                        REQUEST_START_TABLE_NAME + DOT + REQUEST_ID +
                    " )" + AND +
                METHOD_END_TABLE_NAME + DOT + TIME_STAMP + EQUALS +
                    " ( " +
                    SELECT+ MAX + " ( " + METHOD_END_TABLE_NAME + DOT + TIME_STAMP + " ) "+
                    FROM + METHOD_END_TABLE_NAME +
                    WHERE + 
                        METHOD_END_TABLE_NAME + DOT + REQUEST_ID + EQUALS +
                        REQUEST_START_TABLE_NAME + DOT + REQUEST_ID +
                     " )";
                
      // whew      
                        
                
            
            
            
//</editor-fold>    
    
    //<editor-fold defaultstate="collapsed" desc="Queries: getCallStackInformation">
    /**
     * select
     *      'RequestStart',  REQUEST_START_TBL.REQUEST_ID,
     *      REQUEST_START_TBL.TIME_STAMP AS TIMESTAMP, REQUEST_START_TBL.TIME_STAMP_MILLIS, 
     *      REQUEST_START_TBL.REQUEST_TYPE  , '' , '', '', '', '', '', '' ,  '', ''
     * from
     *      "APP"."REQUEST_START_TBL"
     * WHERE
     *      REQUEST_START_TBL.REQUEST_ID='?'
     * UNION
     *
     * (select
     *      'MethodStart', METHOD_START_TBL.REQUEST_ID,
     *      METHOD_START_TBL.TIME_STAMP AS TIMESTAMP, '', '', '',
     *      METHOD_START_TBL.COMPONENT_TYPE, METHOD_START_TBL.COMPONENT_NAME,
     *      METHOD_START_TBL.APP_NAME, METHOD_START_TBL.METHOD_NAME,
     *      METHOD_START_TBL.MODULE_NAME, METHOD_START_TBL.THREAD_ID,
     *      METHOD_START_TBL.TRANSACTION_ID, METHOD_START_TBL.SECURITY_ID
     * from
     *      METHOD_START_TBL WHERE METHOD_START_TBL.REQUEST_ID='?')
     * UNION
     *
     * (select
     *      'MethodEnd',  METHOD_END_TBL.REQUEST_ID,
     *      METHOD_END_TBL.TIME_STAMP AS TIMESTAMP, '', '',
     *      METHOD_END_TBL.EXCEPTION_NAME, '', '', '', '', '', '' , '', ''
     * from
     *      METHOD_END_TBL
     * WHERE
     *      METHOD_END_TBL.REQUEST_ID='?')
     * UNION
     *
     * (select
     *      'RequestEnd', REQUEST_END_TBL.REQUEST_ID,
     *       REQUEST_END_TBL.TIME_STAMP AS TIMESTAMP, '', 
     *       '', '', '', '', '', '', '',  '' , '', ''
     * from
     *      "APP"."REQUEST_END_TBL"
     * WHERE
     *      REQUEST_END_TBL.REQUEST_ID='?')
     *
     * ORDER BY TIMESTAMP
     */
    public static final String TIMESTAMP_FOR_CALLSTACK_QUERY = " TIMESTAMP ";
    
    public static final String 
      REQUEST_START_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY
            = "'"+CallFlowMonitor.CALL_STACK_REQUEST_START +"'" + COMMA + 
            REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP + 
            AS + TIMESTAMP_FOR_CALLSTACK_QUERY + COMMA +
            REQUEST_START_TABLE_NAME + DOT + REQUEST_TYPE + COMMA +
            "''" + COMMA + "''" + COMMA + "''" + COMMA + 
            "''" + COMMA + "''" + COMMA + "''" + COMMA + 
            "''" + COMMA + "''" + COMMA + "''" + COMMA +
            REQUEST_START_TABLE_NAME + DOT + TIME_STAMP_MILLIS;
    
    public static final String CALLSTACK_REQUEST_START_TBL_SQL = 
            SELECT +
                REQUEST_START_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY +
            FROM +
                REQUEST_START_TABLE_NAME +
            WHERE +
                REQUEST_START_TABLE_NAME + DOT + REQUEST_ID + " =?";
    
    public static final String 
      METHOD_START_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY =
           "'"+CallFlowMonitor.CALL_STACK_METHOD_START +"'"  + COMMA + 
            METHOD_START_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            METHOD_START_TABLE_NAME + DOT + TIME_STAMP + 
            AS + TIMESTAMP_FOR_CALLSTACK_QUERY + COMMA + 
            // 'requestType', 'exceptionName'
             "''" + COMMA + "''" + COMMA + 
            METHOD_START_TABLE_NAME + DOT + COMPONENT_TYPE + COMMA +
            METHOD_START_TABLE_NAME + DOT + COMPONENT_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + APP_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + METHOD_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + MODULE_NAME + COMMA +
            METHOD_START_TABLE_NAME + DOT + THREAD_ID + COMMA +
            METHOD_START_TABLE_NAME + DOT + TRANSACTION_ID + COMMA +
            METHOD_START_TABLE_NAME + DOT + SECURITY_ID + COMMA + "0";
    
    public static final String CALLSTACK_METHOD_START_TBL_SQL =
            SELECT +
                METHOD_START_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY +
            FROM +
                METHOD_START_TABLE_NAME +
            WHERE + 
                METHOD_START_TABLE_NAME + DOT + REQUEST_ID + " = ?";
       
    public static final String 
      METHOD_END_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY
            = "'"+CallFlowMonitor.CALL_STACK_METHOD_END +"'"  + COMMA + 
            METHOD_END_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            METHOD_END_TABLE_NAME + DOT + TIME_STAMP + 
            AS + TIMESTAMP_FOR_CALLSTACK_QUERY + COMMA +        
            // 'requestType'
            "''" + COMMA + 
            METHOD_END_TABLE_NAME + DOT + EXCEPTION_NAME + COMMA +
            // 'componentType', 'componentName', 'appname', 'methodname',
            // 'modulename', 'threadid', 'transactionid','securityid'
            "''" + COMMA + "''" + COMMA + "''" + COMMA + "''" + COMMA + 
            "''" + COMMA + "''" + COMMA + "''" + COMMA + "''" + COMMA + "0";

    
    
    public static final String CALLSTACK_METHOD_END_TBL_SQL = 
            SELECT +
                METHOD_END_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY +
            FROM +
                METHOD_END_TABLE_NAME +
            WHERE + 
                METHOD_END_TABLE_NAME + DOT + REQUEST_ID  + " = ?";
  
    public static final String 
      REQUEST_END_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY
            ="'"+CallFlowMonitor.CALL_STACK_REQUEST_END +"'"  + COMMA + 
            REQUEST_END_TABLE_NAME + DOT + REQUEST_ID + COMMA +
            REQUEST_END_TABLE_NAME + DOT + TIME_STAMP + 
            AS + TIMESTAMP_FOR_CALLSTACK_QUERY + COMMA +
            //  ''requestType' , 'exceptionName',
            "''" + COMMA + "''" + COMMA +
            // 'componentType', 'componentName', 'appname', 'methodname',
            // 'modulename', 'threadid', 'transactionid','securityid'
            "''" + COMMA + "''" + COMMA + "''" + COMMA + "''" + COMMA + 
            "''" + COMMA + "''" + COMMA + "''" + COMMA + "''" + COMMA + "0";
    
    public static final String CALLSTACK_REQUEST_END_TBL_SQL = 
            SELECT +
                REQUEST_END_COLUMNS_FOR_GET_CALLSTACK_INFORMATION_QUERY +
            FROM +
                REQUEST_END_TABLE_NAME +
            WHERE +
                REQUEST_END_TABLE_NAME + DOT + REQUEST_ID + " = ?";
    
    public static final String GET_CALLSTACK_INFORMATION_SQL = 
            CALLSTACK_REQUEST_START_TBL_SQL + 
            UNION +
            " ( " + CALLSTACK_METHOD_START_TBL_SQL + " ) " + 
            UNION +
            " ( " + CALLSTACK_METHOD_END_TBL_SQL + " ) " +
            UNION +
            " ( " + CALLSTACK_REQUEST_END_TBL_SQL + " ) " + 
            ORDER_BY + TIMESTAMP_FOR_CALLSTACK_QUERY;
    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Queries: getPieInformation">    
    
/**
 * select START_TIME_TBL.containerTypeOrApplicationType,
 *        SUM (START_TIME_TBL.TIME_STAMP)
 * FROM START_TIME_TBL
 * WHERE 
 *   START_TIME_TBL.RID = 'RequestID_1'
 * GROUP BY START_TIME_TBL.containerTypeOrApplicationType
 * 
 */    
    public static final String GET_PIE_INFORMATION_START_TIME_SQL = 
            SELECT +
                 START_TIME_TABLE_NAME + DOT + CONTAINER_TYPE + COMMA +
                 SUM + " ( "+ START_TIME_TABLE_NAME + DOT + TIME_STAMP +" ) " +
            FROM +
                START_TIME_TABLE_NAME  + 
            WHERE +
                    START_TIME_TABLE_NAME + DOT + REQUEST_ID + EQUALS + " ? " +
           GROUP_BY + START_TIME_TABLE_NAME + DOT + CONTAINER_TYPE;

/**
 * select END_TIME_TBL.containerTypeOrApplicationType,
 *        SUM (END_TIME_TBL.TIME_STAMP)
 * FROM  END_TIME_TBL
 * WHERE END_TIME_TBL.RID = 'RequestID_1'
 * GROUP BY END_TIME_TBL.containerTypeOrApplicationType
 * 
 */    
    public static final String GET_PIE_INFORMATION_END_TIME_SQL = 
            SELECT +
                 END_TIME_TABLE_NAME + DOT + CONTAINER_TYPE + COMMA +
                 SUM + " ( "+ END_TIME_TABLE_NAME + DOT + TIME_STAMP +" ) " +
            FROM +
                END_TIME_TABLE_NAME  + 
            WHERE +
                    END_TIME_TABLE_NAME + DOT + REQUEST_ID + EQUALS + " ? " +
           GROUP_BY + END_TIME_TABLE_NAME + DOT + CONTAINER_TYPE;
    
  //</editor-fold>      
}
