/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Mix-in interface that provides fields to track created and updated information.
 * There is no need to write these fields, or if written, they will be overwritten
 * upon transaction commit
 * See {@link AuditInterceptor} for hooking it up into HK2 config system, i.e.
 */
public interface Auditable {
  @XmlAttribute
  void setCreatedOn(String date);
  String getCreatedOn();
  

  @XmlAttribute
  void setUpdatedOn(String date);
  String getUpdatedOn();
  
  /*
  @DuckTyped
  Date getCreatedOnDate();

  @DuckTyped
  Date getUpdatedOnDate();
  */

  /*
  class Duck {
    public static Date getCreatedOnDate(final Auditable auditable) {
      return date(auditable.getCreatedOn());
    }

    public static Date getUpdatedOnDate(final Auditable auditable) {
      return date(auditable.getUpdatedOn());
    }

    private static Date date(String dateString) {
      if (dateString != null) {
        try {
          // OWLS-13546: SimpleDateFormat is not thread safe - use a new instance each time
          DateFormat dateFormat = new SimpleDateFormat(AuditInterceptor.ISO_DATE_FORMAT);
          return dateFormat.parse(dateString);
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      }
      return null;
    }

  }
  */
}
