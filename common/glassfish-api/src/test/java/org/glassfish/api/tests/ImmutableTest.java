package org.glassfish.api.tests;

import org.junit.Test;
import junit.framework.Assert;
import org.glassfish.api.event.EventTypes;

public class ImmutableTest {


    @Test
    public void test1() {

        EventTypes evt1 = EventTypes.create("foo");
        EventTypes evt2 = EventTypes.create("foo");
        EventTypes evt3 = EventTypes.create("foo34");

        Assert.assertNotSame(evt1, evt3);
        Assert.assertEquals(evt1, evt2);
        
        Assert.assertTrue(evt1==evt2);
        Assert.assertFalse(evt1==evt3);
    }
    
}