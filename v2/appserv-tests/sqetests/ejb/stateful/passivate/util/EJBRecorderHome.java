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
