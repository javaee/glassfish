/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.Activation;
import javax.resource.spi.ConfigProperty;

/**
 * This class is supposed to implemented as a JavaBean
 * so that the app server can instantiate and configure
 * it at the runtime.
 *
 * @author	Qingqing Ouyang
 */
@Activation(
        messageListeners = {connector.MyMessageListener_1.class, connector.MyMessageListener.class}
)
public class MyActivationSpec_1 implements javax.resource.spi.ActivationSpec
    {

    private String destinationName;
    private String destinationType;
    private String testProp;
    private Integer testIntegerProp;
    private ResourceAdapter resourceadapter;
     

    public String getDestinationName () {
        return this.destinationName;
    }

    @ConfigProperty()
    public void setDestinationName (String name) {
        debug("setDestinationName() called... name = " + name);
        this.destinationName = name;
    }

    public String getDestinationType() {
        return this.destinationType;
    }

    //@README : this config property should be present only for MyMessageListener_1
        // and not MyMessageListener as MyMessageListener is already defined in ra.xml with
        // different activation spec class name
    @ConfigProperty(defaultValue="destinationType")
    public void setDestinationType (String type) {
        debug("setDestinationType () called... type = " + type);
        this.destinationType= type;
    }

    public String getTestProp() {
        return this.testProp;
    }

    public void setTestProp (String testProp) {
        debug("setTestProp () called... testProp = " + testProp);
        this.testProp = testProp;
    }

    public Integer getTestIntegerProp() {
        return this.testIntegerProp;
    }

    public void setTestIntegerProp (Integer testProp1) {
        debug("setTestIntegerProp () called... testIntegerProp = " + testProp1);
        this.testIntegerProp = testProp1;
    }

    public ResourceAdapter getResourceAdapter () {
        debug("getResourceAdapter() called... ");
        return this.resourceadapter;
    }

    public void setResourceAdapter (ResourceAdapter ra) {
        debug("setResourceAdapter() called... ra = " + ra);
        this.resourceadapter = ra;
    }

    public void validate() {
      if (this.testIntegerProp.intValue() == 2 && this.testProp.equals("WrongValue")) {
        //valid
      } else {
        throw new RuntimeException("Invalid Configuration testIntegerProp: " + this.testIntegerProp.intValue() + " testProp " + this.testProp);

      }

   }

    private void debug (String message)
    {
        System.out.println("[SimpleActivationSpec] ==> " + message);
    }
}
