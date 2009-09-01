package org.glassfish.orb.admin.cli;

import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Properties;


public class CreateIiopListenerTest extends org.glassfish.tests.utils.ConfigApiTest {

    private Habitat habitat;
    private IiopService iiopService;
    private Properties parameters;
    private AdminCommandContext context;
    private CommandRunner cr;

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    @Before
    public void setUp() {
        habitat = getHabitat();
        iiopService = habitat.getComponent(IiopService.class);
        parameters = new Properties();
        context = new AdminCommandContext(
                LogDomains.getLogger(CreateIiopListenerTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr = habitat.getComponent(CommandRunner.class);
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<IiopService>() {
            public Object run(IiopService param) throws PropertyVetoException,
                    TransactionFailure {
                List<IiopListener> listenerList = param.getIiopListener();
                for (IiopListener listener : listenerList) {
                    String currListenerId = listener.getId();
                    if (currListenerId != null && currListenerId.equals
                            ("iiop_1")) {
                        listenerList.remove(listener);
                        break;
                    }
                }
                return listenerList;
            }
        }, iiopService);
        parameters.clear();
    }

    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enabled=true --securityenabled=true iiop_1
     */
    @Test
    public void testExecuteSuccess() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        parameters.setProperty("enabled", "true");
        parameters.setProperty("securityenabled", "true");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getEnabled());
                assertEquals("4440", listener.getPort());
                assertEquals("true", listener.getSecurityEnabled());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     */

    @Test
    public void testExecuteSuccessDefaultValues() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }


    /**
     * Test of execute method, of class CreateIiopListener.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     */
    @Test
    public void testExecuteFailDuplicateListener() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        CreateIiopListener command1 = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command1, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());

        CreateIiopListener command2 = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command2, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        int numDupRes = 0;
        listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                numDupRes = numDupRes + 1;
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener with same iiop port number
     * and listener address.
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_1
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 iiop_2
     */
    @Test
    public void testExecuteFailForSamePortAndListenerAddress() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());

        parameters.clear();
        parameters.setProperty("listener_id", "iiop_2");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listeneraddress", "localhost");
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());

    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled set to junk
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enabled=junk iiop_1
     */
    //@Test
    public void testExecuteFailInvalidOptionEnabled() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        parameters.setProperty("enabled", "junk");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled has no value
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --enable iiop_1
     */
    @Test
    public void testExecuteSuccessNoValueOptionEnabled() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        parameters.setProperty("enabled", "");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getEnabled());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateIiopListener when enabled has no value
     * asadmin create-iiop-listener --listeneraddress localhost
     * --iiopport 4440 --securityenabled iiop_1
     */
    @Test
    public void testExecuteSuccessNoValueOptionSecurityEnabled() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        parameters.setProperty("securityenabled", "");
        CreateIiopListener command = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                assertEquals("localhost", listener.getAddress());
                assertEquals("true", listener.getSecurityEnabled());
                assertEquals("4440", listener.getPort());
                isCreated = true;
                logger.fine("IIOPListener name iiop_1 is created.");
                break;
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}