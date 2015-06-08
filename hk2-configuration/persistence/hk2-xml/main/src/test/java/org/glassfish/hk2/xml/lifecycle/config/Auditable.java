/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.hk2.xml.lifecycle.config;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;

import org.jvnet.hk2.annotations.Contract;

/**
 * Mix-in interface that provides fields to track created and updated information.
 * There is no need to write these fields, or if written, they will be overwritten
 * upon transaction commit
 * See {@link AuditInterceptor} for hooking it up into HK2 config system, i.e.
 */
@Contract
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
