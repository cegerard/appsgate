package appsgate.lig.test.pax;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestWebServicesAppsgate.class,TestUpnPAppsgate.class,TestCoreAppsgate.class,TestAutoDeployAppsgate.class})
public class AllTests {

}
