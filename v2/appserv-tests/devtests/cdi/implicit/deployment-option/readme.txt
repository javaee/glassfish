client/Client.java is created during build process from client/Client.java.token via copying it and replacing token @ORB_PORT@ with the ORB port number from config.properties (${orb.pprt}).

The application is deployed with the implicitCdiEnabled deployment property set to false, which disables CDI 1.1 implicit bean discovery.
