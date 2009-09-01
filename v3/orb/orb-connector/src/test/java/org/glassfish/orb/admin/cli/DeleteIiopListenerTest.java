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


public class DeleteIiopListenerTest extends org.glassfish.tests.utils.ConfigApiTest {

    private Habitat habitat;
    private IiopService iiopService;
    private Properties parameters;
    private CommandRunner cr;
    private AdminCommandContext context;

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
        cr = habitat.getComponent(CommandRunner.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(DeleteIiopListenerTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
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
    }


    /**
     * Test of execute method, of class DeleteIiopListener.
     * delete-iiop-listener iiop_1
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.setProperty("listeneraddress", "localhost");
        parameters.setProperty("iiopport", "4440");
        parameters.setProperty("listener_id", "iiop_1");
        CreateIiopListener createCommand = habitat.getComponent(CreateIiopListener.class);
        cr.doCommand("create-iiop-listener", createCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        parameters.clear();
        parameters.setProperty("listener_id", "iiop_1");
        DeleteIiopListener deleteCommand = habitat.getComponent(DeleteIiopListener.class);
        cr.doCommand("delete-iiop-listener", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        List<IiopListener> listenerList = iiopService.getIiopListener();
        for (IiopListener listener : listenerList) {
            if (listener.getId().equals("iiop_1")) {
                isDeleted = false;
                logger.fine("IIOPListener name iiop_1 is not deleted.");
                break;
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class DeleteIiopListener.
     * delete-iiop-listener doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.setProperty("DEFAULT", "doesnotexist");
        DeleteIiopListener deleteCommand = habitat.getComponent(DeleteIiopListener.class);
        cr.doCommand("delete-iiop-listener", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}