/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 2005, 2006, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.weaving;

// J2SE imports
import java.lang.instrument.*;
import java.io.FileOutputStream;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.File;
import javax.persistence.spi.ClassTransformer;

// ASM imports
import oracle.toplink.libraries.asm.*;
import oracle.toplink.libraries.asm.attrs.Attributes;

// TopLink imports
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.Session;


/**
 * INTERNAL:
 * This class performs dynamic bytecode weaving: for each attribute
 * mapped with One To One mapping with Basic Indirection it substitutes the
 * original attribute's type for ValueHolderInterface. 
 */
public class TopLinkWeaver implements ClassTransformer {
	
    public static final String WEAVING_OUTPUT_PATH = "toplink.weaving.output.path";
    public static final String WEAVING_SHOULD_OVERWRITE = "toplink.weaving.overwrite.existing";
    public static final String WEAVER_NOT_OVERWRITING = "weaver_not_overwriting";
    public static final String WEAVER_COULD_NOT_WRITE = "weaver_could_not_write";
    public static final String WEAVER_FAILED = "weaver_failed";
    public static final String WEAVER_TRANSFORMED_CLASS = "weaver_transformed_class";
    
    protected Session session; // for logging
	// Map<String, ClassDetails> where the key is className in JVM '/' format 
	protected Map classDetailsMap;
    
	public TopLinkWeaver(Session session, Map classDetailsMap) {
		this.session = session;
		this.classDetailsMap = classDetailsMap;
	}
	
	public Map getClassDetailsMap() {
		return classDetailsMap;
	}

	// @Override: well, not precisely. I wanted the code to be 1.4 compatible,
	// so the method is written without any Generic type <T>'s in the signature
    public byte[] transform(ClassLoader loader, String className,
		Class classBeingRedefined, ProtectionDomain protectionDomain,
		byte[] classfileBuffer) throws IllegalClassFormatException {
        
		/*
		 * The ClassFileTransformer callback - when called by the JVM's
		 * Instrumentation implementation - is invoked for every class loaded.
		 * Thus, we must check the classDetailsMap to see if we are 'interested'
		 * in the class.
		 * 
		 * Note: when invoked by the OC4J wrapper class
		 * oracle.toplink.essentials.internal.ejb.cmp3.oc4j.OC4JClassTransformer,
		 * callbacks are made only for the 'interesting' classes
		 */
		ClassDetails classDetails = (ClassDetails)classDetailsMap.get(className);
        if (classDetails != null) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(true, true);
            TopLinkClassWeaver tcw = new TopLinkClassWeaver(cw, classDetails);
            try {
                cr.accept(tcw, Attributes.getDefaultAttributes(), false);
            } catch (Throwable e) {
                // RuntimeException or Error could be thrown from ASM
                // log here because ClassLoader ignore any Throwable
                log(SessionLog.SEVERE, WEAVER_FAILED, new Object[]{className, e});
                log(SessionLog.SEVERE, e);

                IllegalClassFormatException ex = new IllegalClassFormatException();
                ex.initCause(e);
                throw ex;
            }
            if (tcw.alreadyWeaved) {
                return null;
            }
            byte[] bytes = cw.toByteArray();
			
            String outputPath = System.getProperty(WEAVING_OUTPUT_PATH, "");

            if (!outputPath.equals("")) {
                outputFile(className, bytes, outputPath);
			}
            if (tcw.weavedVH) {
                log(SessionLog.FINER, WEAVER_TRANSFORMED_CLASS, new Object[]{className});
                return bytes;
            }
        }
        return null; // returning null means 'use existing class bytes'
    }
	
    protected void outputFile(String className, byte[] classBytes, String outputPath){
        StringBuffer directoryName = new StringBuffer();;
        StringTokenizer tokenizer = new StringTokenizer(className, "\n\\/");
        String token = null;
        while (tokenizer.hasMoreTokens()){
            token = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()){
                directoryName.append(token + File.separator);
            }
        }
        try{
            String usedOutputPath = outputPath;
            if (!outputPath.endsWith(File.separator)){
                usedOutputPath = outputPath + File.separator;
            }
            File file = new File(usedOutputPath + directoryName);
            file.mkdirs();
            file = new File(file, token + ".class");
            if (!file.exists()){
                file.createNewFile();
            } else {
                if (!System.getProperty(WEAVING_SHOULD_OVERWRITE, "false").equalsIgnoreCase("true")){
                    log(SessionLog.WARNING, WEAVER_NOT_OVERWRITING, new Object[]{className});
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(classBytes);
            fos.close();
        } catch (Exception e){
            log(SessionLog.WARNING, WEAVER_COULD_NOT_WRITE, new Object[]{className, e});
        }
    }
    
	// same as in oracle.toplink.essentials.internal.helper.Helper, but uses
	// '/' slash as delimiter, not '.'
	protected static String getShortName(String name) {
		int pos  = name.lastIndexOf('/');
		if (pos >= 0) {
			name = name.substring(pos+1);
			if (name.endsWith(";")) {
				name = name.substring(0, name.length()-1);
			}
			return name;
		}
		return "";
	}

    protected void log(int level, String msg, Object[] params) {
        ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).log(level,
            SessionLog.WEAVER, msg, params);
    }
    
    protected void log(int level, Throwable t) {
        ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).logThrowable(level,
            SessionLog.WEAVER, t);
    }
}
