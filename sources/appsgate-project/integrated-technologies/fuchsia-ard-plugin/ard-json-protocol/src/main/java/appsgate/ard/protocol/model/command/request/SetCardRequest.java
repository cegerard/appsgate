package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class SetCardRequest extends ARDRequest {

    public SetCardRequest(Integer cardId,String cardNum,String name,Integer catIdx,String validityStart,String validityEnd,Boolean validity) throws JSONException {

        super(0, "set_card");
        json.put("card_idx",cardId);
        json.put("card_num",cardNum);
        json.put("name",name);
        json.put("cat_idx",catIdx);
        json.put("validity_start",validityStart);
        json.put("validity_end",validityEnd);
        json.put("validity",validity);
    }

}
