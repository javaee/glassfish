/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */



package com.sun.persistence.api.deployment;

/**
 * The persistence-api spec allows many descriptors (both in annotations as well
 * as XML DD) to be left unspecified. There are well defined rules to set
 * default values for them. e.g. if entity name is not set, the default value is
 * "unqualified name of the Java class". There are two kinds of models, viz: a)
 * logical model -- this models the object model of the entities and their
 * relationship. b) physical model -- this specifies the schema for the physical
 * data store which the logical model is mapped to. The persistence-api spec is
 * very carefully designed to separate these two models allowing non-relational
 * store to be used as well for a given logical model. This interface exposes
 * two such methods to supply default values the two models.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface DefaultValueSupplier {
    /**
     * This method is responsible for setting default values for the logical
     * model.
     *
     * @param du
     * @throws DeploymentException if it encounters errors during processing
     * @see #populateRelationalModelDefaultValues(DeploymentUnit)
     */
    void populateLogicalModelDefaultValues(DeploymentUnit du)
            throws DeploymentException;

    /**
     * The spec defines rules for mapping logical model to a relational database
     * because that is the most common case. This method is responsible for
     * setting default values for the relational model. Before this method is
     * called, {@link #populateLogicalModelDefaultValues(DeploymentUnit)} must
     * be called.
     *
     * @param du
     * @throws DeploymentException if it encounters errors during processing
     * @see #populateLogicalModelDefaultValues(DeploymentUnit)
     */
    void populateRelationalModelDefaultValues(DeploymentUnit du)
            throws DeploymentException;

}
