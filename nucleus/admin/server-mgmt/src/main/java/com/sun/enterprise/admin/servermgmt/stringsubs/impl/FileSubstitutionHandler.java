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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.admin.servermgmt.SLogger;
import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Abstract class initialize the input file for the string substitution.
 * The sub-classes provides the way to create the {@link Reader} and
 * {@link Writer} for the input & output file.
 */
public abstract class FileSubstitutionHandler implements Substitutable {

    protected static final Logger _logger = SLogger.getLogger(); 
            
    protected static final LocalStringsImpl _strings = new LocalStringsImpl(FileLister.class);

    /** A {@link Reader} to read the character stream from input file. */
    protected Reader _reader;

    /** A {@link Writer} to write the character stream to the output file. */
    protected Writer _writer;

    /** Input file. */
    protected File _inputFile;

    public FileSubstitutionHandler(File file) throws FileNotFoundException {
        if (file.exists()) {
            _inputFile = file;
        } else {
            throw new FileNotFoundException(_strings.get("invalidFileLocation", file.getAbsolutePath()));
        }
    }

    @Override
    public String getName() {
        return _inputFile.getAbsolutePath();
    }

    @Override
    public void finish() {
        if (_reader != null) {
            try {
                _reader.close();
            } catch (Exception e) {
            	if (_logger.isLoggable(Level.FINER)) {
            		_logger.log(Level.FINER, _strings.get("errorInClosingStream", _inputFile.getAbsolutePath() ), e);
            	}
            }
        }
        if (_writer != null) {
            try {
                _writer.close();
            } catch (Exception e) {
            	if (_logger.isLoggable(Level.FINER)) {
            		_logger.log(Level.FINER, _strings.get("errorInClosingStream"), e);
            	}
            }
        }
    }
}
