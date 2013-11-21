package appsgate.lig.adapter.urc;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import appsgate.lig.adapter.urc.services.URCAdapterServices;

@Component(publicFactory=false)
@Instantiate(name="AppsgateURCAdapter")
@Provides(specifications = URCAdapterServices.class)
public class URCAdapter implements URCAdapterServices{

}
