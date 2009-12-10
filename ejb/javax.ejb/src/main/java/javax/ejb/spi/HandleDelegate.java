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

package javax.ejb.spi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;


/**
 * The HandleDelegate interface is implemented by the EJB container. 
 * It is used by portable implementations of javax.ejb.Handle and
 * javax.ejb.HomeHandle.
 * It is not used by EJB components or by client components.
 * It provides methods to serialize and deserialize EJBObject and
 * EJBHome references to streams.
 *
 * <p> The HandleDelegate object is obtained by JNDI lookup at the
 * reserved name "java:comp/HandleDelegate".
 */

public interface HandleDelegate {
    /**
     * Serialize the EJBObject reference corresponding to a Handle.
     *
     * <p> This method is called from the writeObject method of 
     * portable Handle implementation classes. The ostream object is the
     * same object that was passed in to the Handle class's writeObject.
     *
     * @param ejbObject The EJBObject reference to be serialized.
     *
     * @param ostream The output stream.
     *
     * @exception IOException The EJBObject could not be serialized
     *    because of a system-level failure.
     */
    public void writeEJBObject(EJBObject ejbObject, ObjectOutputStream ostream)
	throws IOException;


    /**
     * Deserialize the EJBObject reference corresponding to a Handle.
     *
     * <p> readEJBObject is called from the readObject method of 
     * portable Handle implementation classes. The istream object is the
     * same object that was passed in to the Handle class's readObject.
     * When readEJBObject is called, istream must point to the location
     * in the stream at which the EJBObject reference can be read.
     * The container must ensure that the EJBObject reference is 
     * capable of performing invocations immediately after deserialization.
     *
     * @param istream The input stream.
     *
     * @return The deserialized EJBObject reference.
     *
     * @exception IOException The EJBObject could not be deserialized
     *    because of a system-level failure.
     * @exception ClassNotFoundException The EJBObject could not be deserialized
     *    because some class could not be found.
     */
    public javax.ejb.EJBObject readEJBObject(ObjectInputStream istream)
	throws IOException, ClassNotFoundException;

    /**
     * Serialize the EJBHome reference corresponding to a HomeHandle.
     *
     * <p> This method is called from the writeObject method of 
     * portable HomeHandle implementation classes. The ostream object is the
     * same object that was passed in to the Handle class's writeObject.
     *
     * @param ejbHome The EJBHome reference to be serialized.
     *
     * @param ostream The output stream.
     *
     * @exception IOException The EJBObject could not be serialized
     *    because of a system-level failure.
     */
    public void writeEJBHome(EJBHome ejbHome, ObjectOutputStream ostream)
	throws IOException;

    /**
     * Deserialize the EJBHome reference corresponding to a HomeHandle.
     *
     * <p> readEJBHome is called from the readObject method of 
     * portable HomeHandle implementation classes. The istream object is the
     * same object that was passed in to the HomeHandle class's readObject.
     * When readEJBHome is called, istream must point to the location
     * in the stream at which the EJBHome reference can be read.
     * The container must ensure that the EJBHome reference is 
     * capable of performing invocations immediately after deserialization.
     *
     * @param istream The input stream.
     *
     * @return The deserialized EJBHome reference.
     *
     * @exception IOException The EJBHome could not be deserialized
     *    because of a system-level failure.
     * @exception ClassNotFoundException The EJBHome could not be deserialized
     *    because some class could not be found.
     */
    public javax.ejb.EJBHome readEJBHome(ObjectInputStream istream)
	throws IOException, ClassNotFoundException;
}
