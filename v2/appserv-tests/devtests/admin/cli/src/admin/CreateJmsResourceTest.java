package admin;

/*
 * @author David Zhao
 */
public class CreateJmsResourceTest extends AdminBaseDevTest {
    private static String QUEUE1 = "jms/unittest/queue1";
    private static String FACTORY1 = "jms/unittest/factory1";
    private static String QUEUE2 = "jms/unittest/queue2";
    private static String FACTORY2 = "jms/unittest/factory2";
    private static String FACTORY3 = "jms/unittest/factory3";
    private static String POOL1 = FACTORY3 + "-Connection-Pool";

    public static void main(String[] args) {
        new CreateJmsResourceTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for creating jms resource";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("delete-jms-resource", QUEUE1);
            asadmin("delete-jms-resource", FACTORY1);
            asadmin("delete-jms-resource", QUEUE2);
            asadmin("delete-jms-resource", FACTORY2);
            asadmin("delete-jms-resource", FACTORY3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        createJmsResourceWithoutForce();
        createJmsResourceWithForce();
        // The test - createJmsCFWithDupName is disabled temporarily
        // createJmsCFWithDupName();
        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void createJmsResourceWithoutForce() {
        report("createJmsResourceWithoutForce-0", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", QUEUE1));
        checkResource("checkJmsResourceWithoutForce-0", QUEUE1);
        report("createJmsResourceWithoutForce-1", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", FACTORY1));
        checkResource("checkJmsResourceWithoutForce-0", FACTORY1);
    }

    private void createJmsResourceWithForce() {
        report("createJmsResourceWithForce-0", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", "--force", QUEUE1));
        checkResource("checkJmsResourceWithForce-0", QUEUE1);
        report("createJmsResourceWithForce-1", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force", FACTORY1));
        checkResource("checkJmsResourceWithForce-1", FACTORY1);
        report("createJmsResourceWithForce-2", !asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force=xyz", FACTORY2));
        report("createJmsResourceWithForce-3", asadmin("create-jms-resource", "--restype", "javax.jms.Queue", "--property", "imqDestinationName=myQueue", "--force", QUEUE2));
        checkResource("checkJmsResourceWithForce-3", QUEUE1);
        report("createJmsResourceWithForce-4", asadmin("create-jms-resource", "--restype", "javax.jms.QueueConnectionFactory", "--force", FACTORY2));
        checkResource("checkJmsResourceWithForce-4", FACTORY2);
    }

    // GLASSFISH-21655: When a CF is created with same JNDI name as that of an existing resource, there should not be a CF or a connection pool created
    private void createJmsCFWithDupName() {
        asadmin("create-jms-resource", "--restype", "javax.jms.Topic", FACTORY3);
        report("createJmsCFWithDupName-0", !asadmin("create-jms-resource", "--restype", "javax.jms.ConnectionFactory", FACTORY3));
        AsadminReturn result = asadminWithOutput("list-connector-connection-pools");
        report("createJmsCFWithDupName-1", !result.out.contains(POOL1));
    }

    private void checkResource(String testName, String expected) {
        AsadminReturn result = asadminWithOutput("list-jms-resources");
        report(testName, result.out.contains(expected));
    }
}