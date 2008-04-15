/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.appserv.internal.build.util;
import java.io.*;
import java.util.*;
/** This Class file is intended to merge more than one package.
  * It strips off unnecessary information like comments, $ASINSTDIR, $ASCONFDIR.
  * It checks the existence of all files mentioned in the prototype_com file.
  * It avoids duplicate values after the merge.
  *
  */
public class MergePackage{
public static PrintWriter pwerror=null;
public static void main(String[] args) {

		try {
			String errorFile = args[(args.length-1)];
			String outFile= args[(args.length-2)];
			File outputPackage = new File(outFile);
			pwerror = new PrintWriter(new FileOutputStream(new File(errorFile)),true);
			PrintWriter pw = new PrintWriter(new FileOutputStream(outputPackage),true);
			try {
				ArrayList merged= new ArrayList();
				for (int i=0;i<(args.length - 2); i=i+2) {
					String rdLine ="";
					BufferedReader inputBuffer = new BufferedReader(new FileReader(args[i]));
					List notExist = verifyPackage(args[i], args[(i+1)]);
					//For checking the duplicate an Array "merged"
					//is maintained everytime we write we will make
					//a check wheather that entry is already added 
					//into the Array.if yes we will not add
					//In the merged array, we will put
					// first and third field of entry
					merged.addAll(notExist);
					while(true) {
						rdLine = inputBuffer.readLine();
						//End of the file
						if (rdLine == null)
							break;
						//Strip off $ASINSTDIR	
						if((rdLine.indexOf("$ASINSTDIR") ) > 0)
							rdLine = strip(rdLine,"$ASINSTDIR");
						//Strip off $ASCONFDIR	
						if((rdLine.indexOf("$ASCONFDIR") ) > 0)
							rdLine = strip(rdLine,"$ASCONFDIR");
							
						//ignore comments and empty strings
						if (rdLine.startsWith("#") || rdLine.equals("") )
							continue;
						//Again check for $ASINSTDIR ($ASINSTDIR could be on either side of 
						// "=" operator)
						if((rdLine.indexOf("$ASINSTDIR") ) > 0)
							rdLine = strip(rdLine,"$ASINSTDIR");
						
						String firstAndThird = getFirstAndThird(rdLine);
						if ((rdLine.startsWith("e") || rdLine.startsWith("f") || rdLine.startsWith("d") || rdLine.startsWith("s") || rdLine.startsWith("i") ) && !merged.contains(firstAndThird)) {
							pw.println(rdLine);
							merged.add(firstAndThird);
							pw.flush();
						}

					}
				}
			}catch(Exception e) {
			e.printStackTrace();
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("usage : java com.sun.appserv.internal.build.util.MergePackage (<prototype_com> <base_dir>)* <outputfile> <errorfile>");
		}

}

public static List verifyPackage(String protCom, String baseDirectory) {
		List notExist = new ArrayList();
	try {
		String protFile=protCom;
		String baseDir=baseDirectory;
		BufferedReader prot = new BufferedReader(new FileReader(protFile));
		pwerror.println("-------------------------------------------------------");
		pwerror.println("Error Output for "+protCom);
		String rdLine="";
		try {
			while(true) {
				rdLine = prot.readLine();
				if (rdLine == null)
					break;
				if((rdLine.indexOf("$ASINSTDIR") ) > 0)
					rdLine = strip(rdLine,"$ASINSTDIR");

				if (rdLine.startsWith("#") || rdLine.equals("") )
					continue;

				if((rdLine.indexOf("$ASINSTDIR") ) > 0)
					rdLine = strip(rdLine,"$ASINSTDIR");

				StringTokenizer st = new StringTokenizer(rdLine);
				if(st.countTokens() < 3)
					continue;
				st.nextToken();
				st.nextToken();
				String relativeFileLocation = st.nextToken();
				File f = new File(baseDir + File.separator + relativeFileLocation);
				if((relativeFileLocation.indexOf("=") ) > 0) {
					String splitString = relativeFileLocation.substring(relativeFileLocation.indexOf("=")+1);
					File fs = new File(baseDir + File.separator + splitString);
					if(!(fs.isFile() || fs.isDirectory())) {
						String firstAndThird = getFirstAndThird(rdLine);
						pwerror.println("File "+splitString+" does not exists");
						notExist.add(firstAndThird);
					}
				}
				else if(!(f.isFile() || f.isDirectory())) {
					String firstAndThird = getFirstAndThird(rdLine);
					pwerror.println("File "+relativeFileLocation+" does not exists");
					notExist.add(firstAndThird);
				}

			}
			pwerror.println("-------------------------------------------------------");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}catch(Exception e) {
			e.printStackTrace();

		System.out.println("Usage: java svrpack.CheckPackage <prototype_file> <base_directory> ");
	}
    return notExist;
}

public static String strip(String rdLine, String stripped) {
	int index = rdLine.indexOf(stripped);
	int strlen = stripped.length();
	String halfstring = rdLine.substring(0,index);
	String otherhalf = rdLine.substring((index+1+strlen));
	StringTokenizer st = new StringTokenizer(rdLine);
	st.nextToken();
	st.nextToken();
	String thridToken = st.nextToken();
	if(thridToken.equals(stripped) && stripped.equals("$ASINSTDIR"))
	return (halfstring + "appserver " +otherhalf);
	if(thridToken.equals(stripped) && stripped.equals("$ASCONFDIR"))
	return (halfstring + "appserver/config " +otherhalf);
	//	return "";
	if( stripped.equals("$ASINSTDIR"))
	return (halfstring + "appserver/" +otherhalf);
	if( stripped.equals("$ASCONFDIR"))
	return (halfstring + "appserver/config/" +otherhalf);
	return "";
}

public static String  getFirstAndThird(String rdLine) {
	StringTokenizer st = new StringTokenizer(rdLine);
	String firstToken = st.nextToken();
	String secondToken = st.nextToken();
	if(rdLine.startsWith("i")) {
		return (firstToken+" "+secondToken);
	}
	String thridToken = st.nextToken();
	return (firstToken+" "+thridToken);
}
}
