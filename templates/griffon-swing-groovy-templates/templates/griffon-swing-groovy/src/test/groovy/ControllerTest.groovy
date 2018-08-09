package ${project_package}

import griffon.core.artifact.ArtifactManager
import griffon.test.core.GriffonUnitRule
import griffon.test.core.TestFor
import org.junit.Rule
import org.junit.Test

import javax.inject.Inject

import static org.awaitility.Awaitility.await
import static java.util.concurrent.TimeUnit.SECONDS
import static org.hamcrest.Matchers.greatherThan

@TestFor(${project_class_name}Controller)
class ${project_class_name}ControllerTest {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Inject
    private ArtifactManager artifactManager

    private ${project_class_name}Controller controller

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Test
    void executeClickAction() {
        // given:
        controller.model = artifactManager.newInstance(${project_class_name}Model)

        // when:
        controller.invokeAction('click')
        await().atMost(2, SECONDS).until({ -> model.getClickCount()}, greaterThan(0))

        // then:
        assert 1 == controller.model.clickCount
    }
}
