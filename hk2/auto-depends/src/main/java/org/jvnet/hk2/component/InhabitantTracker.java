/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.component;

import java.util.Collection;

/**
 * Tracks an inhabitant, or set of inhabitants.
 * 
 * @see Habitat#track(InhabitantTrackerContext, Callback)
 * @see Habitat#trackFuture(InhabitantTrackerContext, Boolean)
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public interface InhabitantTracker {

  /**
   * Returns a single inhabitant.  If multiple inhabitants qualify,
   * the one selected is based upon the implementation.
   * 
   * @return an inhabitant, or null if no inhabitants match tracker criteria
   */
  public <T> Inhabitant<T> getInhabitant() throws ComponentException;

  /**
   * Returns the set of inhabitants qualifying.
   * 
   * @return the collection of inhabitants matching tracker criteria
   */
  public Collection<Inhabitant<?>> getInhabitants() throws ComponentException;

  /**
   * Releases / closes this tracker. This MUST be called for performance reasons,
   * to cleanup resources.
   */
  public void release();


  /**
   * The callback is called when there is an event changing one of the tracked
   * inhabitants.  The callback may occur on a different thread than the one
   * that originated the change in the habitat.
   */
  public static interface Callback {
    /**
     * Called when there is a modification to the set of inhabitants of some kind.
     * 
     * @param t the tracker
     * @param h the habitat
     * @param initial
     *    true if the updated even happens during initial tracker creation taking
     *    inventory of starting matches
     */
    public void updated(InhabitantTracker t, Habitat h, boolean initial);
  }

}
