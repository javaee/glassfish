import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;

import devtests.deployment.util.AnnotationTest;

public class WebAnnotationTest extends AnnotationTest {
    public WebAnnotationTest(String name) {
        super(name);
        type = ModuleType.WAR;
        componentClassNames.add("test.ServletTest");
    }
}
