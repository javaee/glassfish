/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.cmp;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.persistence.api.deployment.DeploymentException;
import com.sun.persistence.api.model.mapping.MappingModel;
import com.sun.persistence.deployment.impl.reflection.DeploymentUnitImpl;
import com.sun.persistence.deployment.impl.JDOModelMapper;
import com.sun.persistence.deployment.impl.MappingModelMapper;
import com.sun.persistence.utility.logging.Logger;
import com.sun.persistence.runtime.model.mapping.impl.RuntimeMappingModelFactoryImpl;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGeneratorHelper;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.DeploymentHelper;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.impl.model.jdo.caching.JDOModelFactoryImplCaching;
import com.sun.org.apache.jdo.impl.model.jdo.util.PrintSupport;


/**
 * This class is used during deployment as well as application loading.
 * During deployment, it is called from
 * {@link com.sun.persistence.cmp.ejbc.JDOCodeGenerator}.
 * During application loading, it gets called from
 * {@link com.sun.enterprise.server.AbstractLoader}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceJarLoader
        implements com.sun.enterprise.server.PersistenceJarLoader{

    // TODO:
    // 1) Lower message's log level after M2.
    // 2) Register event handler so that unload gets called when app is unloaded

    /* The module name of this EjbBundleDescriptor */
    private String moduleName;

    /* This represents the EJB 3.0 deployment model */
    private DeploymentUnitImpl du;

    /* Ths provides the database model.
     * Note: We don't support multiple .dbschema file right now.
     * When we do that, we need a collection of SchemaElement
     * as oppsed to just a reference. */
    private SchemaElement schema;

    private String schemaName;

    /* The cloass loader associated with this deployment unit */
    private ClassLoader cl;

    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperCMP.class);

    private final static Logger logger = LogHelperCMP.getLogger();

    /**
     * need a public no arg constructor as this is used in {@link
     * Class#newInstance()} by {@link com.sun.enterprise.server.AbstractLoader}.
     */
    public PersistenceJarLoader() {
    }

    /**
     * {@inheritDoc}
     */
    public void load(EjbBundleDescriptor ejbBundleDescriptor) {
        moduleName = JDOCodeGeneratorHelper.getModuleName(ejbBundleDescriptor);
        logger.info(i18NHelper.msg("MSG_PersistenceJarLoaderLoad", // NOI18N
                moduleName));

        if(!ejbBundleDescriptor.containsPersistenceEntity()) {
            return;
        }

        schemaName = DeploymentHelper.getDDLNamePrefix(ejbBundleDescriptor);
        du = DeploymentUnitImpl.class.cast(
                ejbBundleDescriptor.getDeploymentUnit());
        cl = ejbBundleDescriptor.getClassLoader();
        if(du.getClassLoader() != cl){
            du.setClassLoader(cl);
        }

        try {
            // We don't populate defaults, as we expect user to give us
            // full DD, which they can generate before running asadmin deploy.
            mapModels();
        } catch (DeploymentException e) {
            logger.log(Logger.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void unload(){
        logger.info(i18NHelper.msg("MSG_PersistenceJarLoaderUnload", // NOI18N
                moduleName));

        // TODO: Once model factory has unregister method, call them here.

        SchemaElement.removeFromCache(schemaName);
    }

    /**
     * This method maps PDOL descriptors to persistence model.
     *
     * @return a {@link MappingModel} which has been populated with PDOL
     *         descriptor information. From the {@link MappingModel}, it is
     *         possible to reach the {@link JDOModel} as well as {@link
     *         JavaModel}. The MappingModel also gets registered with
     *         {@link RuntimeMappingModelFactoryImpl}
     * @throws DeploymentException
     */
    private MappingModel mapModels()
            throws DeploymentException {
        // TODO: Remove this once deployment code starts using JDO JavaModel
        // as opposed to its private JavaModel.
        // We should get the JavaModel from du.
        JavaModel javaModel = RuntimeJavaModelFactory.getInstance()
                .getJavaModel(cl); // call getXXX instead of createXXX
        logger.info(i18NHelper.msg("MSG_JDOModelConversion", // NOI18N
                moduleName));
        JDOModel jdoModel = JDOModelFactoryImplCaching.getInstance()
                .getJDOModel(javaModel, false);
        JDOModelMapper.map(du, jdoModel);
        if(logger.isLoggable(Logger.FINER)){
            try{
                PrintSupport.printJDOModel(jdoModel);
            }catch(Exception e){
                logger.log(Logger.WARNING, e.getMessage(), e);
            }
        }
        loadDBModel();
        logger.info(i18NHelper.msg("MSG_MappingModelConversion", // NOI18N
                moduleName));
        MappingModel mappingModel = RuntimeMappingModelFactoryImpl.
                getInstance().getMappingModel(jdoModel, null);
        MappingModelMapper.map(du, schema, mappingModel);
        return mappingModel;
    }

    /**
     * This method is responsible for loading the database model. In mapped
     * mode, we either have .dbschema file which it used to load the database
     * model in memory or we have connection details to read capture schema from
     * database (this is not yet supported).
     *
     * @throws DeploymentException if schema could not be loaded.
     */
    private void loadDBModel() throws DeploymentException {
        logger.info(i18NHelper.msg("MSG_LoadingDBSchema", schemaName)); // NOI18N
        schema = SchemaElement.forName(schemaName,cl);
        if (schema == null) {
            throw new DeploymentException(i18NHelper.msg(
                    "EXC_UnableToLoadDBSchema", schemaName)); // NOI18N
        }
    }
}
