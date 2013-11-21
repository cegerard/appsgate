package appsgate.ard;


import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;

import static org.apache.felix.ipojo.configuration.Instance.instance;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY;
import static org.ow2.chameleon.fuchsia.core.ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY;

@Configuration
public class ConfigureARD {

    Instance ARDSwitchLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(ard.switch.ip=*)(ard.switch.port=*))")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=ARDSwitchImporter)");

    Instance ARDSwitchImporter = instance()
            .of("ARDSwitchImporterFactory")
            .with("target").setto("(&(ard.switch.ip=*)(ard.switch.port=*))");


      /*
    Instance ARDDoorLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(FILTER_IMPORTDECLARATION_PROPERTY).setto("(ard.door.id=*)")
            .with(FILTER_IMPORTERSERVICE_PROPERTY).setto("(address=*)");

    Instance ARDDoorImporter = instance()
            .of("ARDDoorImporterFactory")
            .with("target").setto("(ard.door.id=*)");
    */
}
