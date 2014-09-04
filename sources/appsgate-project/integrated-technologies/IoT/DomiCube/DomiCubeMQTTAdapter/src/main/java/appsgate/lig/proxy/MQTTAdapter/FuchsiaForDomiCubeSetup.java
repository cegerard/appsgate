package appsgate.lig.proxy.MQTTAdapter;

import appsgate.lig.proxy.MQTTAdapter.importer.DomiCubeImporter;
import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FuchsiaForDomiCubeSetup {

    Instance fileBasedDiscovery = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport");

    Instance domiCubeImporter = instance()
            .of(DomiCubeImporter.class.getName());
            //.with("target").setto("(&(domicube.host=*)(domicube.port=*))");;


    Instance domicubeLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(domicube.host=*)(domicube.port=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=domiCubeImporter)");

}
