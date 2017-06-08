/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment.util;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 *Custom Ant task that runs Java, just like the built-in java ant task, but stores the result of the invocation as a 
 *property value, which Ant's java task does not (yet) support.
 *<p>
 *Fortunately, we only need to minimally extend the Java class in Ant, overriding the execute method (to set the result property if
 *it was specified) and adding the setResultProperty method (inspired by the method of the same name from the execute task)
 *so the user can specify a property whose value
 *should be set to the Java execution's result.  The only real difference between this method's execute
 *method and the one from the original Java task is the handling of the result property.  Even so, the code had
 *to be duplicated in order to retain the result value from running the JVM.
 *<p>
 *Note that the execute method, essentially duplicated from the java task source, refers to some private local
 *variables.  Because these are defined as private (not protected) in the java task source, this class cannot
 *refer to them directly.  So, we duplicate those private variables in this class and also duplicate the setter
 *methods for them.  Those setter methods not only set this class's private variable but also invoke the corresponding
 *method in the superclass.
 *
 * @author  tjquinn
 */
public class JavaWithResult extends org.apache.tools.ant.taskdefs.Java {
    
    /*
     *String holding the name of the property to be set to the result value of the Java execution.  If
     *the build.xml file does not specify the property name, then this will remain null and no property
     *value assignment will occur.
     */
    private String resultProperty = null;
    
    /*
     *The next two declarations were duplicated from the java task's source so the duplicated execute
     *method could be modified as little as possible.
     */
    private File dir = null;
    private boolean failOnError = false;

    /** Creates a new instance of JavaWithResult */
    public JavaWithResult() {
    }
    
    public void init() {
        super.init();
        /*
         *Init the resultProperty to null so we can tell if the resultproperty attribute is actually
         *specified in this use of the javaWithResult task.
         */
        resultProperty = null;
    }

    /**
     * The working directory of the process.
     *<p>
     *This method simply mirrors the superclass setDir method.  We use it to set this class's private dir
     *variable and also to set the superclass's private dir variable.  
     */
    public void setDir(File d) {
        super.setDir(d);
        this.dir = d;
    }
    
    /**
     *Assigns the name of the property to receive the result value from the JVM execution.
     *@param resultProperty name of the property to be assigned
     */
    public void setResultProperty(String resultProperty) {
        this.resultProperty = resultProperty;
    }
    
    /**
     * If true, then fail if the command exits with a
     * returncode other than 0
     */
    public void setFailonerror(boolean fail) {
        super.setFailonerror(fail);
        failOnError = fail;
    }


    /**
     * Do the execution.
     *<p>
     *Almost all of this code is duplicated from the source for the build-in java ant task.  The difference is 
     *the logic that checks whether the resultproperty variable has been set and, if so, assigns a value to that
     *property.
     */
    public void execute() throws BuildException {
        File savedDir = dir;

        int err = -1;
        try {
            if ((err = executeJava()) != 0) { 
                if (failOnError) {
                    throw new BuildException("Java returned: " + err, location);
                } else {
                    log("Java Result: " + err, Project.MSG_ERR);
                }
            }
        } finally {
            dir = savedDir;
            
            /*
             *If the result property was assigned - meaning the invoking build.xml wants to know the
             *result of the JVM execution - then assign the JVM result to the specified property.  Because
             *properties are string-valued, convert the integer result from executeJava to a string first.
             */
            String res = Integer.toString(err);
            if (resultProperty != null) {
                project.setNewProperty(resultProperty, res);
            }
        }
    }

}
