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

package com.sun.enterprise.ee.admin.dottedname;

/**
 * Provides Keys for localizable strings defined in a file called LocalStrings.properties.
 *
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Oct 15, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameStrings  {
    private DottedNameStrings(){}

    public final static String	OBJECT_INSTANCE_NOT_FOUND_KEY	= "ObjectInstanceNotFound";
    public final static String	MALFORMED_DOTTED_NAME_KEY		= "MalformedDottedName";
    public final static String	WILDCARD_DISALLOWED_FOR_SET_KEY	= "WildcardDisallowedForSet";
    public final static String	ATTRIBUTE_NOT_FOUND_KEY			= "AttributeNotFound";
    public final static String	ILLEGAL_TO_SET_NULL_KEY			= "IllegalToSetNull";
    public final static String	ILLEGAL_CHARACTER_KEY			= "IllegalCharacter";
    public final static String	MISSING_EXPECTED_NAME_PART_KEY	= "MissingExpectedNamePart";
    public final static String	DOTTED_NAME_MUST_HAVE_ONE_PART_KEY		= "DottedNameMustHaveAtLeastOnePart";
    public final static String	NO_VALUE_NAME_SPECIFIED_KEY		= "NoValueNameSpecified";
    public final static String	RESOURCE_NOT_REFERENCED_BY_CLUSTER_KEY	= "ResourceNotReferencedByCluster";
    public final static String	RESOURCE_NOT_REFERENCED_BY_SERVER_KEY	= "ResourceNotReferencedByServer";
    public final static String	APPLICATION_NOT_REFERENCED_BY_CLUSTER_KEY	= "ApplicationNotReferencedByCluster";
    public final static String	APPLICATION_NOT_REFERENCED_BY_SERVER_KEY	= "ApplicationNotReferencedByServer";
    public final static String	SET_OPERATION_DISALLOWED_FOR_SHARED_RESOURCES_KEY = "SetOperationDisallowedForSharedResources";
    public final static String	SET_OPERATION_DISALLOWED_FOR_SHARED_APPLICATIONS_KEY = "SetOperationDisallowedForSharedApplications";
    public final static String	SET_OPERATION_DISALLOWED_FOR_SHARED_CONFIGS_KEY = "SetOperationDisallowedForSharedConfigs";
}
