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

package com.sun.enterprise.util.collection;

public class IntSet {
    int[] members = new int[6];
    int size = 0;
    	
    public IntSet() {
    }
    	
	public boolean add(int value) {
		for (int i=0; i<size; i++) {
			if (members[i] == value) {
				return false;
			}
		}
			
		if (size == members.length) {
			int[] temp = new int[size + 4];
			System.arraycopy(members, 0, temp, 0, size);
			members = temp;
		}
		members[size++] = value;
		return true;
	}
	
	public boolean remove(int value) {
		for (int i=0; i<size; i++) {
			if (members[i] == value) {
				for (int j=i+1; j<size; j++) {
					members[j-1] = members[j];
				}
				return true;
			}
		}
		return false;
	}
			
	public boolean contains(int value) {
		for (int i=0; i<size; i++) {
			if (members[i] == value) {
				return true;
			}
		}
		return false;
	}
			
	
		
	public int size() {
		return size;
	}
		
	public int[] getMembers() {
		return members;
	}
    	
}
    
