/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package util;

public class LeagueDetails implements java.io.Serializable {

    private String id;
    private String name;
    private String sport;

    public LeagueDetails (String id, String name, String sport) {

        this.id = id;
        this.name = name;
        this.sport = sport;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSport() {
        return sport;
    }

    public String toString() {
        String s = id + " " + name + " " + sport;
        return s;
    }

} // LeagueDetails
