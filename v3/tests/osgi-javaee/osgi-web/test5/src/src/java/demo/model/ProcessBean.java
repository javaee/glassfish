/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/*
 * $Id: ProcessBean.java,v 1.2 2005/12/14 22:27:22 rlubke Exp $
 */

package demo.model;

import java.util.Random;

public class ProcessBean {

    public ProcessBean() {
    }

    private int percentage = 0;
    private int increment = 10;

    public int getPercentage() {
        if (100 < percentage) {
            percentage = 0 - increment;
        }
        return percentage += increment;
    }

    /**
     * Getter for property randomPercentage.
     *
     * @return Value of property randomPercentage.
     */
    public int getRandomPercentage() {

        return random.nextInt(101);
    }

    private Random random = new Random();

    /** Holds value of property pollInterval. */
    private int pollInterval = 250;

    /**
     * Getter for property pollInterval.
     *
     * @return Value of property pollInterval.
     */
    public int getPollInterval() {

        return this.pollInterval;
    }

    /**
     * Setter for property pollInterval.
     *
     * @param pollInterval New value of property pollInterval.
     */
    public void setPollInterval(int pollInterval) {

        this.pollInterval = pollInterval;
    }

}
