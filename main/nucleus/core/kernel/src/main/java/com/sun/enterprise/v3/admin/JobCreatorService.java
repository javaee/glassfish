/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobCreator;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.security.auth.Subject;
import java.io.File;
import org.glassfish.api.admin.ParameterMap;

/**
 * This service implements the <code>JobCreator</code> and is
 * used for creating Jobs
 * @author Bhakti Mehta
 */
@Service (name="job-creator")
public class JobCreatorService  implements JobCreator {

    @Inject
    private ServerEnvironment serverEnvironment;

    @Inject JobManagerService jobManagerService;

    private static final String JOBS_FILE = "jobs.xml";
    /**
     * This will create a new job with the name of command and a new unused id for the job
     *
     *
     * @param scope The scope of the command or null if there is no scope
     * @param name  The name of the command
     * @return   a newly created job
     */
    @Override
    public Job createJob(String id, String scope, String name, Subject subject, boolean isManagedJob, ParameterMap parameters) {
        AdminCommandInstanceImpl job = null;
        if (isManagedJob) {
            job =  new AdminCommandInstanceImpl(id, name, scope, subject, true, parameters);
            job.setJobsFile(jobManagerService.jobsFile);
        } else {
            job =  new AdminCommandInstanceImpl(name, scope, subject, false, parameters);
        }
        return job;
    }


}
