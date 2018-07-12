/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

//import weblogic.jdbc.common.internal.XAConnectionEnvFactory;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import javax.naming.Context;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import wlstest.functional.connector.common.utils.server.Logger;


/**
 * An object of this class is a factory of both ManagedConnection and 
 * connection factory instances.
 * This class supports connection pooling by defining methods for 
 * matching and creating connections.
 * @author Sheetal Vartak
 */
public class CciManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

  private String XADataSourceName;
  private String url;
  
  transient private Context ic;

  public CciManagedConnectionFactory() {
  }

  public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
    return new CciConnectionFactory(this, cxManager);
  }

  public Object createConnectionFactory() throws ResourceException {
    return new CciConnectionFactory(this, null);
  }

  public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info)
      throws ResourceException {

    try {
      XAConnection xacon = null;
      String userName = null;
      PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
      if (pc == null) {
        xacon = getXADataSource().getXAConnection();
      } else {
        userName = pc.getUserName();
        xacon = getXADataSource().getXAConnection(userName, new String(pc.getPassword()));
      }
      Connection con = xacon.getConnection();
      return new CciManagedConnection(this, pc, xacon, con, true, true);
    }
    catch (SQLException ex) {
      ResourceException re = new EISSystemException("SQLException: " + ex.getMessage());
      re.setLinkedException(ex);
      throw re;
    }

  }

  public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
      ConnectionRequestInfo info) throws ResourceException {

    PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
    Iterator it = connectionSet.iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      if (obj instanceof CciManagedConnection) {
        CciManagedConnection mc = (CciManagedConnection) obj;
        ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();
        if (Util.isPasswordCredentialEqual(mc.getPasswordCredential(), pc) && mcf.equals(this)) {
          return mc;
        }
      }
    }
    return null;
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {

    try {
      getXADataSource().setLogWriter(out);
    }
    catch (SQLException ex) {

      ResourceException rex = new ResourceException("SQLException");
      rex.setLinkedException(ex);
      throw rex;
    }
  }

  public PrintWriter getLogWriter() throws ResourceException {
    try {
      return getXADataSource().getLogWriter();
    }
    catch (SQLException ex) {

      ResourceException rex = new ResourceException("SQLException");
      rex.setLinkedException(ex);
      throw rex;
    }
  }

  public String getXADataSourceName() {
    return XADataSourceName;
  }

  public void setXADataSourceName(String XADataSourceName) {
    this.XADataSourceName = XADataSourceName;
  }
  
  public String getConnectionURL(){
      return url;
  }
  
  public void setConnectionURL(String connectionURL){
      this.url = connectionURL;
  }

 public XADataSource getXADataSource() throws ResourceException {
        try {
            String jdbcType = url.trim().substring(0, url.indexOf("//")).toLowerCase() ;
            if(jdbcType.contains("derby"))
              return getDerbyXADataSource();
            else if(jdbcType.contains("oracle"))
                return getOraXADataSource();
            else
                throw new NotSupportedException("Current flex mcf only support oracle & derby EIS simulation.");
        } catch (SQLException ex) {
            Logger.info("catch sql exception while exe getXADataSource()", ex);
            throw new ResourceException("catch sql exception while exe getXADataSource()", ex);
        }
  }
  
   private XADataSource getDerbyXADataSource() throws SQLException{
      String urltrim = url.trim().substring(url.indexOf("//") + 2);
      String host = urltrim.substring(0, urltrim.indexOf(":"));
      String port = urltrim.substring(urltrim.indexOf(":") + 1, urltrim.indexOf("/"));
 //     String dbname = urltrim.substring(urltrim.indexOf("/")+1);  
      String dbname = XADataSourceName + ";create=true;autocommit=false";
      Logger.info("getDerbyXADataSource() for host:" + host + ";port:" + port + ";dbname:" + dbname);

      Object ds = loadAndInstantiateClass("org.apache.derby.jdbc.ClientXADataSource");
      invokeMethod(ds, "setServerName", new Object[]{host});
      invokeMethod(ds, "setPortNumber", new Object[]{new Integer(port)});
      invokeMethod(ds, "setDatabaseName", new Object[]{dbname});

      return (XADataSource) ds;
  }
  
   private XADataSource getOraXADataSource() throws SQLException{
        Object ds = loadAndInstantiateClass("oracle.jdbc.xa.client.OracleXADataSource");
        invokeMethod(ds, "setURL", new Object[]{url});
        return (XADataSource)ds;
  }
   
   private Object loadAndInstantiateClass(String className){
        try {
            Class cls = Class.forName(className);
           Object obj = cls.newInstance();
           Logger.info("JDBC Driver Loaded and instantiated " + className + ".");
           return obj;
        
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.info("catch exception while instantiate class with non-param constructor: "+className, ex);
        }
        catch (ClassNotFoundException ex) {
            Logger.info("could not find class: "+className, ex);
        }   
        
        return null;
   }
   
   private Object invokeMethod(Object obj, String methodName, Object[] args){
       Method methodToInvoke = null;
       Object ret = null;
       Method methods[] = obj.getClass().getMethods();
       for (int i = 0; i < methods.length; i++) {
           if (methodName != null && methodName.equals(methods[i].getName())) {
               methodToInvoke = methods[i];
               break;
           }
       }

       if (methodToInvoke != null) {
           try {
               Logger.info("Found method " + methodName + ".");

               ret = methodToInvoke.invoke(obj, args);
               Logger.info("Invoked method " + methodName + ".");
               return ret;
           } catch (Exception ex) {
               Logger.info("catch exception while invoking method: " + methodName + " of obj: " + obj, ex);
           }
       } else {
           Logger.info("Unable to find method \"" + methodName + "\" in class \"" + obj + "\". Check to see if the method exists and that it is defined as public.");
       }
       return ret;
   }

  public static String throwable2StackTrace(Throwable throwable) {
    if (throwable == null) throwable = new Throwable(
        "[Null exception passed, creating stack trace for offending caller]");
    ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
    throwable.printStackTrace(new PrintStream(bytearrayoutputstream));
    return bytearrayoutputstream.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj instanceof CciManagedConnectionFactory) {
      String v1 = ((CciManagedConnectionFactory) obj).XADataSourceName;
      String v2 = this.XADataSourceName;
      return (v1 == null) ? (v2 == null) : (v1.equals(v2));
    } else {
      return false;
    }
  }

  public int hashCode() {
    if (XADataSourceName == null) {
      return (new String("")).hashCode();
    } else {
      return XADataSourceName.hashCode();
    }
  }
}
