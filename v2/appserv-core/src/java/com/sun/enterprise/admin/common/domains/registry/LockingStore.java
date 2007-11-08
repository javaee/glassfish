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

package com.sun.enterprise.admin.common.domains.registry;

import java.io.IOException;


import java.io.Serializable;


/**
 * Implementors of this interface represent a store which can be
 * locked and which will store and retrieve a single data object
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version 1.0
 */
public interface LockingStore
{

	  /**
		 Write the given object to the store via serialization.
		 <p>
		 Precondition - store is locked
		 <p>
		 Postcondition - store contains this object, and only this
		 object. 
		 @param o the object to be put into the store. Must implement
		 {@link Serializable}
		 @throws IOException if there was a problem writing to the
		 store. Store will no longer be locked.
		 @throws IllegalStateException if the store is not
		 locked. Store will no longer be locked.
	  */
  void writeObject(Object o) throws IOException, IllegalStateException;
	  /**
		 Read the object from the store.
		 <p>
		 precondition - true
		 <p>
		 postcondition - store has not been modified
		 @return the single object that was in the store (or null if
		 the store is empty)
		 @throws TimeoutException if a lock on the store couldn't be
		 obtained in a reasonable time.
		 @throws IOException if there was a problem reading from the
		 store
		 @throws ClassNotFoundException if the object could not be
		 restored from the store
	  */
  Object readObject() throws IOException, TimeoutException, ClassNotFoundException;
	  /**
		 lock the store.
		 <p>
		 precondition - true
		 <p>
		 postcondition - the store is locked
		 @throws IOException if there was a problem initializing the
		 store
		 @throws TimeoutException if there was a problem obtaining a
		 lock in a reasonable amount of time
	  */
  void lock() throws IOException, TimeoutException;
	  /**
		 unlock the store.
		 <p>
		 precondition - store is not locked, or the store has been
		 locked by the caller.
		 <p>
		 postcondition - store is closed and all resources released
	  */
  void unlock();
	  /**
		 get the last time this store was modified
		 @return the time that this store was last modified, as per
		 {@link java.io.File#lastModified()}
	  */
  long lastModified();
}
