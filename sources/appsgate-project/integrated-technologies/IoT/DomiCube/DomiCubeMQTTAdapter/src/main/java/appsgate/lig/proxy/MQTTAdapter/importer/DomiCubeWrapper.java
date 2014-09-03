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
    public static final String HOST="domicube.host";
    public static final String PORT="domicube.port";
    public static final String TOPIC_FACE="domicube.topic.face";
    public static final String TOPIC_BATTERY="domicube.topic.battery";
    public static final String TOPIC_DIM="domicube.topic.dim";

    private String id;
    private String host;
    private Integer port;
    private String topicFace;
    private String topicBattery;
    private String topicDim;

    private DomiCubeWrapper(){

    }

    private static Filter buildFilter() {
        Filter filter;
        String stringFilter = String.format("(&(%s=*)(%s=*)(%s=*)(%s=*)(%s=*)(%s=*))",
                ID,HOST,PORT,TOPIC_FACE,TOPIC_BATTERY,TOPIC_DIM);
        try {
            System.out.println("filtering with:"+stringFilter);
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
        wrapper.topicFace = (String) metadata.get(TOPIC_FACE);
        wrapper.topicBattery = (String) metadata.get(TOPIC_BATTERY);
        wrapper.topicDim = (String) metadata.get(TOPIC_DIM);

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

    public String getTopicFace() {
        return topicFace;
    }

    public String getTopicBattery() {
        return topicBattery;
    }

    public String getTopicDim() {
        return topicDim;
    }
}
