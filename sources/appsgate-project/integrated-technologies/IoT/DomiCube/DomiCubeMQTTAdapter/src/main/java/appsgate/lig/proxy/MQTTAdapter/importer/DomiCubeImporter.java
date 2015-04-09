package appsgate.lig.proxy.MQTTAdapter.importer;

import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import fr.imag.adele.apam.impl.ComponentBrokerImpl;
import org.apache.felix.ipojo.Factory;
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
import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.apache.felix.ipojo.Factory.INSTANCE_NAME_PROPERTY;

@Component
@Provides(specifications = {ImporterService.class, ImporterIntrospection.class})
public class DomiCubeImporter extends AbstractImporterComponent {

    private static final String DOMICUBE_APAM_ADAPTOR_INSTANCE="DomiCubeAdapterImpl";
    private static final String DOMICUBE_APAM_INSTANCE="DomiCubeImpl";

    private static final Logger logger = LoggerFactory.getLogger(DomiCubeImporter.class);

    private final BundleContext context;

    private final Executor executor= Executors.newCachedThreadPool();

    private ServiceReference serviceReference;

    @ServiceProperty(mandatory = false,value = "(&(id=*)(discovery.mdns.device.host=*)(discovery.mdns.device.port=*))")//(name = "target", value = "(&(discovery.knx.device.object=*)(appsgate.type=lamp))")
    private String target;

    @ServiceProperty(name = INSTANCE_NAME_PROPERTY)
    private String name;

    public DomiCubeImporter(BundleContext context){
        this.context=context;
    }

    @PostRegistration
    public void registration(ServiceReference serviceReference) {
        super.setServiceReference(serviceReference);
    }

    @Override
    protected void useImportDeclaration(final ImportDeclaration importDeclaration) throws BinderException {

        Runnable run=new Runnable() {
            @Override
            public void run() {
                try {

                    Implementation apamImpl = CST.apamResolver.findImplByName(null, DOMICUBE_APAM_ADAPTOR_INSTANCE);
                    Implementation apamDomicubeImpl = CST.apamResolver.findImplByName(null,DOMICUBE_APAM_INSTANCE);

                    DomiCubeWrapper dto=DomiCubeWrapper.create(importDeclaration);

                    Map propertiesAdaptor = new HashMap<String, String>();

                    logger.info("MQTT Instance adapter host {} port {}",dto.getHost(),dto.getPort().toString());

                    propertiesAdaptor.put(Factory.INSTANCE_NAME_PROPERTY, dto.getId()+"-adaptor");
                    propertiesAdaptor.put("host", dto.getHost());
                    propertiesAdaptor.put("port", dto.getPort().toString());

                    Instance apamInstance = apamImpl.createInstance(null, propertiesAdaptor);

                    Map propertiesDomicube = new HashMap<String, String>();

                    propertiesDomicube.put(Factory.INSTANCE_NAME_PROPERTY, dto.getId()+"-appsgate");
                    propertiesDomicube.put("activeFace", "-1");
                    propertiesDomicube.put("batteryLevel", "100");
                    propertiesDomicube.put("dimValue", "90.0");

                    Instance apamDomiCubeInstance = apamDomicubeImpl.createInstance(null,propertiesDomicube);

                    logger.info("Appsgate instance for domicube {} created", dto.getId());

                    DomiCubeImporter.this.handleImportDeclaration(importDeclaration);

                }catch(BinderException e){
                    e.printStackTrace();
                    logger.warn("Impossible to create Appsgate instance for domicube {}, reason {}", importDeclaration.getMetadata().get(ID),e.getMessage(),e);
                }
            }
        };

        executor.execute(run);

    }

    @Override
    protected void denyImportDeclaration(ImportDeclaration importDeclaration) throws BinderException {

        logger.info("Domicube has been removed.");

        unhandleImportDeclaration(importDeclaration);

        final DomiCubeWrapper dto=DomiCubeWrapper.create(importDeclaration);

        logger.info("Removing domicube instance {}",dto.getId());

        Runnable destroyApamInstance=new Runnable(){

            public void run(){

                logger.info("DomiCube {} removing..",dto.getId());
                ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(dto.getId()+"-adaptor");
                ((ComponentBrokerImpl)CST.componentBroker).disappearedComponent(dto.getId()+"-appsgate");
                logger.info("DomiCube {} removed",dto.getId());
            }

        };

        executor.execute(destroyApamInstance);

    }

    @Override
    public String getName() {
        return name;
    }
}
