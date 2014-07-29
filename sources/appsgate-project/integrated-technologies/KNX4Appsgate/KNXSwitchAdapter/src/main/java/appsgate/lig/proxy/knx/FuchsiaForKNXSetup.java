package appsgate.lig.proxy.knx;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.importer.knx.KNXDeviceLightImporter;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FuchsiaForKNXSetup {

    Instance fileBasedDiscoveryImport = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport")
            .named("FileBasedDiscoveryImport");

    Instance knxImporter = instance()
            .of(KNXDeviceLightImporter.class.getName())
            .with("target").setto("(discovery.knx.device.addr=*)");


    Instance knxLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(discovery.knx.device.addr=*)")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=knxImporter)");

}
