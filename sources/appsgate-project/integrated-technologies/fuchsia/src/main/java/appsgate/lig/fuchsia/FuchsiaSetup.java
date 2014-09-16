package appsgate.lig.fuchsia;

import appsgate.lig.proxy.MQTTAdapter.importer.DomiCubeImporter;
import appsgate.lig.proxy.knx.importer.AppsgateLampImporter;
import appsgate.lig.proxy.knx.importer.AppsgateSwitchImporter;
import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import org.ow2.chameleon.fuchsia.importer.knx.KNXDeviceLightImporter;

import static org.apache.felix.ipojo.configuration.Instance.instance;

@Configuration
public class FuchsiaSetup {

        Instance fileBasedDiscovery = instance()
                .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport");

        Instance domicubeMDNSDiscovery = instance()
            .of("org.ow2.chameleon.fuchsia.discovery.mdns.DNSSDDiscovery")
                .with("dnssd.service.type").setto("_http._tcp.local.")
                .with("dnssd.service.name").setto("domicube");

        /**
         * Domicube
         */

        Instance domiCubeImporter = instance()
                .of(DomiCubeImporter.class.getName());

        Instance domicubeLinker = instance()
                .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
                .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.mdns.device.host=*)(discovery.mdns.device.port=*))")
                .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=domiCubeImporter)");

        /**
         * Philips Hue
         */

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

        /**
         * KNX
         */

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

        Instance appsgateKNXShutterActuatorImporter = instance()
            .of(AppsgateSwitchImporter.class.getName())
            .with("target").setto("(&(discovery.knx.device.object=*)(appsgate.type=shutter))");
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

        Instance appsgateKNXShutterActuatorLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(discovery.knx.device.object=*)(appsgate.type=shutter))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=appsgateKNXShutterActuatorImporter)");

        /**
         * ARD
         */

        Instance ardBridgeImporter = instance()
                .of("appsgate.ard.protocol.fuchsia.ARDBrigdeImporter")
                .with("target").setto("(ard.bridge.ip=*)");

        Instance ardBridgeLinkerBridge = instance()
                .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
                .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(ard.bridge.ip=*)")
                .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=ardBridgeImporter)");

}