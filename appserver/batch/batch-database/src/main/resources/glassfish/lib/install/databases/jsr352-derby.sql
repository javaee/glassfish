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

CREATE TABLE JOBINSTANCEDATA(
  jobinstanceid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT JOBINSTANCE_PK PRIMARY KEY,
  name		VARCHAR(512), 
  apptag VARCHAR(512)
);

CREATE TABLE EXECUTIONINSTANCEDATA(
  jobexecid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT JOBEXECUTION_PK PRIMARY KEY,
  jobinstanceid BIGINT,
  createtime	TIMESTAMP,
  starttime		TIMESTAMP,
  endtime		TIMESTAMP,
  updatetime	TIMESTAMP,
  parameters	BLOB,
  batchstatus		VARCHAR(512),
  exitstatus		VARCHAR(512),
  CONSTRAINT JOBINST_JOBEXEC_FK FOREIGN KEY (jobinstanceid) REFERENCES JOBINSTANCEDATA (jobinstanceid)
  );
  
CREATE TABLE STEPEXECUTIONINSTANCEDATA(
	stepexecid BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT STEPEXECUTION_PK PRIMARY KEY,
	jobexecid	BIGINT,
	batchstatus         VARCHAR(512),
    exitstatus			VARCHAR(512),
    stepname			VARCHAR(512),
	readcount			INTEGER,
	writecount			INTEGER,
	commitcount         INTEGER,
	rollbackcount		INTEGER,
	readskipcount		INTEGER,
	processskipcount	INTEGER,
	filtercount			INTEGER,
	writeskipcount		INTEGER,
	startTime           TIMESTAMP,
	endTime             TIMESTAMP,
	persistentData		BLOB,
	CONSTRAINT JOBEXEC_STEPEXEC_FK FOREIGN KEY (jobexecid) REFERENCES EXECUTIONINSTANCEDATA (jobexecid)
); 

CREATE TABLE JOBSTATUS (
  id BIGINT CONSTRAINT JOBSTATUS_PK PRIMARY KEY,
  obj		BLOB,
  CONSTRAINT JOBSTATUS_JOBINST_FK FOREIGN KEY (id) REFERENCES JOBINSTANCEDATA (jobinstanceid) ON DELETE CASCADE
);

CREATE TABLE STEPSTATUS(
  id BIGINT CONSTRAINT STEPSTATUS_PK PRIMARY KEY,
  obj		BLOB,
  CONSTRAINT STEPSTATUS_STEPEXEC_FK FOREIGN KEY (id) REFERENCES STEPEXECUTIONINSTANCEDATA (stepexecid) ON DELETE CASCADE
);

CREATE TABLE CHECKPOINTDATA(
  id		VARCHAR(512),
  obj		BLOB
);

CREATE INDEX CHK_INDEX ON CHECKPOINTDATA(id);

  
