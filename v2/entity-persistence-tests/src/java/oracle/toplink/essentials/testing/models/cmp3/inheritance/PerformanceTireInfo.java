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


package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import java.io.*;
import oracle.toplink.essentials.tools.schemaframework.*;
import javax.persistence.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.InheritanceType.*;

@Entity
@Table(name="CMP3_TIRE")
@DiscriminatorValue("Performance")
public class PerformanceTireInfo extends TireInfo implements Serializable {
    protected Integer speedrating;

    public PerformanceTireInfo() {}

	@Column(name="SPEEDRATING")
    public Integer getSpeedRating() {
        return this.speedrating;
    }

    public void setSpeedRating(Integer rating) {
        this.speedrating = rating;
    }

}