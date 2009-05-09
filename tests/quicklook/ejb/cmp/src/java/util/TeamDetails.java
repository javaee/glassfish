/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package util;

public class TeamDetails implements java.io.Serializable {

    private String id;
    private String name;
    private String city;

    public TeamDetails (String id, String name, String city) {

        this.id = id;
        this.name = name;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String toString() {
        String s = id + " " + name + " " + city;
        return s;
    }

} // TeamDetails
