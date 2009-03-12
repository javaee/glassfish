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

package com.sun.enterprise.security.web.integration;

import com.sun.enterprise.security.web.*;
import com.sun.enterprise.security.*;
import java.util.*;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import java.util.logging.*; 
import com.sun.logging.LogDomains;
import java.security.Permission;
import java.security.Permissions;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.web.*;
import com.sun.enterprise.security.acl.*;
/**
 * This class is used for generating Web permissions based on the 
 * deployment descriptor.
 * @author Harpreet Singh
 * @author Jean-Francois Arcand
 * @author Ron Monzillo
 */
public class WebPermissionUtil {
    private static Logger logger = 
	Logger.getLogger(LogDomains.SECURITY_LOGGER);
    
    public WebPermissionUtil() {
    }
    
    /* changed to order default pattern / below extension */
    private static final int PT_DEFAULT      = 0;
    private static final int PT_EXTENSION    = 1;
    private static final int PT_PREFIX	     = 2;
    private static final int PT_EXACT 	     = 3;
      
    static int patternType(Object urlPattern) {
	String pattern = urlPattern.toString();
	if (pattern.startsWith("*.")) return PT_EXTENSION;
	else if (pattern.startsWith("/") && pattern.endsWith("/*")) 
	    return PT_PREFIX;
	else if (pattern.equals("/")) return PT_DEFAULT;
	else return PT_EXACT;
    }
    /**
     * Exclude list when processing resource to url mapping.
     **/
    private static ArrayList skippableList;
    
    static {
        skippableList = new ArrayList();
        skippableList.add("meta-inf");
        skippableList.add("web-inf");
        skippableList.add("tld");
        skippableList.add(".com.sun.deployment.backend.lock");
    }

    static boolean implies(String pattern, String path) {

        // Check for exact match
        if (pattern.equals(path))
            return (true);

        // Check for path prefix matching
        if (pattern.startsWith("/") && pattern.endsWith("/*")) {
            pattern = pattern.substring(0, pattern.length() - 2);

	    int length = pattern.length();

            if (length == 0) return (true);  // "/*" is the same as "/"

	    return (path.startsWith(pattern) && 
		    (path.length() == length || 
		     path.substring(length).startsWith("/")));
        }

        // Check for suffix matching
        if (pattern.startsWith("*.")) {
            int slash = path.lastIndexOf('/');
            int period = path.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) &&
                path.endsWith(pattern.substring(1))) {
                return (true);
            }
            return (false);
        }

        // Check for universal mapping
        if (pattern.equals("/"))
            return (true);

        return (false);
    }

    public static HashMap parseConstraints(WebBundleDescriptor wbd)
    {
	
      if(logger.isLoggable(Level.FINE)){
	  logger.entering("WebPermissionUtil", "parseConstraints");
      }

      HashMap qpMap = new HashMap();

      // bootstrap the map with the default pattern;
      qpMap.put("/", new MapValue("/"));

      //Enumerate over security constraints
      Enumeration esc = wbd.getSecurityConstraints(); 
      while (esc.hasMoreElements()) {
	  
	  if(logger.isLoggable(Level.FINE)){
	      logger.log(Level.FINE,"JACC: constraint translation: begin parsing security constraint");
	  }

	  SecurityConstraint sc = (SecurityConstraint) esc.nextElement();
	  AuthorizationConstraint ac = sc.getAuthorizationConstraint();
	  UserDataConstraint udc = sc.getUserDataConstraint();

	  // Enumerate over collections of URLPatterns within constraint
	  for (WebResourceCollection wrc: sc.getWebResourceCollections()) {

	      if(logger.isLoggable(Level.FINE)){
		  logger.log(Level.FINE,"JACC: constraint translation: begin parsing web resource collection");
	      }

	      // Enumerate over URLPatterns within collection
	      for (String url: wrc.getUrlPatterns()) {
                  if (url != null) {
 		      // FIX TO BE CONFIRMED: encode all colons
 		      url = url.replaceAll(":","%3A");
 		  }

		  if(logger.isLoggable(Level.FINE)){
		      logger.log(Level.FINE,"JACC: constraint translation: process url: "+url);
		  }

		  // determine if pattern is already in map
		  MapValue mValue = (MapValue) qpMap.get(url);

		  // apply new patterns to map
		  if (mValue == null) {
		      mValue = new MapValue(url);

		      //Iterate over patterns in map
		      Iterator it = qpMap.keySet().iterator(); 
		      while (it.hasNext()) {

			  String otherUrl = (String) it.next();

			  int otherUrlType = patternType(otherUrl);
			  switch(patternType(url)) {

			      // if the new url/pattern is a path-prefix 
			      // pattern, it must be qualified by every 
			      // different (from it) path-prefix pattern 
			      // (in the map) that is implied by the new 
			      // pattern, and every exact pattern (in the map)
			      // that is implied by the new url.
			      // Also, the new pattern  must be added as a 
			      // qualifier of the default pattern, and every 
			      // extension pattern (existing in the map), and 
			      // of every different path-prefix pattern that 
			      // implies the new pattern.
			      // Note that we know that the new pattern does
			      // not exist in the map, thus we know that the
			      // new pattern is different from any existing
			      // path prefix pattern.
	     
			      case PT_PREFIX:
				  if ((otherUrlType == PT_PREFIX || 
				      otherUrlType == PT_EXACT) &&
				      implies(url,otherUrl))
				      mValue.addQualifier(otherUrl);
			  
				  else if (otherUrlType == PT_PREFIX &&
					   implies(otherUrl,url))
				      ((MapValue) qpMap.get(otherUrl)).
					  addQualifier(url);
				  
				  else if (otherUrlType == PT_EXTENSION ||
				       otherUrlType == PT_DEFAULT)
				      ((MapValue) qpMap.get(otherUrl)).
					  addQualifier(url);
				  break;

			      // if the new pattern is an extension pattern,
			      // it must be qualified by every path-prefix
			      // pattern (in the map), and every exact
			      // pattern (in the map) that is implied by
			      // the new pattern.
			      // Also, it must be added as a qualifier of
			      // the defualt pattern, if it exists in the
			      // map.
			      case PT_EXTENSION:
				  if (otherUrlType == PT_PREFIX || 
				       (otherUrlType == PT_EXACT &&
					implies(url,otherUrl)))
				      mValue.addQualifier(otherUrl);

				  else if (otherUrlType == PT_DEFAULT)
				      ((MapValue) qpMap.get(otherUrl)).
					  addQualifier(url); 
				  break;

			      // if the new pattern is the default pattern
			      // it must be qualified by every other pattern
			      // in the map.
			      case PT_DEFAULT:
				  if (otherUrlType != PT_DEFAULT) 
				      mValue.addQualifier(otherUrl);
				  break;

			      // if the new pattern is an exact pattern, it
			      // is not be qualified, but it must be added as 
			      // as a qualifier to the default pattern, and to
			      // every path-prefix or extension pattern (in 
			      // the map) that implies the new pattern.
			      case PT_EXACT:
				  if ((otherUrlType == PT_PREFIX || 
				       otherUrlType == PT_EXTENSION) &&
				      implies(otherUrl,url))
				      ((MapValue) qpMap.get(otherUrl)).
					  addQualifier(url); 
				  else if (otherUrlType == PT_DEFAULT)
				      ((MapValue) qpMap.get(otherUrl)).
					  addQualifier(url);
				  break;
			  }
		      }

		      // add the new pattern and its pattern spec to the map
		      qpMap.put(url, mValue);

		  }

		  BitSet methods = 
		      MapValue.methodArrayToSet(wrc.getHttpMethodsAsArray());

		  if(logger.isLoggable(Level.FINE)){
		      logger.log(Level.FINE,"JACC: constraint translation: methods of collection: "+ MapValue.getActions(methods));
		  }

		  if (ac == null) {
		      if(logger.isLoggable(Level.FINE)){
			  logger.log(Level.FINE,"JACC: constraint translation: collection is unchecked for authorization at methods: "+ MapValue.getActions(methods));
		      }
		      mValue.setPredefinedOutcomeOnMethods(methods,true);
		  }
		  else {
		      Enumeration eroles = ac.getSecurityRoles();
		      if (!eroles.hasMoreElements()) {
			  if(logger.isLoggable(Level.FINE)){
			      logger.log(Level.FINE,"JACC: constraint translation: collection is exclude at methods: "+ MapValue.getActions(methods));
			  }
			  mValue.setPredefinedOutcomeOnMethods(methods,false);
		      }
		      else while (eroles.hasMoreElements()) {
			  SecurityRoleDescriptor srd = 
			      (SecurityRoleDescriptor)eroles.nextElement();
			  mValue.setRoleOnMethods(srd.getName(),methods,wbd);
			  if(logger.isLoggable(Level.FINE)){
			      logger.log(Level.FINE,"JACC: constraint translation: collection is athorized to: "+ srd.getName() + " at methods: "+ MapValue.getActions(methods));
			  }
		      }
		  }

		  if (udc == null) {
		      if(logger.isLoggable(Level.FINE)){
			  logger.log(Level.FINE,"JACC: constraint translation: collection requires no transport guarantee at methods: "+ MapValue.getActions(methods));
		      }
		      mValue.setConnectOnMethods(null,methods);
		  }
		  else {
		      if(logger.isLoggable(Level.FINE)){
			  logger.log(Level.FINE,"JACC: constraint translation: collection requires transport guarantee: "+ udc.getTransportGuarantee()+ " at methods: "+ MapValue.getActions(methods));
		      }
		      mValue.setConnectOnMethods(udc.getTransportGuarantee(),
						 methods);
		  }
   
		  if(logger.isLoggable(Level.FINE)){
		      logger.log(Level.FINE,"JACC: constraint translation: end processing url: "+url);
		  }
	      }

	      if(logger.isLoggable(Level.FINE)){
		  logger.log(Level.FINE,"JACC: constraint translation: end parsing web resource collection");
	      }
	  }

	  if(logger.isLoggable(Level.FINE)){
	      logger.log(Level.FINE,"JACC: constraint translation: end parsing security constraint");
	  }
      }

      if(logger.isLoggable(Level.FINE)){
	  logger.exiting("WebPermissionUtil","parseConstraints");
      }

      return qpMap;
    }

    public static void processConstraints(WebBundleDescriptor wbd,
					  PolicyConfiguration pc)
    throws javax.security.jacc.PolicyContextException 
    {
	if(logger.isLoggable(Level.FINE)){
	    logger.entering("WebPermissionUtil", "processConstraints");
	    logger.log(Level.FINE,"JACC: constraint translation: CODEBASE = "+
		       pc.getContextID());
	}

	HashMap qpMap = parseConstraints(wbd);
	HashMap roleMap = new HashMap();

	Permissions excluded = new Permissions();
	Permissions unchecked = new Permissions();

	// for each urlPatternSpec in the map
	if(logger.isLoggable(Level.FINE)){
	    logger.log(Level.FINE,"JACC: constraint capture: begin processing qualified url patterns");
	}

	Iterator it = qpMap.values().iterator();
	while (it.hasNext()) {
	    MapValue m = (MapValue) it.next();
	    if (!m.irrelevantByQualifier) {

		String name = m.urlPatternSpec.toString();

		if(logger.isLoggable(Level.FINE)){
		    logger.log(Level.FINE,"JACC: constraint capture: urlPattern: "+ name);
		}

		// handle excluded method
		BitSet methods = m.getExcludedMethods();
		if (!methods.isEmpty()) {

		    if(logger.isLoggable(Level.FINE)){
			logger.log(Level.FINE,"JACC: constraint capture: adding excluded methods: "+ MapValue.getActions(methods));

		    }

		    String[] actions = MapValue.getMethodArray(methods);
		    excluded.add(new WebResourcePermission(name,actions));
		    excluded.add(new WebUserDataPermission(name,actions,null));
		}

		// handle methods requring  role
		HashMap rMap = m.getRoleMap();
		Iterator rit = rMap.keySet().iterator();
		while (rit.hasNext()) {

		    String role = (String) rit.next();
		    methods = (BitSet) rMap.get(role);

		    if (!methods.isEmpty()) {

			Permissions p = (Permissions) roleMap.get(role);
			if (p == null) {
			    p = new Permissions();
			    roleMap.put(role,p);
			}

			if(logger.isLoggable(Level.FINE)){
			    logger.log(Level.FINE,"JACC: constraint capture: adding methods that may be called by role: "+ role+" methods: "+ MapValue.getActions(methods));
			}

			String[] actions = MapValue.getMethodArray(methods);
			p.add(new WebResourcePermission(name,actions));
		    }
		}

		// handle transport constrained methods (skip unprotected
		// that is, connectKey index == 0)
		for (int i=1; i<MethodValue.connectKeys.length; i++) {
		    methods = m.getConnectMap(1<<i);
		    if (!methods.isEmpty()) {
			
			if(logger.isLoggable(Level.FINE)){

			    logger.log(Level.FINE,"JACC: constraint capture: adding methods that accept connections with protection: "+ MethodValue.connectKeys[i]+" methods: "+ MapValue.getActions(methods));
			}

			String[] actions = MapValue.getMethodArray(methods);
			unchecked.add(new WebUserDataPermission
			    (name, actions,
			     (String) MethodValue.connectKeys[i]));
		    }
		}

		// handle methods that are not auth constrained
		methods = m.getAuthConstrainedMethods();
		if (!methods.get(MethodValue.AllMethodsIdx)) {
		    String actions;
		    if (methods.isEmpty()) {
			actions = null;
		    } else {
			actions = "!" + MapValue.getActions(methods);
		    }
		    if(logger.isLoggable(Level.FINE)){
			logger.log(Level.FINE,"JACC: constraint capture: adding unchecked (for authorization) methods: "+ actions);
		    }
		    unchecked.add(new WebResourcePermission(name,actions));
		} 

		// handle methods that are not transport constrained
		methods = m.getTransportConstrainedMethods();
		if (!methods.get(MethodValue.AllMethodsIdx)) {
		    String actions;
		    if (methods.isEmpty()) {
			actions = null;
		    } else {
			actions = "!" + MapValue.getActions(methods);
		    }
		    if(logger.isLoggable(Level.FINE)){
			logger.log(Level.FINE,"JACC: constraint capture: adding methods that accept unprotected connections: "+ actions);
		    }
		    unchecked.add(new WebUserDataPermission(name,actions));
		}
	    }
	}

	if(logger.isLoggable(Level.FINE)){
	    logger.log(Level.FINE,"JACC: constraint capture: end processing qualified url patterns");

	    Enumeration e = excluded.elements();
	    while (e.hasMoreElements()) {
		Permission p = (Permission) e.nextElement();
		String ptype = (p instanceof WebResourcePermission) ? "WRP  " : "WUDP ";
		logger.log(Level.FINE,"JACC: permission(excluded) type: "+ ptype + " name: "+ p.getName() + " actions: "+ p.getActions());
	    }

	    e = unchecked.elements();
	    while (e.hasMoreElements()) {
		Permission p = (Permission) e.nextElement();
		String ptype = (p instanceof WebResourcePermission) ? "WRP  " : "WUDP ";
		logger.log(Level.FINE,"JACC: permission(unchecked) type: "+ ptype + " name: "+ p.getName() + " actions: "+ p.getActions());
	    }
	}
	
	pc.addToExcludedPolicy(excluded);

	pc.addToUncheckedPolicy(unchecked);

	it = roleMap.keySet().iterator();
	while (it.hasNext()) {
	    String role = (String) it.next();
	    Permissions pCollection = (Permissions) roleMap.get(role);
	    pc.addToRole(role,pCollection);

	    if(logger.isLoggable(Level.FINE)){
		Enumeration e = pCollection.elements();
		while (e.hasMoreElements()) {
		    Permission p = (Permission) e.nextElement();
		    String ptype = (p instanceof WebResourcePermission) ? "WRP  " : "WUDP ";
		    logger.log(Level.FINE,"JACC: permission("+ role + ") type: "+ ptype + " name: "+ p.getName() + " actions: "+ p.getActions());
		}

	    }
	}

	if(logger.isLoggable(Level.FINE)){
	    logger.exiting("WebPermissionUtil", "processConstraints");
	}

    }
      
    public static void createWebRoleRefPermission(WebBundleDescriptor wbd, 
						  PolicyConfiguration pc)
	throws javax.security.jacc.PolicyContextException 
    {
	if(logger.isLoggable(Level.FINE)){
	    logger.entering("WebPermissionUtil", "createWebRoleRefPermission");
	    logger.log(Level.FINE,"JACC: role-reference translation: Processing WebRoleRefPermission : CODEBASE = "+ pc.getContextID());
	}
	List role = new ArrayList();
	Set roleset = wbd.getRoles();
        Set<WebComponentDescriptor> descs = wbd.getWebComponentDescriptors();
	//V3 Commented for(Enumeration e = wbd.getWebComponentDescriptors(); e.hasMoreElements();){
        for (WebComponentDescriptor comp : descs) {
	    //V3 Commented WebComponentDescriptor comp = (WebComponentDescriptor) e.nextElement();

	    String name = comp.getCanonicalName();
	    Enumeration  esrr = comp.getSecurityRoleReferences();

	    for (; esrr.hasMoreElements();){
		SecurityRoleReference srr = (SecurityRoleReference)esrr.nextElement();
		if(srr != null){
		    String action = srr.getRolename();
		    WebRoleRefPermission wrrp = new WebRoleRefPermission(name, action);
		    role.add(new Role(action));
		    pc.addToRole(srr.getSecurityRoleLink().getName(),wrrp);
		    if(logger.isLoggable(Level.FINE)){
			logger.log(Level.FINE,"JACC: role-reference translation: RoleRefPermission created with name(servlet-name)  = "+ name  + 
				   " and action(Role-name tag) = " + action + " added to role(role-link tag) = "+ srr.getSecurityRoleLink().getName());
		    }

		}
	    }
	    if(logger.isLoggable(Level.FINE)){
		logger.log(Level.FINE,"JACC: role-reference translation: Going through the list of roles not present in RoleRef elements and creating WebRoleRefPermissions ");
	    }
	    for(Iterator it = roleset.iterator(); it.hasNext();){
		Role r = (Role)it.next();
		if(logger.isLoggable(Level.FINE)){
		    logger.log(Level.FINE,"JACC: role-reference translation: Looking at Role =  "+r.getName());
		}
		if(!role.contains(r)){
		    String action = r.getName();
		    WebRoleRefPermission wrrp = new WebRoleRefPermission(name, action);
		    pc.addToRole(action ,wrrp);
		    if(logger.isLoggable(Level.FINE)){
			logger.log(Level.FINE,"JACC: role-reference translation: RoleRef  = "+ action + 
				   " is added for servlet-resource = " + name);
			logger.log(Level.FINE, "JACC: role-reference translation: Permission added for above role-ref =" 
				   + wrrp.getName() +" "+ wrrp.getActions());
		    }
		}
	    }
	}
	if(logger.isLoggable(Level.FINE)){
	    logger.exiting("WebPermissionUtil", "createWebRoleRefPermission");
	}
        
        // START S1AS8PE 4966609
        /**
         * For every security role in the web application add a
         * WebRoleRefPermission to the corresponding role. The name of all such
         * permissions shall be the empty string, and the actions of each 
         * permission shall be the corresponding role name. When checking a 
         * WebRoleRefPermission from a JSP not mapped to a servlet, use a 
         * permission with the empty string as its name
         * and with the argument to isUserInRole as its actions
         */
        for(Iterator it = roleset.iterator(); it.hasNext();){
            Role r = (Role)it.next();
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,
                    "JACC: role-reference translation: Looking at Role =  "
                        + r.getName());
            }
            String action = r.getName();
            WebRoleRefPermission wrrp = new WebRoleRefPermission("", action);
            pc.addToRole(action ,wrrp);
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,
                    "JACC: role-reference translation: RoleRef  = "
                    + action 
                    + " is added for jsp's that can't be mapped to servlets");
                logger.log(Level.FINE, 
                    "JACC: role-reference translation: Permission added for above role-ref =" 
                     + wrrp.getName() +" "+ wrrp.getActions());
            }
        }
        // END S1AS8PE 4966609
        
        
    }
    
}


class MethodValue {

    int index;
    boolean authConstrained;
    boolean excluded;
    List roleList;
    int connectSet;

    static final int AllMethodsIdx = 0;

    private static ArrayList<String> methodNames = new ArrayList();
    static {
	methodNames.add(0,null);
	methodNames.add("DELETE");
	methodNames.add("GET");
	methodNames.add("HEAD");
	methodNames.add("OPTIONS");
	methodNames.add("POST");
	methodNames.add("PUT");
	methodNames.add("TRACE");
    };

    static Object connectKeys[] = 
    { "NONE",
      "INTEGRAL",
      "CONFIDENTIAL"
    };

    static int connectTypeNone = 1;
    static HashMap connectHash = new HashMap();
    static 
    {
	for (int i=0; i<connectKeys.length; i++)
	    connectHash.put(connectKeys[i], new Integer(1<<i));
    };

    MethodValue (String methodName)
    {
	index = getMethodIndex(methodName);
	this.authConstrained = true;
	this.excluded = false;
	this.roleList = new ArrayList();
	this.connectSet = 0;
    } 

    static String getMethodName(int index) 
    {
	synchronized(methodNames) {
	    return methodNames.get(index);
	}
    }

    static int getMethodIndex(String name) 
    {
	synchronized(methodNames) {
	    int index = methodNames.indexOf(name);
	    if (index < 0) {
		index = methodNames.size();
		methodNames.add(index,name);
	    }	
	    return index;
	}
    }
}

class MapValue {

    int patternType;

    int patternLength;

    boolean irrelevantByQualifier;

    StringBuffer urlPatternSpec;

    HashMap<String,MethodValue> methodValues = 
        new HashMap<String,MethodValue>();

    static String getActions (BitSet methodSet)
    {

	// should never be null, and don't call this when no bits are set
	if (methodSet == null || methodSet.isEmpty()) {
	    throw new IllegalArgumentException
		("internal constraint tranlation error - empty methodSet");
	} else if (methodSet.get(MethodValue.AllMethodsIdx)) {
	    // return null if all methods bit is set
	    return null;
	} 
	    
	StringBuffer actions = null;

	for (int i=methodSet.nextSetBit(0); i>=0; i=methodSet.nextSetBit(i+1)){
	    if (actions == null) {
		actions = new StringBuffer();
	    } else {
		actions.append(",");
	    }
	    actions.append(MethodValue.getMethodName(i));
	}

	return (actions == null ? null : actions.toString());
    }

    static String[] getMethodArray (BitSet methodSet)
    {
	// should never be null, and don't call this when no bits are set
	if (methodSet == null || methodSet.isEmpty()) {
	    throw new IllegalArgumentException
		("internal constraint tranlation error - empty methodSet");
	} else if (methodSet.get(MethodValue.AllMethodsIdx)) {
	    // return null if all methods bit is set
	    return null;
	} 
	    
	int size = 0;

	ArrayList<String> methods = new ArrayList();

	for (int i=methodSet.nextSetBit(0); i>=0; i=methodSet.nextSetBit(i+1)) {
	    methods.add(MethodValue.getMethodName(i));
	    size += 1;
	}

	return (String[]) methods.toArray(new String[size]);
    }

    static BitSet methodArrayToSet(String[] methods)
    {
	BitSet methodSet = new BitSet();

	if (methods == null || methods.length == 0) {
	    methodSet.set(MethodValue.AllMethodsIdx);
	} else  for (int i=0; i<methods.length; i++) {
	    int bit = MethodValue.getMethodIndex(methods[i]);
	    methodSet.set(bit);
	}

	return methodSet;
    };

    MapValue (String urlPattern)
    {
	this.patternType = WebPermissionUtil.patternType(urlPattern);
	this.patternLength = urlPattern.length();
	this.irrelevantByQualifier = false;
	this.urlPatternSpec = new StringBuffer(urlPattern);
	this.methodValues = new HashMap();
    } 

    void addQualifier(String urlPattern) 
    {
	if (WebPermissionUtil.implies(urlPattern,
		    this.urlPatternSpec.substring(0,this.patternLength)))
	    this.irrelevantByQualifier = true;
	this.urlPatternSpec.append(":" + urlPattern);
    }

    MethodValue getMethodValue(int methodIndex) 
    {
	String methodName = MethodValue.getMethodName(methodIndex);

	synchronized(methodValues) {
	    MethodValue methodValue = methodValues.get(methodName);
	    if (methodValue == null) {
		methodValue = new MethodValue(methodName);
		methodValues.put(methodName,methodValue);
	    }
	    return methodValue;
	}
    }

    void setRoleOnMethods(String role,BitSet methodSet,WebBundleDescriptor wbd)
    {
	if (role.equals("*")) {

	    Iterator it = wbd.getRoles().iterator();
	    while(it.hasNext()) {
		setRoleOnMethods(((Role)it.next()).getName(),methodSet,wbd);
	    } 

	} else for (int i = methodSet.nextSetBit(0); i >= 0; 
		    i = methodSet.nextSetBit(i+1)) {

	    MethodValue methodValue = getMethodValue(i);
		
	    if (methodValue.roleList.contains(role)) {
		continue;
	    }
	    
	    methodValue.roleList.add(role);
	}
    }

    void setPredefinedOutcomeOnMethods (BitSet methodSet, boolean outcome)
    {
	for (int i = methodSet.nextSetBit(0); i >= 0; 
	     i = methodSet.nextSetBit(i+1)) {

	    MethodValue methodValue = getMethodValue(i);

	    if (!outcome) {
		methodValue.excluded = true;
	    } else {
		methodValue.authConstrained = false;
	    }
	}
    }

    void setConnectOnMethods(String guarantee, BitSet methodSet)
    {
	int b = MethodValue.connectTypeNone;
	if (guarantee != null) {
	    Integer bit = (Integer) 
		MethodValue.connectHash.get(guarantee);
	    if (bit == null) 
		throw new IllegalArgumentException
		    ("constraint translation error-illegal trx guarantee");
	    b = bit.intValue();
	}

	for (int i = methodSet.nextSetBit(0); i >= 0; 
	     i = methodSet.nextSetBit(i+1)) {

	    MethodValue methodValue = getMethodValue(i);

	    methodValue.connectSet |= b;
	}
    }

    BitSet getExcludedMethods()
    {
	BitSet methodSet = new BitSet();

	synchronized(methodValues) {

	    Collection<MethodValue> values = methodValues.values();
	    Iterator it = values.iterator();

	    while (it.hasNext()) {
		MethodValue v = (MethodValue) it.next();
		if (v.excluded) {
		    methodSet.set(v.index);
		}
	    }
	}
	return methodSet;
    }

    BitSet getAuthConstrainedMethods()
    {
	BitSet methodSet = new BitSet();

	synchronized(methodValues) {

	    Collection<MethodValue> values = methodValues.values();
	    Iterator it = values.iterator();

	    while (it.hasNext()) {
		MethodValue v = (MethodValue) it.next();
		if (v.excluded || v.authConstrained || !v.roleList.isEmpty()) {
		    methodSet.set(v.index);
		}
	    }
	}
	return methodSet;
    }

    static boolean bitIsSet(int map , int bit) {
        return (map & bit) == bit ? true : false;
    }

    BitSet getTransportConstrainedMethods()
    {
	BitSet methodSet = new BitSet();

	synchronized(methodValues) {

	    Collection<MethodValue> values = methodValues.values();
	    Iterator it = values.iterator();

	    while (it.hasNext()) {
		MethodValue v = (MethodValue) it.next();
		if (v.excluded || 
		    !bitIsSet(v.connectSet,MethodValue.connectTypeNone)) {
		    methodSet.set(v.index);
		}
	    }
	}
	return methodSet;
    }

    HashMap getRoleMap() 
    {
	HashMap roleMap = new HashMap();

	synchronized(methodValues) {

	    Collection<MethodValue> values = methodValues.values();

	    Iterator it = values.iterator();
	    while (it.hasNext()) {

		MethodValue v = (MethodValue) it.next();
		
		if (!v.excluded && v.authConstrained) {

		    Iterator rit = v.roleList.iterator();
		    while(rit.hasNext()) {

			String role = (String) rit.next();
			BitSet methodSet = (BitSet) roleMap.get(role);

			if (methodSet == null) {
			    methodSet = new BitSet();
			    roleMap.put(role,methodSet);
			}

			methodSet.set(v.index);
		    }
		}
	    }
	}

	return roleMap;
    }

    BitSet getConnectMap(int cType) 
    {
	BitSet methodSet = new BitSet();

	synchronized(methodValues) {

	    Collection<MethodValue> values = methodValues.values();
	    Iterator it = values.iterator();

	    while (it.hasNext()) {

		MethodValue v = (MethodValue) it.next();

		if (!v.excluded) {
		    if (v.connectSet == 0) {
			v.connectSet = MethodValue.connectTypeNone;
		    }
		    if (bitIsSet(v.connectSet,cType)) {
			methodSet.set(v.index);
		    }
		}
	    }
	}

	return methodSet;
    }

}




