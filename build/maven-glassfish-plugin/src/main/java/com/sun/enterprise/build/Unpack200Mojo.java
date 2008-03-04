package com.sun.enterprise.build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Copy a single file
 * @goal unpack200
 */
public class Unpack200Mojo extends AbstractMojo 
{
    /**
     * @parameter
     */
    private String sourceDirectory;
    

    /**
     * @parameter 
     */
    private String outputDirectory;
    
    enum FileType { unknown, gzip, pack200, zip };    

    public void execute() throws MojoExecutionException 
    {
        
        File sourceDir;
        if (sourceDirectory!=null && sourceDirectory.length()>0) {
            sourceDir = new File(sourceDirectory);
        } else {
            sourceDir = new File(System.getProperty("user.dir"));
        }
        
        System.out.println("Source directory is " + sourceDirectory);
        System.out.println("Out is " + outputDirectory);
        
	Pack200.Unpacker unpkr = Pack200.newUnpacker();        
        if (!sourceDir.exists()) {
            getLog().warn("source directory " + sourceDir.getAbsolutePath()  + "does not exist");
            return;
        }
        
        if (outputDirectory==null) {
            outputDirectory = sourceDirectory;
        }
        
        File destinationDir  = new File(outputDirectory);
        if (!destinationDir.exists()) {
            if (!destinationDir.mkdirs()) {
                throw new MojoExecutionException("Aborting - Cannot create destination directory " + destinationDir.getAbsolutePath());
            }
        }
        for (File file : sourceDir.listFiles()) {

            if (file.isDirectory()) {
                continue;
            }
            
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStream is;
                try {
                    FileType fileType = getMagic(file);
                    
                    if (fileType == FileType.gzip) {
                        is = new BufferedInputStream(new GZIPInputStream(fis));
                    } else 
                    if (fileType == FileType.pack200) {
                        is = new BufferedInputStream(fis);
                    } else {
                        fis.close();
                        continue;
                    } 
                } catch(IOException ioe) {
                    getLog().info(ioe.getMessage());
                    continue;
                }
                getLog().info("Unpacking " + file.getName());                
                
                // compute real name...
                StringTokenizer stoken = new StringTokenizer(file.getName(), ".");
                
                String destFileName = stoken.nextToken() + ".";
                if (stoken.hasMoreElements()) {
                    destFileName = destFileName + stoken.nextToken();
                }
                
                FileOutputStream fos = new FileOutputStream(new File(destinationDir, destFileName));
                JarOutputStream jout = new JarOutputStream(
					new BufferedOutputStream(fos));
	    
                unpkr.unpack(is, jout);
                is.close();
                jout.close();
                
                file.delete();

            } catch (IOException ioe) {
                throw new MojoExecutionException(ioe.getMessage(), ioe);
            }
        }
    }
    
    private FileType getMagic(File in) throws IOException {
        
        DataInputStream is = new DataInputStream(new FileInputStream(in));
        int i = is.readInt();
        is.close();
        if ( (i & 0xffffff00) == 0x1f8b0800) {
            return FileType.gzip;
        } else if ( i == 0xcafed00d) {
            return FileType.pack200;
        } else if ( i == 0x504b0304) {
            return FileType.zip;
        } else {
            return FileType.unknown;
        }
    }
}
