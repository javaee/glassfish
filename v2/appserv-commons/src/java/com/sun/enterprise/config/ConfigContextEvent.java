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



/**

 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.

 *

 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,

 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.

 * All rights reserved.

 */

package com.sun.enterprise.config;



import javax.management.Notification;



/**

 * event used for notification of changes

 */

public class ConfigContextEvent extends Notification {

   
    public static final String PRE_ACCESS = "PRE_ACCESS";

    public static final String POST_ACCESS = "POST_ACCESS";

    public static final String PRE_ADD_CHANGE = "PRE_ADD_CHANGE";

    public static final String POST_ADD_CHANGE = "POST_ADD_CHANGE";

    public static final String PRE_UPDATE_CHANGE = "PRE_UPDATE_CHANGE";

    public static final String POST_UPDATE_CHANGE = "POST_UPDATE_CHANGE";

    public static final String PRE_DELETE_CHANGE = "PRE_DELETE_CHANGE";

    public static final String POST_DELETE_CHANGE = "POST_DELETE_CHANGE";

    public static final String PRE_SET_CHANGE = "PRE_SET_CHANGE";

    public static final String POST_SET_CHANGE = "POST_SET_CHANGE";

    public static final String PRE_FLUSH_CHANGE = "PRE_FLUSH_CHANGE";

    public static final String POST_FLUSH_CHANGE = "POST_FLUSH_CHANGE";



    private static long eventCounter = 0;

    private String name;

    private Object value;

    private String choice;

    private String beanName;

    /* The classObject will represent the true config bean that is being invoked

     * through the admin GUI.

     */

    private Object classObject;

    //</addition>



    /**

     * Create a new ConfigChangeEvent event.

     * @param ctx ConfigContext which is the source

     */

    public ConfigContextEvent(ConfigContext ctx, String eventType) {

        super(eventType, ctx, ++eventCounter, System.currentTimeMillis());

    }

    

    // <addition> srini@sun.com server.xml verifier

    public ConfigContextEvent(final ConfigContext ctx, 
                              final String eventType, 
                              final String name, 
                              final Object value, 
                              final String choice) {
        this(ctx, eventType, name, value, choice, null);
   }

    public ConfigContextEvent(final ConfigContext ctx, 
                              final String eventType, 
                              final String name, 
                              final Object value, 
                              final String choice,
                              final String beanName) {
        this(ctx,eventType);
        this.name = name;
        this.value = value;
        this.choice = choice;
        this.beanName = beanName;
    }

    

  

    public Object getObject(){

        return value;

    }

    

    public String getName(){

        return name;

    }

    

    public String getChoice(){

        return choice;

    }

    

    public void setBeanName(String beanName){

        this.beanName = beanName;

    }

    

    public String getBeanName() {

        return beanName;

    }

    // </addition> server.xml verifier



    /**

     * Returns ConfigContext which is the source of the event. The difference

     *

     * @see getSource()

     * @return ConfigContext

     */

    public ConfigContext getConfigContext() {

        return (ConfigContext)getSource();

    }



    /**

     * Return a String representation.

     */

    public String toString() {

        return this.getClass().getName() + " -- " 

                + " [Id:" + this.getSequenceNumber()

                + ", ts:" + this.getTimeStamp() + "]";

    }

    

    //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >

    /** The function returns the classObject which is set through the base config

     * bean. The classObject represents the actual config bean that has been

     * invoked through the admin GUI

     *

     * @return Object  

     */

    public Object getClassObject()

    {

        return classObject;

    }

    

    /** The function is called from the ConfigBean class and sets the actual

     * config bean as the classObject. 

     * 

     * @param Object - ConfigBean object representing the actual config bean

     */

    public void setClassObject(Object obj)

    {

        this.classObject = obj;

    }

    //</addition>

            /**
           Return the object which is the target of the validation,
           given the {@link ConfigContextEvent}
           @param cce the {@link ConfigContextEvent} from which the
           target is to be found
           @return the {@link ConfigBean} target
           object which is to be validated.
           @throws {@link ConfigException} if the validation target
           cannot be obtained.
         */
        // The validation target is the class that a *Test class is
        // testing. Its in different places in the ConfigContextEvent
        // for different kinds of event. Where it is is simply a
        // matter of convention, not enforced by any programmatic
        // construct. See
        // appserv-commons/src/java/com/sun/enterprise/config/ConfigBean
        // for what gets loaded where!
    public ConfigBean getValidationTarget() throws ConfigException{
        final String choice = this.getChoice();
        if (choice.equals("VALIDATE")){
            return (ConfigBean) this.getObject();
        } else if (choice.equals("ADD")) {
            return (ConfigBean) this.getObject();
        } else if (choice.equals("DELETE")) {
            return (ConfigBean) this.getObject();
        } else if (choice.equals("UPDATE")) {
            return (ConfigBean) this.getClassObject();
        } else if (choice.equals("SET")) {
            if (null == this.getBeanName()){
                if (this.getName().equals("Description")){
                    throw new ConfigException("Internal error - invalid condition - attempting to validate a SET operation with a \"Description\" object");
                } else {
                    return (ConfigBean) this.getObject();
                }
            } else {
                return (ConfigBean) this.getClassObject();
            }
        } else {
            throw new ConfigException("Internal error - invalid choice received: \""+choice+"\". Only expecting ADD, DELETE, UPDATE or SET");
        }
    }
    
    


}

