/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorFilter;

/**
 * The implementation of the descriptor itself, with the
 * bonus of being serializable
 * 
 * @author jwells
 *
 * TODO:  Should this implementation class be put into a public package?
 * I (JRW) think yes, it should
 */
public class DescriptorImpl implements DescriptorFilter, Serializable {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 77937523212000548L;
	
	private final Set<String> contractsAndImplementations = new HashSet<String>();
	private Set<String> contracts;
	private Set<String> implementations;
	private Set<String> names;
	private Set<String> scopes;
	private Map<String, List<String>> metadatas;
	private Set<String> qualifiers;
	private Long id;
	
	/**
	 * For serialization
	 */
	public DescriptorImpl() {	
	}
	
	public DescriptorImpl(Descriptor copyMe) {
		contracts = copyMe.getContracts();
		names = copyMe.getNames();
		scopes = copyMe.getScopes();
		implementations = copyMe.getImplementations();
		qualifiers = copyMe.getQualifiers();
		metadatas = copyMe.getMetadata();
		id = copyMe.getServiceId();
	}
	
	/**
	 * This is a deep copy, as the sets are taken directly into
	 * this object
	 * 
	 * @param contracts
	 * @param names
	 * @param scopes
	 * @param implementations
	 * @param metadatas
	 * @param qualifiers
	 * @param id
	 */
	public DescriptorImpl(Set<String> contracts,
			Set<String> names,
			Set<String> scopes,
			Set<String> implementations,
			Map<String, List<String>> metadatas,
			Set<String> qualifiers,
			Long id) {
		this.contracts = new HashSet<String>(contracts);
		contractsAndImplementations.addAll(contracts);
		
		this.implementations = new HashSet<String>(implementations);
		contractsAndImplementations.addAll(implementations);
		
		this.names = new HashSet<String>(names);
		this.scopes = new HashSet<String>(scopes);
		this.metadatas = new HashMap<String, List<String>>(metadatas);
		this.qualifiers = new HashSet<String>(qualifiers);
		this.id = id;
	}

	@Override
	public Set<String> getContracts() {
		return new HashSet<String>(contracts);
	}

	@Override
	public Set<String> getImplementations() {
		return new HashSet<String>(implementations);
	}

	@Override
	public Set<String> getScopes() {
		return new HashSet<String>(scopes);
	}

	@Override
	public Set<String> getNames() {
		return new HashSet<String>(names);
	}

	@Override
	public Set<String> getQualifiers() {
		return new HashSet<String>(qualifiers);
	}

	@Override
	public Map<String, List<String>> getMetadata() {
		return new HashMap<String, List<String>>(metadatas);
	}
	
	@Override
	public Long getServiceId() {
		return id;
	}
	
	@Override
	public boolean matches(Descriptor d) {
	  HashSet<String> dCandI = new HashSet<String>();
	  dCandI.addAll(d.getImplementations());
	  dCandI.addAll(d.getContracts());
	  
	  if (!dCandI.containsAll(contractsAndImplementations)) return false;
		
		if (!d.getNames().containsAll(names)) return false;
		
		if (!d.getQualifiers().containsAll(qualifiers)) return false;
		
		Set<String> dKeys = d.getMetadata().keySet();
		if (!dKeys.containsAll(metadatas.keySet())) return false;
		
		for (String dKey : dKeys) {
			List<String> dValues = d.getMetadata().get(dKey);
			
			if (!dValues.containsAll(metadatas.get(dKey))) return false;
		}
		
		return true;
	}
	
	private static String writeSet(Set<?> set) {
		StringBuffer sb = new StringBuffer("{");
		
		boolean first = true;
		for (Object writeMe : set) {
			if (first) {
				first = false;
				sb.append(writeMe.toString());
			}
			else {
				sb.append("," + writeMe.toString());
			}
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	private static String writeList(List<String> list) {
		StringBuffer sb = new StringBuffer("[");
		
		boolean first = true;
		for (String writeMe : list) {
			if (first) {
				first = false;
				sb.append(writeMe.toString());
			}
			else {
				sb.append("," + writeMe.toString());
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	private static String writeMetadata(Map<String, List<String>> metadata) {
		StringBuffer sb = new StringBuffer("{");
		
		boolean first = true;
		for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
			if (first) {
				first = false;
			    sb.append(entry.getKey() + "=");
			}
			else {
				sb.append("," + entry.getKey() + "=");
			}
			
			sb.append(writeList(entry.getValue()));
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("DescriptorImpl(");
		
		sb.append("\n\timplementations=");
		sb.append(writeSet(implementations));
		
		sb.append("\n\tcontracts=");
		sb.append(writeSet(contracts));
		
		sb.append("\n\tscopes=");
		sb.append(writeSet(scopes));
		
		sb.append("\n\tqualifiers=");
		sb.append(writeSet(qualifiers));
		
		sb.append("\n\tmetadata=");
		sb.append(writeMetadata(metadatas));
		
		sb.append("\n\tid=" + id);
		
		sb.append(")");
		
		return sb.toString();
	}
}
