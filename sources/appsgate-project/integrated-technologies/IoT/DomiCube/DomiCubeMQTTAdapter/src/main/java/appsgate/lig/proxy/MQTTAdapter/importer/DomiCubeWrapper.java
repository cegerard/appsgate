package appsgate.lig.proxy.MQTTAdapter.importer;

import org.osgi.framework.Filter;
import org.ow2.chameleon.fuchsia.core.FuchsiaUtils;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import org.ow2.chameleon.fuchsia.core.exceptions.InvalidFilterException;

import java.util.Map;

import static org.ow2.chameleon.fuchsia.core.declaration.Constants.ID;

public class DomiCubeWrapper {

    private static Filter declarationFilter = buildFilter();
    public static final String HOST="discovery.mdns.device.host";
    public static final String PORT="discovery.mdns.device.port";

    private String id;
    private String host;
    private Integer port;

    private DomiCubeWrapper(){

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*))",
                ID,HOST,PORT);
        try {
            filter = FuchsiaUtils.getFilter(stringFilter);
        } catch (InvalidFilterException e) {
            throw new IllegalStateException(e);
        }
        return filter;
    }

    public static DomiCubeWrapper create(ImportDeclaration importDeclaration) throws BinderException {

        Map<String, Object> metadata = importDeclaration.getMetadata();

        if (!declarationFilter.matches(metadata)) {
            throw new BinderException("Not enough information in the metadata to be used by the DomiCube importer");
        }

        DomiCubeWrapper wrapper = new DomiCubeWrapper();

        wrapper.id = (String) metadata.get(ID);
        wrapper.host = (String) metadata.get(HOST);
        wrapper.port = Integer.parseInt(metadata.get(PORT).toString());

        return wrapper;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
