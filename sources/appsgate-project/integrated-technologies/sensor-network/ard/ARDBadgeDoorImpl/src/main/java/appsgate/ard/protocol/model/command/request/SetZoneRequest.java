package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

import java.util.Collection;

public class SetZoneRequest extends ARDRequest {

    public SetZoneRequest(Integer zoneIndex, String name, Integer seqOnIdx, Integer seqOnTT, Integer seqOffIdx, Integer seqOffTT,
                          Integer seqEjectIdx, Integer seqEjectTT, Collection<Integer> points) throws JSONException {
        super(0, "set_zone");
        json.put("zone_idx", zoneIndex);
        json.put("name", name);
        json.put("seq_on_idx", seqOnIdx);
        json.put("seq_on_tt", seqOnTT);
        json.put("seq_off_idx", seqOffIdx);
        json.put("seq_off_tt", seqOffTT);
        json.put("seq_eject_idx", seqEjectIdx);
        json.put("seq_eject_tt", seqEjectTT);
        json.put("points", points);
    }

}
