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

/*
 * EJBRecorderHome.java
 *
 * Created on November 4, 2003, 6:29 PM
 */

package sqetests.ejb.stateful.passivate.util;

import java.util.HashMap;
import javax.naming.spi.*;
import javax.naming.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  dsingh
 */
public class EJBRecorderHome implements java.io.Serializable  {

  private static EJBRecorderHome recorderInstance = null;
  public String m_bean = null;

  private HashMap beanStatus = new HashMap();

  /** Private constructor to make class Singleton*/
  private EJBRecorderHome() {
    System.out.println("EjbRecorderHome constructor");
   
  }

  /** Creates a new instance of EJBRecorderHome */
  public static synchronized EJBRecorderHome getInstance() {
    if (recorderInstance == null) {
        System.out.println("SINGLETON%%%%%%%%%doesn't exist,creating one");

      recorderInstance = new EJBRecorderHome();

    }
    return recorderInstance;
  }

  /** Creates a new instance of EJBRecorderHome with BeanStatus as parameter*/
  public static synchronized EJBRecorderHome getInstance(HashMap beanmap) {
    if (recorderInstance == null) {

      recorderInstance = new EJBRecorderHome(beanmap);

    }
    return recorderInstance;
  }

  public EJBRecorderHome(String beanname) {
    m_bean = beanname;
  }

  public EJBRecorderHome(HashMap beanmap) {
    beanStatus = beanmap;
  }

  public void setBeanMap(String EJBName,HashMap map) {
      m_bean=EJBName;
      beanStatus.put(EJBName, map);
      
  }
  
  public HashMap getAllBeanResults(){
      System.out.println("EJBRECORDER :Returning bean results..");
      String keys=beanStatus.keySet().toString();
      System.out.println("result for these beans :"+keys);
      return beanStatus;
  }

  private Object readResolve()
             throws java.io.ObjectStreamException {
        return recorderInstance;
    }

  }
