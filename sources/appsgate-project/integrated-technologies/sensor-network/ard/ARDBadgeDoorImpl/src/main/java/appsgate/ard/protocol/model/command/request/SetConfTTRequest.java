package appsgate.ard.protocol.model.command.request;

import appsgate.ard.protocol.model.command.ARDRequest;
import org.json.JSONException;

public class SetConfTTRequest extends ARDRequest {

    public SetConfTTRequest(String codeSite,String mainNumber,String backupNumber) throws JSONException {
        super(0, "set_conf_tt");
        json.put("code_sire",codeSite);
        json.put("main_number",mainNumber);
        json.put("backup_number",backupNumber);
    }

}
