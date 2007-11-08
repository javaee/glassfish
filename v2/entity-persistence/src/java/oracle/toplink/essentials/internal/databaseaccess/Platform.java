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
package oracle.toplink.essentials.internal.databaseaccess;

import java.io.*;
import java.util.Map;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.sequencing.Sequence;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.sessions.DatabaseSession;

/**
 * Platform is private to TopLink. It encapsulates behavior specific to a datasource platform
 * (eg. Oracle, Sybase, DB2, Attunity, MQSeries), and provides the interface for TopLink to access this behavior.
 *
 * @see DatasourcePlatform
 * @see DatabasePlatform
 * @see oracle.toplink.essentials.eis.EISPlatform
 * @see oracle.toplink.essentials.xml.XMLPlatform
 * @see oracle.toplink.essentials.sdk.SDKPlatform
 *
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public interface Platform extends Serializable, Cloneable {
    public Object clone();

    /**
     * Convert the object to the appropriate type by invoking the appropriate
     * ConversionManager method
     * @param object - the object that must be converted
     * @param javaClass - the class that the object must be converted to
     * @exception - ConversionException, all exceptions will be thrown as this type.
     * @return - the newly converted object
     */
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException;

    /**
     * Copy the state into the new platform.
     */
    public void copyInto(Platform platform);

    /**
     * The platform hold its own instance of conversion manager to allow customization.
     */
    public ConversionManager getConversionManager();

    /**
     * The platform hold its own instance of conversion manager to allow customization.
     */
    public void setConversionManager(ConversionManager conversionManager);

    /**
     * Return the qualifier for the table. Required by some
     * databases such as Oracle and DB2
     */
    public String getTableQualifier();

    /**
     * Answer the timestamp from the server.
     */
    public java.sql.Timestamp getTimestampFromServer(AbstractSession session, String sessionName);

    /**
     * This method can be overridden by subclasses to return a
     * query that will return the timestamp from the server.
     * return null if the time should be the local time.
     */
    public ValueReadQuery getTimestampQuery();

    public boolean isAccess();

    public boolean isAttunity();

    public boolean isCloudscape();

    public boolean isDerby();

    public boolean isDB2();

    public boolean isDBase();

    public boolean isHSQL();

    public boolean isInformix();

    public boolean isMySQL();

    public boolean isODBC();

    public boolean isOracle();

    public boolean isPointBase();

    public boolean isSQLAnywhere();

    public boolean isSQLServer();

    public boolean isSybase();

    public boolean isTimesTen();

    public boolean isPostgreSQL();
        
    /**
     * Set the qualifier for the table. Required by some
     * databases such as Oracle and DB2
     */
    public void setTableQualifier(String qualifier);

    /**
     * Can override the default query for returning a timestamp from the server.
     * See: getTimestampFromServer
     */
    public void setTimestampQuery(ValueReadQuery tsQuery);

    /**
     * Add the parameter.
     * Convert the parameter to a string and write it.
     */
    public void appendParameter(Call call, Writer writer, Object parameter);

    /**
     * Allow for the platform to handle the representation of parameters specially.
     */
    public Object getCustomModifyValueForCall(Call call, Object value, DatabaseField field, boolean shouldBind);

    /**
     * Allow for the platform to handle the representation of parameters specially.
     */
    public boolean shouldUseCustomModifyForCall(DatabaseField field);

    /**
     * Get default sequence.
     * Sequence name shouldn't be altered -
     * don't do: getDefaultSequence().setName(newName).
     */
    public Sequence getDefaultSequence();

    /**
     * Set default sequence.
     * The sequence should have a unique name
     * that shouldn't be altered after the sequence has been set:
     * don't do: getDefaultSequence().setName(newName)).
     * Default constructors for Sequence subclasses
     * set name to "SEQ".
     */
    public void setDefaultSequence(Sequence sequence);

    /**
     * Add sequence.
     * The sequence should have a unique name
     * that shouldn't be altered after the sequence has been added -
     * don't do: getSequence(name).setName(newName))
     */
    public void addSequence(Sequence sequence);

    /**
     * Get sequence corresponding to the name.
     * The name shouldn't be altered -
     * don't do: getSequence(name).setName(newName)
     */
    public Sequence getSequence(String seqName);

    /**
     * Remove sequence corresponding to the name
     * (the sequence was added through addSequence method)
     */
    public Sequence removeSequence(String seqName);

    /**
     * Remove all sequences that were added throud addSequence method.
     */
    public void removeAllSequences();

    /**
     * INTERNAL:
     * Returns a map of sequence names to Sequences (may be null).
     */
    public Map getSequences();

    /**
     * INTERNAL:
     * Used only for writing into XML or Java.
     */
    public Map getSequencesToWrite();

    /**
     * INTERNAL:
     * Used only for writing into XML or Java.
     */
    public Sequence getDefaultSequenceToWrite();

    /**
     * INTERNAL:
     * Used only for reading from XML.
     */
    public void setSequences(Map sequences);

    /**
     * INTERNAL:
     * Indicates whether defaultSequence is the same as platform default sequence.
     */
    public boolean usesPlatformDefaultSequence();
}
