/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.io;

import java.io.*;
import java.util.*;

/**
 * A class for sanitizing Files.
 * Note that the main reason for this class is that on non-Windows, 
 * getCanonicalXXX and getAbsoluteXXX might point at different files.  
 * If the file is a soft link then the Canonical will be the file that is linked to.
 * The Absolute will be the link file itself.
 * This method will give you the benefits of Canonical -- but will always point
 * at the link file itself. 
 * <p>
 * I.e. It is just like getAbsoluteXXX -- but it removes all relative path 
 * elements from the path.
 * @author bnevins
 */

public class SmartFile {
    /**
     * Sanitize a File object -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * @param f The file to sanitize
     * @return THe sanitized File
     */
    public static File sanitize(File f)
    {
        SmartFile sf = new SmartFile(f);
        return new File(sf.path);
    }

    /**
     * Sanitize a path -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * Note that the main reason for this class is that on non-Windows, 
     * getCanonicalXXX and getAbsoluteXXX might point at different files.  
     * If the file is a soft link then the Canonical will be the file that is linked to.
     * The Absolute will be the link file itself.
     * This method will give you the benefits of Canonical -- but will always point
     * at the link file path itself. 
     * @param filename The path to sanitize
     * @return The sanitized path
     */
    public static String sanitize(String filename) {
        SmartFile sf = new SmartFile(filename);
        return sf.path;
    }
    
    private SmartFile(File f) {
        if(f == null)
            throw new NullPointerException();
        
        convert(f.getAbsolutePath());
    }
    private SmartFile(String s) {
        if(s == null)
            throw new NullPointerException();

        // note that "" is a valid filename
        convert(new File(s).getAbsolutePath());
    }

    private void convert(String oldPath) {
        // guarantee -- the beginning will not have "." or ".."
        oldPath = oldPath.replace('\\', '/');
        String[] elemsArray = oldPath.split(SLASH);
        List<String> elems = new ArrayList<String>();
        
        for(String s : elemsArray) {
            elems.add(s);
        }
            
        // 4 possibilities
        // 1. (Windows) //x
        // 2. (Windows) X:/
        // 3. / (UNIX)
        // 4. /x (UNIX)
        
        // Windows -->  \\computer\x\y\z
        if(oldPath.startsWith("//")) { // 1.
            path = "//";
        }
        else if(!oldPath.startsWith(SLASH)) { // 2.
            path = elems.get(0) + SLASH;
            elems.remove(0);
        }
        else if(oldPath.startsWith(SLASH)) { //3,4
            path = SLASH;
        }
        
        // remove empty elems
            for(Iterator<String> it = elems.iterator(); it.hasNext(); ) {
                String elem = it.next();
                if(elem.length() <= 0) {
                    it.remove();
            }
        }
        
        // replace dots
        
        while(hasDots(elems)) {
            for(int i = 0; i < elems.size(); i++) {
                String elem = elems.get(i);

                if(elem.equals(".")) {
                    elems.remove(i);
                    break;
                }
                else if(elem.equals("..")) {
                    elems.remove(i);
                    
                    // special case -- path is something like "/foo/../../.."
                    // just return the convertial path...
                    if(i <= 0)
                        return;
                    
                    elems.remove(i-1);
                    break;
                }
            }
        }

        // now all the dots are gone.
        for(String s : elems) {
            path += s + SLASH;
        }
        // get rid of trailing slash
        if(path.length() > 1 && !path.equals("//"))
            path = path.substring(0, path.length() - 1);

        // Finally -- make it OS-friendly
        if(File.separatorChar != '/') {
            doWindows();
        }            
    }

    private void doWindows() {
        path = path.replace('/', File.separatorChar);
        
        // make the drive letter uppercase
        String drive = path.substring(0, 2);
        if(drive.endsWith(":")) {
            drive = drive.toUpperCase();
            path = drive + path.substring(2);
        }
    }

    private boolean hasDots(List<String> elems) {
        return elems.contains(".") || elems.contains("..");
    }


    private String path;
    private static final String SLASH = "/";
}
