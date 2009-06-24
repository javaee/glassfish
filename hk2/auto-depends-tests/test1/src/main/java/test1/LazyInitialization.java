package test1;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import com.sun.hk2.component.Holder;

/**
 * Tests lazy initialization patterns of hk2
 *
 * @author Jerome Dochez
 */
@Service
public class LazyInitialization extends Test {

    @Inject
    Holder<HeavyBean> holder;

    @Inject
    Habitat habitat;
    
    public void run() {
        assertNotNull(holder);

        // test injection access
        if (holder instanceof Inhabitant) {
            Inhabitant<HeavyBean> i = (Inhabitant<HeavyBean>) holder;
            assertFalse(i.isInstantiated());
        }

        // test API access.
        Inhabitant<HeavyBean> inhabitant = habitat.getInhabitantByType(HeavyBean.class);
        assertFalse(inhabitant.isInstantiated());
        System.out.println("Inhabitant isInitialized is " + inhabitant.isInstantiated());

        // instanciate the component.
        assertNotNull(holder.get());

        // ensure only instance
        assertTrue(holder.get()==inhabitant.get());

        // post-conditions testing
        assertTrue(inhabitant.isInstantiated());

        if (holder instanceof Inhabitant) {
            Inhabitant<HeavyBean> i = (Inhabitant<HeavyBean>) holder;
            assertTrue(i.isInstantiated());
        }
        System.out.println("Inhabitant isInitialized is " + inhabitant.isInstantiated());
    }
}
