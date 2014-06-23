package appsgate.lig.proxy.PhilipsHUE;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;


import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FuchsiaForPhilipsSetup {

    Instance philipsBridgeImporter = instance()
            .of("org.ow2.chameleon.fuchsia.importer.philipshue.PhilipsHueBridgeImporter")
            .with("target").setto("(discovery.philips.bridge.type=*)");

    Instance philipsLinkerBridge = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.philips.bridge.type=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=philipsBridgeImporter)");

    Instance philipsLampImporter = instance()
            .of("appsgate.lig.proxy.PhilipsHUE.importer.PhilipsHueImporter")
            .with("target").setto("(discovery.philips.device.name=*)");

    Instance philipsLinkerLamp = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.philips.device.name=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=philipsLampImporter)");


}
