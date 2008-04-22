/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli;
import java.sql.*;
import java.io.File;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.cli.framework.CLILogger;

/**
 *  This class will created a dummy database called testconnectivity.
 *  and handshake with the database to get the Client Driver info.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a> 
 *  @version  $Revision: 1.2 $
 */
public class DatabaseReporter {
    private static final StringManager lsm = StringManager.getManager(DatabaseReporter.class);
    private static final CLILogger logger = CLILogger.getInstance();
    private static final String DERBY_CLIENT_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    private static final String URL = "jdbc:derby://localhost:";
    private static final String DB_NAME = "/testconnectivity;create=true";
    private static final String USER = "APP";
    private static final String PASS = "APP";
    private static final String DB_PORT = "1527";
    private static final String DB_HOME = System.getProperty("user.dir");
    final private String dcn, url, user, password, dbhome, dbport;
    final private Connection conn;

        //constructor
    public DatabaseReporter( final String dbhome, final String dbport, final String dcn,
                             final String url, final String user, final String password) throws Exception {
	    this.dcn = dcn;
    	this.user = user;
    	this.password = password;
        this.dbhome = dbhome;
        this.dbport = dbport;
        this.url = url+dbport+DB_NAME;
    	Class.forName(dcn);
    	conn = DriverManager.getConnection(this.url, this.user, this.password);
    }

        /**
         * display the database info.
         */
    private void display() throws Exception
    {
        logger.printMessage(getDatabaseInfoMsg());
        logger.printMessage(getDatabaseDriverNameMsg());
        logger.printMessage(getDatabaseDriverVersionMsg());
        logger.printMessage(getJDBCSpecificationMsg());
    }

        /**
         * get database info msg.
         */
    private String getDatabaseInfoMsg()
    {
        return (lsm.getString("database.info.msg"));
    }
    
        /**
         * get database driver name.
         */
    private String getDatabaseDriverNameMsg() throws SQLException
    {
        final String sDriverName = conn.getMetaData().getDriverName();
        return ( lsm.getString("database.driver.name.msg", sDriverName));
    }

        /**
         * get database driver version.
         */
    private String getDatabaseDriverVersionMsg() throws SQLException
    {
        final String sDriverVersion = conn.getMetaData().getDriverVersion();
        return ( lsm.getString("database.driver.version.msg", sDriverVersion));
    }

        /**
         * get jdbc major and minor version.
         */
    private String getJDBCSpecificationMsg() throws SQLException
    {
        final int iJDBCMajorVersion = conn.getMetaData().getJDBCMajorVersion();
        final int iJDBCMinorVersion = conn.getMetaData().getJDBCMinorVersion();
        final String sJDBCSpec = iJDBCMajorVersion + "." + iJDBCMinorVersion;
        return ( lsm.getString("jdbc.version.msg", sJDBCSpec));
    }

        /**
         *  remove database file
         */
    private void removeDatabaseFile()
    {
		File f = new File(dbhome, "testconnectivity");
		FileUtils.whack(f);
    }
    
    

    public static void main(final String[] args) throws Exception {
	    DatabaseReporter dmt = null;
    	if (args == null || (args.length < 6 && args.length>2) ) {
	        dmt = new DatabaseReporter(DB_HOME, DB_PORT, DERBY_CLIENT_DRIVER_CLASS_NAME, URL, USER, PASS);
    	}
        else if (args.length == 2) {
	        dmt = new DatabaseReporter(args[0], args[1], DERBY_CLIENT_DRIVER_CLASS_NAME, URL, USER, PASS);
        }
    	else {
	        dmt = new DatabaseReporter(args[0], args[1], args[2], args[3], args[4], args[5]);
    	}
        try {
            dmt.display();
            dmt.removeDatabaseFile();
        }
        catch (Exception e) {
            logger.printMessage(lsm.getString("UnableToConnectToDatabase"));
            System.exit(1);
        }
        
    }
} 
