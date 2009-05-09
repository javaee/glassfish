package org.glassfish.tests.kernel.deployment.container;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;
import java.util.Map;
import java.io.IOException;

import com.sun.enterprise.module.Module;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:20:37 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class FakeSniffer implements Sniffer {

    public boolean handles(ReadableArchive source, ClassLoader loader) {
        // I handle everything
        return true;
    }

    public String[] getURLPatterns() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<? extends Annotation>[] getAnnotationTypes() {
        return null;
    }

    public String getModuleType() {
        return "fake";
    }

    public Module[] setup(String containerHome, Logger logger) throws IOException {
        return null;
    }

    public void tearDown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getContainersNames() {
        return new String[] { "FakeContainer" };
    }

    public boolean isUserVisible() {
        return false;
    }

    public Map<String, String> getDeploymentConfigurations(ReadableArchive source) throws IOException {
        return null;
    }

    public String[] getIncompatibleSnifferTypes() {
        return new String[0];
    }

}
