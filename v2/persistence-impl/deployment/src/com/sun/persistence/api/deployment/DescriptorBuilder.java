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

import com.sun.persistence.spi.deployment.Archive;

import java.io.IOException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface DescriptorBuilder extends DefaultValueSupplier {

    /**
     * This method is responsible for reading XML DD (if any), reading
     * annotations (if any) and merging them if required.
     *
     * @param archive          which contains both the XML DD as well as the
     *                         annotated classes
     * @param populateDefaults to indicate whether default values should be
     *                         populated or not.
     * @return a deployment unit representing this archive. if there is no XML
     *         DD, it creates an empty deployment unit.
     */
    DeploymentUnit readXMLAndAnnotations(
            Archive archive,
            boolean populateDefaults)
            throws DeploymentException, IOException;

    /**
     * @param archive which contains the XML DD.
     * @return a deployment unit representing this exploded dir. If there is no
     *         XML DD, it creates an empty deployment unit.
     * @see XMLReader
     */
    DeploymentUnit readXML(Archive archive) throws IOException,
            DeploymentException;

    /**
     * @param archive which will be used to scan for annotations.
     * @param du      the DeploymentUnit to be populated with values read from
     *                annotations. du can be null, in which case, it creates a
     *                new DeploymentUnit.
     * @return the DeploymentUnit which is populated with annotation information
     *         returns a new DeploymentUnit if null is passed in the parameter,
     *         else it returns the same DeploymentUnit.
     * @throws DeploymentException if errors were encountered during
     *                             processing.
     */
    DeploymentUnit readAnnotations(Archive archive, DeploymentUnit du)
            throws DeploymentException, IOException;

}
