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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

/**
 * INTERNAL:
 * 
 * Static values for metadata processing.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataConstants {
    /* Relationship mappings. */
    public static final String LAZY = "LAZY";
    public static final String EAGER = "EAGER";
    
    /* Inheritance constants. */
    public static final String SINGLE_TABLE = "SINGLE_TABLE";
    public static final String TABLE_PER_CLASS = "TABLE_PER_CLASS";
    
    /* Discriminator column type constants. */
    public static final String CHAR = "CHAR";
    public static final String STRING = "STRING";
    public static final String INTEGER = "INTEGER";
    
    /* Order by constants. */
    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";
    
    /* Temporal field classification constants. */
    public static final String DATE = "DATE";
    public static final String TIME = "TIME";
    public static final String TIMESTAMP = "TIMESTAMP";
    
    /* XML cascade type constants. */
	public static final String CASCADE_ALL = "cascade-all";
    public static final String CASCADE_MERGE = "cascade-merge";
    public static final String CASCADE_REMOVE = "cascade-remove";
    public static final String CASCADE_PERSIST = "cascade-persist";
    public static final String CASCADE_REFRESH = "cascade-refresh";

    /* Sequencing constants. */
    public static final String AUTO = "AUTO";
    public static final String TABLE = "TABLE";
    public static final String IDENTITY = "IDENTITY";
    public static final String SEQUENCE = "SEQUENCE";
    public static final String DEFAULT_AUTO_GENERATOR = "SEQ_GEN";
    public static final String DEFAULT_TABLE_GENERATOR = "SEQ_GEN_TABLE";
    public static final String DEFAULT_SEQUENCE_GENERATOR = "SEQ_GEN_SEQUENCE";
    public static final String DEFAULT_IDENTITY_GENERATOR = "SEQ_GEN_IDENTITY";
}
