package org.jvnet.hk2.component.matcher;

/**
 * Common metadata properties used by the inhabitant / habitat, mainly
 * for tracking purposes.  This is loosely modeled from OSGi.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class Constants {

  /**
   * represents class / contract type name(s)
   */
  public static final String OBJECTCLASS = "objectclass";
  
  /**
   * Service property identifying a service's ranking number.
   * 
   * <p>
   * The service ranking is used by the Framework to determine the <i>natural
   * order</i> of services (when specifically used with the Tracker).
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
  
}
