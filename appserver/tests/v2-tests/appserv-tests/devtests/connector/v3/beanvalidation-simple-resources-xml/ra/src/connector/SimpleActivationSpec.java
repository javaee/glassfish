/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.validation.constraints.*;

/**
 * This class is supposed to implemented as a JavaBean
 * so that the app server can instantiate and configure
 * it at the runtime.
 *
 * @author	Qingqing Ouyang
 */
public class SimpleActivationSpec 
    implements ActivationSpec, java.io.Serializable {

    private String destinationName;
    private String destinationType;
    private String testProp;
    private Integer testIntegerProp;
    private ResourceAdapter resourceadapter;

    /**
     * Default constructor.
     */
    public SimpleActivationSpec () {}

    public String getDestinationName () {
        return this.destinationName;
    }
    
    public void setDestinationName (String name) {
        debug("setDestinationName() called... name = " + name);
        this.destinationName = name;
    }

    public String getDestinationType() {
        return this.destinationType;
    }
    
    public void setDestinationType (String type) {
        debug("setDestinationType () called... type = " + type);
        this.destinationType= type;
    }

    @NotNull 
    public String getTestProp() {
        return this.testProp;
    }
    
    public void setTestProp (String testProp) {
        debug("setTestProp () called... testProp = " + testProp);
        this.testProp = testProp;
    }

    @Min(value=2)
    @Max(value=2)
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
        throw new RuntimeException("Invalid Configuration testIntegerProp: " +
                this.testIntegerProp.intValue() +
                " testProp " + this.testProp + ", Not caught via bean-validation annotations");

      }

   } 

    private void debug (String message)
    {
        System.out.println("[SimpleActivationSpec] ==> " + message);
    }
}
