package org.glassfish.enterprise.admin.ncli;

/**  The main class that is designed to work on arguments that are presented on a <i> command line </i>.
 *   It is designed for remote commands as of now.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since  GlassFish v3
 */
public class RemoteCommandRunner {
    private final String[] args;

    public RemoteCommandRunner(String[] args) {
        this.args = args;
    }

    public static void main(String... args) throws GenericCommandException {
        new RemoteCommandRunner(args).run();
    }

    private void run() throws GenericCommandException {
    }
}
