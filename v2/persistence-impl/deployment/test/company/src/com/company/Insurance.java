/*
 *
 * Created on June 28, 2005, 12:21 PM
 */

package com.company;

import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;

/**
 *
 * @author Markus Fuchs
 */
@Entity
public class Insurance {
    private long insid;
    private String carrier;
    private Employee employee;
    private char discriminator;

    @Id public long getInsid() {
        return insid;
    }
    public void setInsid(long insid) {
        this.insid = insid;
    }

    @Basic public String getCarrier() {
        return carrier;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    @Basic public char getDiscriminator() {
        return discriminator;
    }
    public void setDiscriminator(char discriminator) {
        this.discriminator = discriminator;
    }

    @OneToOne()
    @JoinColumn(name="PERSONID", referencedColumnName="PERSONID", nullable=true)
    public Employee getEmployee() {
        return employee;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
