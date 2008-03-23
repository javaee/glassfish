package com.sun.enterprise.php;

import com.sun.enterprise.module.impl.CookedLibRepository;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleDefinition;

import java.io.IOException;
import java.io.File;
import java.util.jar.Attributes;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 19, 2007
 * Time: 2:55:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhpRepository extends CookedLibRepository {

    List<ModuleDefinition> moduleDefs = new ArrayList<ModuleDefinition>();

    public PhpRepository() {
        super(System.getProperty("com.sun.aas.installRoot") + File.separator + "lib" + File.separator + "php");
    }
    /**
     * Initialize the repository for use. This need to be called at least
     * once before any find methods is invoked.
     * @throws java.io.IOException if an error occur accessing the repository
     */
    public void initialize() throws IOException {
        
        Attributes quercusAttr = new Attributes();
        quercusAttr.putValue(Attributes.Name.CLASS_PATH.toString(), "resin-util.jar script-10.jar");

        CookedModuleDefinition quercus = new CookedModuleDefinition(
                new File(rootLocation, "quercus.jar"), quercusAttr);
        quercus.addDependency(new ModuleDependency("javaee.jar", null));
        moduleDefs.add(quercus);

    }

    public List<ModuleDefinition> findAll() {
        return moduleDefs;
    }

    /**
     * Returns the repository name
     * @return repository name
     */
    public String getName() {
        return "quercus ";
    }

}
