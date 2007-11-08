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
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.InheritanceType.*;

/**
 * This tests;
 * <ul>
 * <li> the init problem
 * <li> class name indicator usage
 * <li> concreate root class
 * <li> big int as primary key
 */
@Entity
@Table(name="CMP3_PERSON")
@Inheritance(strategy=JOINED)
@DiscriminatorValue("P")
public class Person implements Serializable {
    public Number id;
    public String name;
    public Car car;
    public Engineer bestFriend;
    public Lawyer representitive;

	@Id
    @GeneratedValue(strategy=TABLE, generator="PERSON_TABLE_GENERATOR")
	@TableGenerator(
        name="PERSON_TABLE_GENERATOR", 
        table="CMP3_INHERITANCE_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="PERSON_SEQ"
    )
    @Column(name="ID")
    public Number getId() {
        return id;
    }

	public void setId(Number id) { 
        this.id = id; 
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY, optional=false)
	@JoinColumn(name="CAR_ID")
    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

	@Column(name="NAME", length=80)
    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY, optional=false)
	@JoinColumn(name="FRIEND_ID")
    public Engineer getBestFriend() {
        return bestFriend;
    }

    public void setBestFriend(Engineer friend) {
        bestFriend = friend;
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY, optional=false)
	@JoinColumn(name="REP_ID", referencedColumnName="ID")
    public Lawyer getRepresentitive() {
        return representitive;
    }

    public void setRepresentitive(Lawyer representitive) {
        this.representitive = representitive;
    }

    public String toString() {
        return this.name;
    }
}