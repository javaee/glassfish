import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;

import devtests.deployment.util.AnnotationTest;

public class AppClientAnnotationTest extends AnnotationTest {
    public AppClientAnnotationTest(String name) {
        super(name);
        type = ModuleType.CAR;
        componentClassNames.add("test.Client");
    }
}
