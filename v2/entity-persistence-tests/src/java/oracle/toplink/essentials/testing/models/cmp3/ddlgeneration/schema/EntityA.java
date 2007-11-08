/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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

package oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.schema;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.TABLE;
import java.util.*;

@Entity
@Table(name="CMP3_DDL_ENTITYA", schema="MYSCHEMA")
@NamedQuery(name = "EntityA.findById", query = "SELECT a FROM EntityA a WHERE a.id = ?1")    

public class EntityA implements Serializable {
    
    private Integer id;
    private String name;
    private Set<EntityB> entityBs;
    
    public EntityA() {       
        entityBs = new HashSet<oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.schema.EntityB>();
    }

    @Id
    @GeneratedValue(strategy=TABLE, generator="DDL_ENTITYA_TABLE_GENERATOR")
    @TableGenerator(
        name="DDL_ENTITYA_TABLE_GENERATOR",
        table="CMP3_DDL_ENTITYA_SEQ",
        pkColumnName="SEQ_NAME",
        valueColumnName="SEQ_COUNT",
        pkColumnValue="DDL_ENTITYA_SEQ"
    )
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @ManyToMany(mappedBy="entityAs")
    public Set<oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.schema.EntityB> getEntityBs() {
        return entityBs;
    }
    public void setEntityBs(Set<oracle.toplink.essentials.testing.models.cmp3.ddlgeneration.schema.EntityB> entityBs) {
        this.entityBs = entityBs;
    }
}
