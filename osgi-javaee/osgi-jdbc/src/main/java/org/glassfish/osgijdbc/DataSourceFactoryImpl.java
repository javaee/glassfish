/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgijdbc;

import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DataSourceFactoryImpl implements DataSourceFactory {

    // represents the driver implementation class names for various types
    // java.sql.Driver, javax.sql.DataSource, javax.sql.ConnectionPoolDataSource, javax.sql.XADataSource
    private Dictionary header;
    private BundleContext driverBundleContext;

    private static final Logger logger = Logger.getLogger(
            DataSourceFactoryImpl.class.getPackage().getName());

    private static final Locale locale = Locale.getDefault();
    public DataSourceFactoryImpl(BundleContext context) {
        this.header = context.getBundle().getHeaders();
        this.driverBundleContext = context;
    }

    public DataSource createDataSource(Properties props) throws SQLException {
        String dataSourceClass = (String) header.get(Constants.DS.replace('.', '_'));
        try {
            Class dsClass = driverBundleContext.getBundle().loadClass(dataSourceClass);
            DataSource ds = (DataSource) dsClass.newInstance();
            populateBean(props, dsClass, ds);
            return ds;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {
        String cpdsClassName = (String) header.get(Constants.CPDS.replace('.', '_'));
        try {
            Class cpdsClass = driverBundleContext.getBundle().loadClass(cpdsClassName);
            ConnectionPoolDataSource cpds = (ConnectionPoolDataSource) cpdsClass.newInstance();
            populateBean(props, cpdsClass, cpds);
            return cpds;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public XADataSource createXADataSource(Properties props) throws SQLException {
        String xadsClassName = (String) header.get(Constants.XADS.replace('.', '_'));
        try {
            Class xadsClass = driverBundleContext.getBundle().loadClass(xadsClassName);
            XADataSource xads = (XADataSource) xadsClass.newInstance();
            populateBean(props, xadsClass, xads);
            return xads;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public Driver createDriver(Properties props) throws SQLException {
        String driverClassName = (String) header.get(Constants.DRIVER.replace('.', '_'));
        try {
            Class driverClass = driverBundleContext.getBundle().loadClass(driverClassName);
            Driver driver = (Driver) driverClass.newInstance();
            populateBean(props, driverClass, driver);
            //register the driver with JDBC Driver Manager
            Class.forName(driverClassName, false, driverClass.getClassLoader());
            return driver;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    private void populateBean(Properties properties, Class clazz, Object object) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException, SQLException {
        if (properties == null) return; // nothing to do. DSF.createXXX allows null value
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

        Set keys = properties.keySet();
        Iterator keyIterator = keys.iterator();

        while (keyIterator.hasNext()) {

            String propertyName = (String) keyIterator.next();
            String value = properties.getProperty(propertyName);
            boolean propertyFound = false;

            for (PropertyDescriptor desc : propertyDescriptors) {
                if (desc.getName().equalsIgnoreCase(propertyName)) {
                    String type = desc.getPropertyType().getName();
                    Object result = null;

                    if (type != null) {
                        type = type.toUpperCase(locale);
                        try {
                            if (type.endsWith("INT") || type.endsWith("INTEGER")) {
                                result = Integer.valueOf(value);
                            } else if (type.endsWith("LONG")) {
                                result = Long.valueOf(value);
                            } else if (type.endsWith("DOUBLE")) {
                                result = Double.valueOf(value);
                            } else if (type.endsWith("FLOAT")) {
                                result = Float.valueOf(value);
                            } else if (type.endsWith("CHAR") || type.endsWith("CHARACTER")) {
                                result = value.charAt(0);
                            } else if (type.endsWith("SHORT")) {
                                result = Short.valueOf(value);
                            } else if (type.endsWith("BYTE")) {
                                result = Byte.valueOf(value);
                            } else if (type.endsWith("BOOLEAN")) {
                                result = Boolean.valueOf(value);
                            } else if (type.endsWith("STRING")) {
                                result = value;
                            }
                        } catch (Exception e) {
                            throw new SQLException(e);
                        }
                    } else {
                        throw new SQLException("Unable to find the type of property [ " + propertyName + " ]");
                    }

                    Method setter = desc.getWriteMethod();
                    if (setter != null) {
                        propertyFound = true;
                        debug("invoking setter method [" + setter.getName() + "], value [" + result + "]");
                        setter.invoke(object, result);
                    } else {
                        throw new SQLException
                                ("Unable to find the setter method for property [ " + propertyName + " ]");
                    }
                    break;
                }
            }
            if(!propertyFound){
                throw new SQLException("No such property ("+propertyName+") in " + clazz.getName());
            }
        }
    }

    public void preDestroy() {
        debug("predestroy() called");
    }

    private static void debug(String s) {
        if(logger.isLoggable(Level.FINEST)){
            logger.finest("[osgi-jdbc] : " + s);
        }
    }
}
