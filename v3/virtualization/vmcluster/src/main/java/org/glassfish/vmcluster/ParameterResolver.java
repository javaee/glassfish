package org.glassfish.vmcluster;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Sep 28, 2010
 * Time: 2:30:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ParameterResolver {

    // possible paremeters that can be passed to a cluster creation time script
    enum ClusterCreationParameter {
        CLUSTER_NAME,
        ADMIN_IP,
        ADMIN_PORT,
        IMAGE_PATH,
        IMAGE_FILE
    }

    enum InstanceCreationParameter {
        CLUSTER_NAME,
        INSTANCE_NAME,
        IMAGE_FILE
    }

    enum InstanceLifecycleParameter {
        INSTANCE_NAME
    }

    enum InstanceDeletionParameter {
        INSTANCE_NAME
    }

    enum ClusterDeletionParameter {
        CLUSTER_NAME
    }

    String resolve(String name);
}
