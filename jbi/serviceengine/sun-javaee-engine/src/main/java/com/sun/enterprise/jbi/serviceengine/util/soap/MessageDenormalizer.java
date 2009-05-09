/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

// Copyright (c) 2004-2005 Sun Microsystems Inc., All Rights Reserved.

/*
 * MessageDenormalizer.java
 *
 * SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.enterprise.jbi.serviceengine.util.soap;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.NormalizedMessage;


/**
 * This object is used by <code>MessageProcessor</code> implementations to
 * denormalize a JBI NormalizedMessage and convert it into a SOAP Message.
 * The object maps the Normalized Message's message content to SOAP:Body element in the
 * SOAP Message. The SOAP:header elements are extracted from a Normalized Message's
 * message context property "SoapHeader".
 *
 * @author Sun Microsystems, Inc.
 */
public interface MessageDenormalizer
{
    /**
     * Converts a normalized message to a SOAP Message.
     *
     * @param normalizedMessage message to be denormalized.
     * @param operation operation invoked
     * @param isResponse indicates if a response messages needs to be generated
     *
     * @return the SOAP Message.
     */
    SOAPWrapper denormalizeMessage(
        NormalizedMessage normalizedMessage, Operation operation, boolean isResponse);

    /**
     * Converts a fault mesage to a SOAP Message using the specified fault code.
     *
     * @param faultMessage fault message.
     *
     * @return a new SOAPWrapper instance which contains the SOAP fault Message.
     */
    SOAPWrapper denormalizeFaultMessage(Fault faultMessage);

    /**
     * Converts an exception to a SOAP Message. It uses the Server fault code in the soap
     * namespace.
     *
     * @param exception exception instance
     *
     * @return denormalized exception instance.
     */
    SOAPWrapper denormalizeMessage(Exception exception);

    /**
     * Converts an exception to a SOAP Message. It uses the faultCode passed. The code
     * expects the faultcode passed to be part of the soap namespace.
     *
     * @param exception exception instance
     * @param faultCode fault code
     *
     * @return denormalized exception instance.
     */
    SOAPWrapper denormalizeMessage(Exception exception, String faultCode);
}
