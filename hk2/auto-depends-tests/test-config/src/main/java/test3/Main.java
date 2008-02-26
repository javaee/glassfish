package test3;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.config.Dom;
import test3.cage.JmsCompanion;
import test3.cage.TestCageBuilder;
import test3.substitution.SecurityMap;

import java.util.Collection;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Main extends Assert implements ModuleStartup {
    @Inject
    FooBean foo;

    @Inject
    Bird bird;

    @Inject
    Lion lion;

    @Inject
    Habitat habitat;

    @Inject
    TestCageBuilder testCageBuilder;

//    private static final XMLOutputFactory xof = XMLOutputFactory.newInstance();

    public void setStartupContext(StartupContext context) {
    }

    public void run() {
        assertNotNull(foo);
        // foo.e.printStackTrace(); // could be useful to find out where foo is created.
        assertEquals(80,foo.httpPort);
        assertEquals(foo.bar,"qwerty");

        // test the proxies
        JmsHost jms = find(foo.all, JmsHost.class);
        System.out.println(jms.toString());
        List<Property> props = jms.getProperties();
        assertEquals(2, props.size());
        assertEquals("foo",props.get(0).name);
        assertEquals("abc",props.get(0).value);

        assertEquals(3,foo.properties.size());
        assertNotNull(foo.properties.get("xyz"));
        assertNotNull(foo.properties.get("qqq"));
        assertNotNull(foo.properties.get("adminPort"));

        for (Property p : foo.properties.values())
            assertTrue(p.constructed);

        assertEquals(2,foo.jvmOptions.size());
        assertEquals(foo.jvmOptions.get(0),"-Xmx256m");
        assertEquals(foo.jvmOptions.get(1),"-verbose:abcwww");

        assertEquals(2,foo.httpListeners.size());

        HttpListener listener = habitat.getComponent(HttpListener.class, "a");
        assertEquals("a",listener.id);

        assertEquals(1,foo.virtualServers.size());
        VirtualServer vserver = foo.virtualServers.get(0);
        assertEquals(2,vserver.httpListeners.size());
        assertTrue(vserver.httpListeners.contains(habitat.getComponent(HttpListener.class, "a")));
        assertTrue(vserver.httpListeners.contains(habitat.getComponent(HttpListener.class, "b")));

        // test substitutability
        System.out.println(foo.find(SecurityMap.class).toString());

        // testing dynamic reconfiguration
        assertEquals(5,listener.acceptorThreads);
        Dom i = (Dom) habitat.getInhabitant(HttpListener.class, "a");
        i.attribute("acceptor-threads","56");
        assertEquals(56,listener.acceptorThreads);

        // turning off those tests until we get a clear picture on how the @Configured
        // and @CagedBy can play together.
        /*System.out.println(bird.name);
        assertEquals(bird.name, "Caged tweety");

        System.out.println(lion.name);
        assertEquals(lion.name, "Caged kitty");     
        */
        // this test is breaking Hudson. will come back to this. Must be a classloader issue
        /* stack trace:

org.jvnet.hk2.component.ComponentException: Failed to create class test3.Main
	at com.sun.hk2.component.ConstructorWomb.create(ConstructorWomb.java:40)
	at com.sun.hk2.component.AbstractWombImpl.get(AbstractWombImpl.java:38)
	at com.sun.hk2.component.SingletonInhabitant.get(SingletonInhabitant.java:22)
	at com.sun.hk2.component.LazyInhabitant.get(LazyInhabitant.java:71)
	at com.sun.hk2.component.AbstractInhabitantImpl.get(AbstractInhabitantImpl.java:19)
	at org.jvnet.hk2.component.Habitat$1.get(Habitat.java:138)
	at java.util.AbstractList$Itr.next(AbstractList.java:422)
	at com.sun.enterprise.module.bootstrap.Main.launch(Main.java:346)
	at com.sun.enterprise.module.bootstrap.Main.launch(Main.java:265)
	at com.sun.enterprise.module.maven.RunMojo.execute(RunMojo.java:102)
	at org.apache.maven.plugin.DefaultPluginManager.executeMojo(DefaultPluginManager.java:443)
	at hudson.maven.agent.PluginManagerInterceptor.executeMojo(PluginManagerInterceptor.java:135)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.executeGoals(DefaultLifecycleExecutor.java:539)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.executeGoalWithLifecycle(DefaultLifecycleExecutor.java:480)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.executeGoal(DefaultLifecycleExecutor.java:459)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.executeGoalAndHandleFailures(DefaultLifecycleExecutor.java:311)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.executeTaskSegments(DefaultLifecycleExecutor.java:278)
	at org.apache.maven.lifecycle.DefaultLifecycleExecutor.execute(DefaultLifecycleExecutor.java:143)
	at org.apache.maven.lifecycle.LifecycleExecutorInterceptor.execute(LifecycleExecutorInterceptor.java:42)
	at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:334)
	at org.apache.maven.DefaultMaven.execute(DefaultMaven.java:125)
	at org.apache.maven.cli.MavenCli.main(MavenCli.java:280)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:585)
	at org.codehaus.classworlds.Launcher.launchEnhanced(Launcher.java:315)
	at org.codehaus.classworlds.Launcher.launch(Launcher.java:255)
	at hudson.maven.agent.Main.launch(Main.java:97)
	at hudson.maven.MavenBuilder.call(MavenBuilder.java:129)
	at hudson.maven.MavenModuleSetBuild$Builder.call(MavenModuleSetBuild.java:446)
	at hudson.maven.MavenModuleSetBuild$Builder.call(MavenModuleSetBuild.java:392)
	at hudson.remoting.UserRequest.perform(UserRequest.java:69)
	at hudson.remoting.UserRequest.perform(UserRequest.java:23)
	at hudson.remoting.Request$2.run(Request.java:200)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:417)
	at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:269)
	at java.util.concurrent.FutureTask.run(FutureTask.java:123)
	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:650)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:675)
	at java.lang.Thread.run(Thread.java:595)
Caused by: java.lang.ExceptionInInitializerError
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:494)
	at java.lang.Class.newInstance0(Class.java:350)
	at java.lang.Class.newInstance(Class.java:303)
	at com.sun.hk2.component.ConstructorWomb.create(ConstructorWomb.java:34)
	... 40 more
Caused by: java.lang.ClassCastException: com.sun.xml.stream.ZephyrWriterFactory
	at javax.xml.stream.XMLOutputFactory.newInstance(XMLOutputFactory.java:98)
	at test3.Main.<clinit>(Main.java:36)
	... 47 more
         */
//        {// test update
//            // TODO: we should have the add method in the config API
//            Dom pointConfig = new Dom(habitat, Dom.unwrap(jms), PointConfig.class);
//            pointConfig.attribute("x","100");
//            pointConfig.attribute("y","-100");
//            jms.getPoints().add((PointConfig)pointConfig.createProxy());
//
//            try {
//                // dump for visual inspection
//                DomDocument doc = Dom.unwrap(jms).document;
//                doc.writeTo(new IndentingXMLStreamWriter(xof.createXMLStreamWriter(System.out)));
//
//                // make sure it's there
//                DOMResult dr = new DOMResult();
//                dr.setNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
//                doc.writeTo(xof.createXMLStreamWriter(dr));
//                assertNotNull(new DOMReader().read((Document) dr.getNode()).selectSingleNode("//jms-host/point[@x='100'][@y='-100']"));
//            } catch (XMLStreamException e) {
//                throw new AssertionError(e);
//            } catch (ParserConfigurationException e) {
//                throw new AssertionError(e);
//            }
//        }

        testJmsCompanion();
    }

    private void testJmsCompanion() {
        JmsHost jms = find(foo.all, JmsHost.class);
        Dom dom = Dom.unwrap(jms);
        assertEquals(1,dom.companions().size());
        Inhabitant i = (Inhabitant)dom.companions().iterator().next();
        assertTrue(i.get() instanceof JmsCompanion);

        assertEquals(1,testCageBuilder.inhabitants.size());
    }

    private <T> T find(Collection<?> all, Class<T> type) {
        for (Object t : all) {
            if(type.isInstance(t))
                return type.cast(t);
        }
        return null;
    }
}
