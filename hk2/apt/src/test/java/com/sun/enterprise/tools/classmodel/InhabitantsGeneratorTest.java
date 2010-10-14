package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.classmodel.ClassPathHelper;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import com.sun.enterprise.tools.InhabitantsDescriptor;

/**
 * Tests the introspective type of InhabitantsGenerator.
 *
 * @author Jeff Trent
 */
public class InhabitantsGeneratorTest {

    @Test
    public void sanityTest() throws Exception {
        ArrayList<File> sources = getTestClassPathEntries();

        InhabitantsGenerator generator = new InhabitantsGenerator();
        InhabitantsParsingContextGenerator ipcGen = generator.getContextGenerator();
        ipcGen.parse(sources);
        
        ParsingContext pc = ipcGen.getContext();
        assertNotNull(pc);

        Types types = pc.getTypes();

        AnnotationType ia = types.getBy(AnnotationType.class, InhabitantAnnotation.class.getName());
        AnnotationType s = types.getBy(AnnotationType.class, Service.class.getName());
        AnnotationType c = types.getBy(AnnotationType.class, Contract.class.getName());

        assertNotNull("@InhabitantAnnotation not found", ia);
        assertNotNull("Service not found", s);
        assertNotNull("@Contract not found", c);
    }

    @Ignore
    @Test
    public void testHabitatFileGeneration() throws IOException {
        ArrayList<File> testDir = getTestClassPathEntries();

        InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
        descriptor.enableDateOutput(false);

        InhabitantsGenerator generator = new InhabitantsGenerator(descriptor);
        generator.add(testDir);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        generator.generate(writer, null);
        writer.close();

        String output = out.toString();
        assertNotNull(output);
        assertEquals(expected(), output);
    }

    String expected() {
        StringBuilder sb = new StringBuilder();
        sb.append("class=com.sun.enterprise.tools.classmodel.test.BService,index=com.sun.enterprise.tools.classmodel.test.BContract\r\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.RunLevelCloseableService,index=java.io.Closeable:closeable,index=org.jvnet.hk2.annotations.RunLevel\r\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.AService,index=com.sun.enterprise.tools.classmodel.test.AContract:aservice,a=1,b=2\r\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.FactoryForCService,index=org.jvnet.hk2.annotations.FactoryFor:com.sun.enterprise.tools.classmodel.test.CService\r\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.CService\r\n");
        return sb.toString();
    }

    @Ignore
    @Test
    public void testMain() throws Exception {
        File testDir = new File(new File("."), "target/test-classes");
        File outputFile = new File(testDir, "META-INF/inhabitants/default");
//    System.out.println(outputFile.getAbsolutePath());
        outputFile.delete();

        System.setProperty(InhabitantsGenerator.PARAM_INHABITANT_FILE, outputFile.getAbsolutePath());
        System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES, testDir.getAbsolutePath());
        InhabitantsGenerator.main(null);

        assertTrue("expect to find: " + outputFile, outputFile.exists());

        StringBuilder sb = new StringBuilder();
        FileInputStream fis = new FileInputStream(outputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line;
        while (null != (line = reader.readLine())) {
            sb.append(line).append("\r\n");
        }
        fis.close();

        assertTrue(sb.toString() + " was not found in output file", sb.toString().contains(expected()));
    }

    public ArrayList<File> getTestClassPathEntries() {
        ArrayList<File> entries = new ArrayList<File>();

        ClassPathHelper classpath = ClassPathHelper.create(null, false);
        Set<String> cpSet = classpath.getEntries();
        for (String entry : cpSet) {
            entries.add(new File(entry));
        }

        if (entries.isEmpty()) {
            throw new RuntimeException("can't find test-classes in " + cpSet);
        }

        System.out.println("classpath is " + entries);

        return entries;
    }
}
