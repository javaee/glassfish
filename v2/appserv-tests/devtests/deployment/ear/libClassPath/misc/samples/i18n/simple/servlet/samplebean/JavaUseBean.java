/*
 * JavaUseBean.java
 *
 * Created on May 27, 2002, 11:13 AM
 */

package samples.i18n.simple.servlet.samplebean;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Simple Java Bean used to set/get name and greeting messages from a jsp
 * @author  Chand Basha
 * @version	1.0
 */
public class JavaUseBean {

    private String name;
    private String greet;

    /** Default constructor
     */
    public JavaUseBean() {}

    /** Sets the name passed from the jsp file
     */
    public void setName(String name) {
        this.name = name;
    }

	/** Gets the name that was set using setName(String name) method of this class
     */
    public String getName() {
        return name;
    }

    /** Sets the greeting passed from the jsp file
     */
    public void setGreet(String greet) {
        this.greet = greet;
    }

    /** Gets the greeting that was set using setGreet(String greet) method of this class
     */
    public String getGreet() {
        return greet;
    }

    /** Returns a greeting message with the name and message
     */
    public String getGreetString () {
        return "Greetings " +  greet + " " + name;
    }
}

