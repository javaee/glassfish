/*
 * EmploymentPeriod.java
 *
 * Created on February 23, 2005, 8:22 PM
 */

package com.sun.s1asdev.ejb.ejb30.hello.session;

import java.io.*;
import java.util.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;
/**
 *
 * @author ss141213
 */
@Embeddable
public class EmploymentPeriod implements Serializable {
	private Date start; 
	private Date end; 
	@Basic
	public Date getStartDate() { return start; } 
	public void setStartDate(Date start) { this.start = start; } 
	@Basic
	public Date getEndDate() { return end; } 
	public void setEndDate(Date end) { this.end = end; } 
}
