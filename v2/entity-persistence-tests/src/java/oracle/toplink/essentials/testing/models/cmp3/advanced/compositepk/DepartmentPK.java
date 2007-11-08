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

package oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk;

public class DepartmentPK {
    public String name;
    public String role;
    public String location;

    public DepartmentPK(String name, String role, String location) {
        this.name = name;
        this.role = role;
        this.location = location;
    }

    public boolean equals(Object other) {
        if (other instanceof DepartmentPK) {
            final DepartmentPK otherDepartmentPK = (DepartmentPK) other;
            return (otherDepartmentPK.name.equals(name) && otherDepartmentPK.role.equals(role) && otherDepartmentPK.location.equals(location));
        }
        
        return false;
    }
}
