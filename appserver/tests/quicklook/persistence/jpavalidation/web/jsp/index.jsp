<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

--%>

<html>
<body>
<h2>JPA Validation Tests</h2>
<br>
The entity Employee has a validation constraint of size 5.
The tests check ConstraintViolationException and expected resuls to persist, update, remove an employee with the name longer than 5.
<br>
<ul>
<li>
<a href="test?tc=initialize">Step 1. Persist a project with a few employees with short name</a> </li>
<ul><li>Expected Result: The project and employees are in database.</li></ul>
<li><a href="test?tc=validatePersist">Step 2. Persist an employee with a long name</a> </li>
<ul><li>Expected Result: That employee will not be in databse.</li></ul>
<li><a href="test?tc=validateUpdate">Step 3. Update an employee with a long name</a> </li>
<ul><li>Expected Result: The name change will not be in database.</li></ul>
<li><a href="test?tc=validateRemove">Step 4. Remove an employee with a long name</a></li>
<ul><li>Expected Result: That employee will be removed from database.</li></ul>
<li><a href="test?tc=verify">Step 5. Verify the validation</a> </li>
<ul><li>Expected Result: No employee in database has long name.</li></ul>
</ul>
</body>
</html>
