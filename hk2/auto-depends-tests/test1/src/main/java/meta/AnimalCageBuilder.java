package meta;

import cagedby.Lion;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.annotations.Service;

/**
 * CageBuilder should be called for anything annotated with @Prisoner.
 * @author Jerome Dochez
 */
@Service
public class AnimalCageBuilder implements CageBuilder {
    public void onEntered(Inhabitant<?> i) {
        System.out.println("Meta Animal builder got " + i.get());
        Animal a = Animal.class.cast(i.get());
        a.setName("Caged " + a.getName());
    }
}

