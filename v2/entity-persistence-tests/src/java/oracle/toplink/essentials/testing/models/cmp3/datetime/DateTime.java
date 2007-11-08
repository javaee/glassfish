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
package oracle.toplink.essentials.testing.models.cmp3.datetime;

import java.io.Serializable;

import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name="CMP3_DATE_TIME")

public class DateTime implements Serializable {
    private Integer id;
    private java.sql.Date date;
    private Time time;
    private Timestamp timestamp;
    private Date utilDate;
    private Calendar calendar;

    public DateTime() {
    }

    public DateTime(java.sql.Date date, Time time, Timestamp timestamp, Date utilDate, Calendar calendar) {
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.utilDate = utilDate;
        this.calendar = calendar;
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="DATETIME_TABLE_GENERATOR")
    @TableGenerator(
        name="DATETIME_TABLE_GENERATOR", 
        table="CMP3_DATETIME_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT"
    )
    @Column(name="DT_ID")
    public Integer getId() { 
        return id; 
    }
    
    public void setId(Integer id) { 
        this.id = id; 
    }

    @Column(name="SQL_DATE")
    public java.sql.Date getDate() { 
        return date; 
    }
    
    public void setDate(java.sql.Date date) { 
        this.date = date; 
    }

    @Column(name="SQL_TIME")
    public Time getTime() { 
        return time; 
    }
    
    public void setTime(Time date) { 
        this.time = date; 
    }

    @Column(name="SQL_TS")
    public Timestamp getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(Timestamp date) { 
        this.timestamp = date; 
    }

    @Column(name="UTIL_DATE")
    @Temporal(TIMESTAMP)
    public Date getUtilDate() { 
        return utilDate; 
    }
    
    public void setUtilDate(Date date) { 
        this.utilDate = date; 
    }

    @Column(name="CAL")
    @Temporal(TIMESTAMP)
    public Calendar getCalendar() { 
        return calendar; 
    }
    
    public void setCalendar(Calendar date) { 
        this.calendar = date; 
    }
}
