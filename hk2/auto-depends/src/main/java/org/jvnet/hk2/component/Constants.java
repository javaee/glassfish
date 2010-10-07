package org.jvnet.hk2.component;

/**
 * Common metadata properties used by the inhabitant / habitat.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class Constants {

  /**
   * Represents class / contract type name(s).  Only used by Trackers
   * at present.
   */
  public static final String OBJECTCLASS = "objectclass";
  
  /**
   * Service property identifying a service's ranking number.
   * 
   * <p>
   * The service ranking is used by the Framework to determine the <i>natural
   * order</i> of services (when used specifically with the Tracker).
   * 
   * <p>
   * The default ranking is zero (0). A service with a ranking of
   * <code>Integer.MAX_VALUE</code> is very likely to be returned as the
   * default service, whereas a service with a ranking of
   * <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
   * 
   * <p>
   * If the supplied property value is not of type <code>Integer</code>, it is
   * deemed to have a ranking value of zero.
   * 
   * <p>
   * Hk2 manages its meta information as String, but converts to Integer
   * for comparisons.
   */
  public static final String SERVICE_RANKING = "service.ranking";
 
  /**
   * Qualifier (annotation names) used to describe the service.
   */
  public static final String QUALIFIER = "qualifier"; 

}
