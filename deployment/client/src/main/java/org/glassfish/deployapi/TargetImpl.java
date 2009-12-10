/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.deployapi;

import javax.enterprise.deploy.spi.Target;
import org.glassfish.deployment.client.TargetOwner;

import java.io.IOException;

/**
 * Implements the Target interface as specified by JSR-88.
 * <p>
 * This implementation is independent of the concrete type of its owner.
 * 
 * @author tjquinn
 */
public class TargetImpl implements Target {

    private TargetOwner owner;
    
    private String name;
    
    private String description;
    
    /**
     * Creates a new TargetImpl object.
     * <p>
     * Note that this constructor should normally be used only by a TargetOwner.
     * Logic that needs to create {@link Target} instances should invoke {@link TargetOwner#createTarget} or 
     * {@link TargetOwner#createTargets} on the TargetOwner.
     * 
     * @param owner
     * @param name
     * @param description
     */ // XXX It would be nice to move classes around so this could be package-visible and not public
    public TargetImpl(TargetOwner owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of the Target.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the Target.
     * @return
     */
    public String getDescription() {
        return description;
    }
    
    public TargetOwner getOwner() {
        return owner;
    }

    /**
     *  Exports the Client stub jars to the given location.
     *  @param appName The name of the application or module.
     *  @param destDir The directory into which the stub jar file
     *  should be exported.
     *  @return the absolute location to the main jar file.
     */
    public String exportClientStubs(String appName, String destDir) 
        throws IOException {
        return owner.exportClientStubs(appName, destDir);
    }
}
