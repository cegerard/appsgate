package appsgate.lig.proxy.knx.importer;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.PostRegistration;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.core.component.AbstractImporterComponent;
import org.ow2.chameleon.fuchsia.core.component.ImporterIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ImporterService;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.importer.knx.device.iface.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component
@Provides(specifications = {ImporterService.class, ImporterIntrospection.class})
public class AppsgateLampImporter extends AbstractImporterComponent {

    private static final String APAM_LAMP_IMPL="KNXLightImpl";
    private static final Logger logger = LoggerFactory.getLogger(AppsgateLampImporter.class);

        private final BundleContext context;

        private ServiceReference serviceReference;

        @ServiceProperty(name = "target", value = "(&(discovery.knx.device.object=*)(appsgate.type=lamp))")
        private String filter;

        @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
        private String name;

        public AppsgateLampImporter(BundleContext context) {
            this.context = context;
        }

        @PostRegistration
        public void registration(ServiceReference serviceReference) {
            super.setServiceReference(serviceReference);
        }

        private String generateAppsgateDeviceInstanceName(String deviceId){
            return "appsgate-"+deviceId;
        }

        @Override
        protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

            logger.info("****************** Appsgate Lamp importer was invoked *************************");

            final Switch fuchsiaSwitch=(Switch)importDeclaration.getMetadata().get("discovery.knx.device.object");

            final String appsgateDeviceInstance=generateAppsgateDeviceInstanceName(fuchsiaSwitch.getId());

            logger.info("Found new KNX instance, creating Appsgate instance to represent the device {} ..",appsgateDeviceInstance);

            Thread t1=new Thread(){

                public void run() {

                    Implementation apamImpl = CST.componentBroker.getImpl(APAM_LAMP_IMPL);

                    Map properties = new HashMap<String, String>();

                    properties.put("instance.name", appsgateDeviceInstance);
                    properties.put("deviceName", fuchsiaSwitch.getId());
                    properties.put("deviceId", fuchsiaSwitch.getId());
                    properties.put("deviceType", "7");

                    Instance apamInstance = apamImpl.createInstance(null, properties);

                    logger.info("Appsgate instance {} created", appsgateDeviceInstance);
                }
            };

            super.handleImportDeclaration(importDeclaration);

            t1.setDaemon(true);
            t1.start();

        }

        @Override
        protected void denyImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

            logger.info("******************  Appsgate Switch deny importer was invoked **************");

            final String id=importDeclaration.getMetadata().get("id").toString();

            Thread t1=new Thread(){

                public void run(){

                    String appsgateInstanceName=generateAppsgateDeviceInstanceName(id);
                    logger.info("KNX device removed, removing instance {} ..",appsgateInstanceName);
                    ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(appsgateInstanceName);
                    logger.info("Appsgate instance {} removed",appsgateInstanceName);
                }

            };

            t1.setDaemon(true);
            t1.start();

            unhandleImportDeclaration(importDeclaration);
        }


        public String getName() {
            return name;
        }

}
