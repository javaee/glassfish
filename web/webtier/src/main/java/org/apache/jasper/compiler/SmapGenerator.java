

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.jasper.compiler;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a source map (SMAP), which serves to associate lines
 * of the input JSP file(s) to lines in the generated servlet in the
 * final .class file, according to the JSR-045 spec.
 * 
 * @author Shawn Bayern
 */
public class SmapGenerator {

    //*********************************************************************
    // Overview

    /*
     * The SMAP syntax is reasonably straightforward.  The purpose of this
     * class is currently twofold:
     *  - to provide a simple but low-level Java interface to build
     *    a logical SMAP
     *  - to serialize this logical SMAP for eventual inclusion directly
     *    into a .class file.
     */


    //*********************************************************************
    // Private state

    private String outputFileName;
    private String defaultStratum = "Java";
    private List strata = new ArrayList();
    private List embedded = new ArrayList();
    private boolean doEmbedded = true;

    //*********************************************************************
    // Methods for adding mapping data

    /**
     * Sets the filename (without path information) for the generated
     * source file.  E.g., "foo$jsp.java".
     */
    public synchronized void setOutputFileName(String x) {
	outputFileName = x;
    }

    /**
     * Adds the given SmapStratum object, representing a Stratum with
     * logically associated FileSection and LineSection blocks, to
     * the current SmapGenerator.  If <tt>default</tt> is true, this
     * stratum is made the default stratum, overriding any previously
     * set default.
     *
     * @param stratum the SmapStratum object to add
     * @param defaultStratum if <tt>true</tt>, this SmapStratum is considered
     *                to represent the default SMAP stratum unless
     *                overwritten
     */
    public synchronized void addStratum(SmapStratum stratum,
					boolean defaultStratum) {
	strata.add(stratum);
	if (defaultStratum)
	    this.defaultStratum = stratum.getStratumName();
    }

    /**
     * Adds the given string as an embedded SMAP with the given stratum name.
     *
     * @param smap the SMAP to embed
     * @param stratumName the name of the stratum output by the compilation
     *                    that produced the <tt>smap</tt> to be embedded
     */
    public synchronized void addSmap(String smap, String stratumName) {
	embedded.add("*O " + stratumName + "\n"
		   + smap
		   + "*C " + stratumName + "\n");
    }

    /**
     * Instructs the SmapGenerator whether to actually print any embedded
     * SMAPs or not.  Intended for situations without an SMAP resolver.
     *
     * @param status If <tt>false</tt>, ignore any embedded SMAPs.
     */
    public void setDoEmbedded(boolean status) {
	doEmbedded = status;
    }

    //*********************************************************************
    // Methods for serializing the logical SMAP

    public synchronized String getString() {
	// check state and initialize buffer
	if (outputFileName == null)
	    throw new IllegalStateException();
        StringBuffer out = new StringBuffer();

	// start the SMAP
	out.append("SMAP\n");
	out.append(outputFileName + '\n');
	out.append(defaultStratum + '\n');

	// include embedded SMAPs
	if (doEmbedded) {
	    int nEmbedded = embedded.size();
	    for (int i = 0; i < nEmbedded; i++) {
	        out.append(embedded.get(i));
	    }
	}

	// print our StratumSections, FileSections, and LineSections
	int nStrata = strata.size();
	for (int i = 0; i < nStrata; i++) {
	    SmapStratum s = (SmapStratum) strata.get(i);
	    out.append(s.getString());
	}

	// end the SMAP
	out.append("*E\n");

	return out.toString();
    }

    public String toString() { return getString(); }

    //*********************************************************************
    // For testing (and as an example of use)...

    public static void main(String args[]) {
	SmapGenerator g = new SmapGenerator();
	g.setOutputFileName("foo.java");
	SmapStratum s = new SmapStratum("JSP");
	s.addFile("foo.jsp");
	s.addFile("bar.jsp", "/foo/foo/bar.jsp");
	s.addLineData(1, "foo.jsp", 1, 1, 1);
	s.addLineData(2, "foo.jsp", 1, 6, 1);
	s.addLineData(3, "foo.jsp", 2, 10, 5);
	s.addLineData(20, "bar.jsp", 1, 30, 1);
	g.addStratum(s, true);
	System.out.print(g);

	System.out.println("---");

	SmapGenerator embedded = new SmapGenerator();
	embedded.setOutputFileName("blargh.tier2");
	s = new SmapStratum("Tier2");
	s.addFile("1.tier2");
	s.addLineData(1, "1.tier2", 1, 1, 1);
	embedded.addStratum(s, true);
	g.addSmap(embedded.toString(), "JSP");
	System.out.println(g);
    }
}
