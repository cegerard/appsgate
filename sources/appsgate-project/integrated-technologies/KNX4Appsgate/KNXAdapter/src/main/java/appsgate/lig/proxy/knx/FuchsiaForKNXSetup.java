package appsgate.lig.proxy.knx;

import appsgate.lig.proxy.knx.importer.AppsgateLampImporter;
import appsgate.lig.proxy.knx.importer.AppsgateSwitchImporter;
import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.importer.knx.KNXDeviceLightImporter;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FuchsiaForKNXSetup {

    Instance fileBasedDiscovery = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport");

    Instance knxImporter = instance()
            .of(KNXDeviceLightImporter.class.getName())
            .with("target").setto("(&(discovery.knx.device.addr=*)(!(discovery.knx.device.object=*)))");

    Instance knxLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.knx.device.addr=*)(!(discovery.knx.device.object=*)))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=knxImporter)");

    Instance appsgateKNXLampImporter = instance()
            .of(AppsgateLampImporter.class.getName())
            .with("target").setto("(&(discovery.knx.device.object=*)(appsgate.type=lamp))");

    Instance appsgateKNXOnOffActuatorImporter = instance()
            .of(AppsgateSwitchImporter.class.getName())
            .with("target").setto("(&(discovery.knx.device.object=*)(appsgate.type=socket))");

    /**
     * Generic Linker
    Instance appsgateKNXLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.knx.device.object=*)(appsgate.type=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=appsgateKNX*)");
     **/

    /** One linker for each importer (in case of problems sharing linkers **/
    Instance appsgateKNXLampLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.knx.device.object=*)(appsgate.type=lamp))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=appsgateKNXLampImporter)");

    Instance appsgateKNXOnOffActuatorLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.knx.device.object=*)(appsgate.type=socket))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=appsgateKNXOnOffActuatorImporter)");



}
