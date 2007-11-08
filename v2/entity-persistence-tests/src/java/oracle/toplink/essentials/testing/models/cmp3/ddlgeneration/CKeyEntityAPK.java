/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  

package oracle.toplink.essentials.testing.models.cmp3.ddlgeneration;

/**
 * @author Wonseok Kim
 */
public class CKeyEntityAPK {
    public int seq;
    public String firstName;
    public String lastName;


    public CKeyEntityAPK() {
    }

    public CKeyEntityAPK(int seq, String firstName, String lastName) {
        this.seq = seq;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CKeyEntityAPK that = (CKeyEntityAPK) o;

        return seq == that.seq && firstName.equals(that.firstName) && lastName.equals(that.lastName);
    }

    public int hashCode() {
        int result;
        result = seq;
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        return result;
    }
}
