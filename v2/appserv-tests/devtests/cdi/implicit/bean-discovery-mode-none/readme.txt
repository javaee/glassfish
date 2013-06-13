client/Client.java is created during build process from client/Client.java.token via copying it and replacing token @ORB_PORT@ with the ORB port number from config.properties (${orb.pprt}).

The application is assembled with a beans.xml that indicates that implicit CDI bean discovery should not be applied.
