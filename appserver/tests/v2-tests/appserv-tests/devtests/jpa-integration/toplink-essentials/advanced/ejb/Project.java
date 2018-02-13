/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

// Copyright (c) 1998, 2006, Oracle. All rights reserved.  


// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.InheritanceType.*;
import static javax.persistence.FetchType.*;

/**
 * Bean class: ProjectBean
 * Remote interface: Project
 * Primary key class: ProjectPK
 * Home interface: ProjectHome
 *
 * >Employees have a many-to-many relationship with Projects through the
 *  projects attribute.
 * >Projects refer to Employees through the employees attribute.
 */
@Entity
@Table(name="CMP3_PROJECT")
@Inheritance(strategy=JOINED)
@DiscriminatorColumn(name="PROJ_TYPE")
@DiscriminatorValue("P")
@NamedQuery(
	name="findProjectByName",
	query="SELECT OBJECT(project) FROM Project project WHERE project.name = :name"
)
public class Project implements Serializable {
    public int pre_update_count = 0;
    public int post_update_count = 0;
    public int pre_remove_count = 0;
    public int post_remove_count = 0;
    public int pre_persist_count = 0;
    public int post_persist_count = 0;
    public int post_load_count = 0;
    
	private Integer id;
	private int version;
	private String name;
	private String description;
	private Employee teamLeader;
	private Collection<Employee> teamMembers;

	public Project () {
        this.teamMembers = new Vector<Employee>();
	}

	@Id
    @GeneratedValue(strategy=SEQUENCE, generator="PROJECT_SEQUENCE_GENERATOR")
	@SequenceGenerator(name="PROJECT_SEQUENCE_GENERATOR", sequenceName="PROJECT_SEQ", allocationSize=10)
	@Column(name="PROJ_ID")
	public Integer getId() {
        return id; 
    }
    
	public void setId(Integer id) {
        this.id = id; 
    }

	@Version
	@Column(name="VERSION")
	public int getVersion() {
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }

	@Column(name="PROJ_NAME")
	public String getName() { 
        return name; 
    }
    
	public void setName(String name) { 
        this.name = name; 
    }

	@Column(name="DESCRIP")
	public String getDescription() { 
        return description; 
    }
    
	public void setDescription(String description) { 
        this.description = description; 
    }

	@OneToOne
	@JoinColumn(name="LEADER_ID")
	public Employee getTeamLeader() {
        return teamLeader; 
    }
    
	public void setTeamLeader(Employee teamLeader) { 
        this.teamLeader = teamLeader; 
    }

	@ManyToMany(mappedBy="projects")
	public Collection<Employee> getTeamMembers() { 
        return teamMembers; 
    }
    
	public void setTeamMembers(Collection<Employee> employees) {
		this.teamMembers = employees;
	}

    public void addTeamMember(Employee employee) {
        getTeamMembers().add(employee);
    }

    public void removeTeamMember(Employee employee) {
        getTeamMembers().remove(employee);
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("Project ").append(getId()).append(": ").append(getName()).append(", ").append(getDescription());

        return sbuff.toString();
    }
    
    @PrePersist
	public void prePersist() {
        ++pre_persist_count;
	}

	@PostPersist
	public void postPersist() {
        ++post_persist_count;
	}

	@PreRemove
	public void preRemove() {
        ++pre_remove_count;
	}

	@PostRemove
	public void postRemove() {
        ++post_remove_count;
	}

	@PreUpdate
	public void preUpdate() {
        ++pre_update_count;
	}

	@PostUpdate
	public void postUpdate() {
        ++post_update_count;
	}

	@PostLoad
	public void postLoad() {
        ++post_load_count;
	}
}
