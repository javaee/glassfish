/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package util;

public class PlayerDetails implements java.io.Serializable {

    private String id;
    private String name;
    private String position;
    private double salary;

    public PlayerDetails (String id, String name, String position, 
        double salary) {

        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public double getSalary() {
        return salary;
    }

    public String toString() {
        String s = id + " " + name + " " +  position + " " + salary;
        return s;
    }


} // PlayerDetails
