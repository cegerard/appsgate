package appsgate.lig.enocean.ubikit.adapter;

import appsgate.lig.manager.client.communication.service.send.SendWebsocketsService;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubikit.PhysicalEnvironmentItem;
import org.ubikit.pem.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thibaud on 08/07/2014.
 */
public class PEMSimpleListener implements NewItemEvent.Listener, ItemAddedEvent.Listener, ItemAddingFailedEvent.Listener, ItemDroppedEvent.Listener, ItemPropertiesUpdatedEvent.Listener {

    private static Logger logger = LoggerFactory.getLogger(UbikitAdapter.class);

    private SendWebsocketsService sendToClientService;
    private UbikitAdapter adapter;

    public PEMSimpleListener(SendWebsocketsService sendToClientService, UbikitAdapter adapter) {
        this.sendToClientService=sendToClientService;
        this.adapter = adapter;
    }


    @Override
    public void onEvent(ItemPropertiesUpdatedEvent itemPropertiesUpdatedEvent) {
        logger.debug("!ItemPropertiesUpdatedEvent! from "
                + itemPropertiesUpdatedEvent.getSourceItemUID());
    }

    @Override
    public void onEvent(NewItemEvent newItEvent) {
        logger.debug("!NewItemEvent! from " + newItEvent.getSourceItemUID()
                + " to " + newItEvent.getPemUID() + ", type="
                + newItEvent.getItemType());

        if (!adapter.containSid(newItEvent.getSourceItemUID())) {


            adapter.addSidToInstance(newItEvent.getSourceItemUID(), null);

            NewItemEvent.CapabilitySelection cs = newItEvent
                    .doesCapabilitiesHaveToBeSelected();

            if (cs == NewItemEvent.CapabilitySelection.NO) {
                adapter.validateItem(newItEvent.getSourceItemUID(), null, false);

            } else if (cs == NewItemEvent.CapabilitySelection.SINGLE) {

                String[] capabilities = newItEvent.getCapabilities();
                int capListLength = capabilities.length;
                int i = 0;
                ArrayList<EnOceanProfiles> tempCapList = new ArrayList<EnOceanProfiles>();
                while (i < capListLength) {
                    tempCapList.add(EnOceanProfiles
                            .getEnOceanProfile(capabilities[i]));
                    i++;
                }
                adapter.addTempEventCapability(newItEvent.getSourceItemUID(),
                        tempCapList);
                JSONObject onEventMSG = new JSONObject();
                JSONObject newUndefinedJSON = new JSONObject();
                try {
                    newUndefinedJSON.put("id", newItEvent.getSourceItemUID());
                    newUndefinedJSON.put("capabilities", adapter.getItemCapabilities(newItEvent.getSourceItemUID()));
                    onEventMSG.put("newUndefinedSensor", newUndefinedJSON);
                    onEventMSG.put("TARGET", UbikitAdapter.CONFIG_TARGET);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendToClientService.send(onEventMSG.toString());

            } else if (cs == NewItemEvent.CapabilitySelection.MULTIPLE) {
                logger.error("Multiple capability not supported yet for "
                        + newItEvent.getSourceItemUID() + " to "
                        + newItEvent.getPemUID() + ", type="
                        + newItEvent.getItemType());
            }
        }

    }


    @Override
    public void onEvent(ItemAddedEvent addItEvent) {
        logger.debug("!ItemAddedEvent! from "
                + addItEvent.getSourceItemUID() + " to "
                + addItEvent.getPemUID() + ", type "
                + addItEvent.getItemType());

        EnOceanProfiles ep = EnOceanProfiles.EEP_00_00_00;
        Implementation impl = null;
        Map<String, String> properties = new HashMap<String, String>();

        if (addItEvent.getItemType().equals(PhysicalEnvironmentItem.Type.SENSOR)
                || addItEvent.getItemType()
                .equals(PhysicalEnvironmentItem.Type.SENSOR_AND_ACTUATOR)) {
            if (addItEvent.getCapabilities().length == 1) {
                String capabilitie = addItEvent.getCapabilities()[0];
                ep = EnOceanProfiles.getEnOceanProfile(capabilitie);
            } else {
                ArrayList<EnOceanProfiles> profilesList = adapter.getTempEventCapability(
                        addItEvent.getSourceItemUID());
                // TODO manage for multiple profiles sensors.
                ep = profilesList.iterator().next();
                adapter.removeTempEventCapability(addItEvent
                        .getSourceItemUID());
            }
            properties.put("isPaired", "true");

        } else if (addItEvent.getItemType().equals(PhysicalEnvironmentItem.Type.ACTUATOR)) {
            String capabilitie = addItEvent.getCapabilities()[0];
            ep = EnOceanProfiles.getEnOceanProfile(capabilitie);
            properties.put("isPaired", "false");
        }

        impl = CST.apamResolver.findImplByName(null,
                ep.getApAMImplementation());

        properties.put("deviceName", ep.getUserFriendlyName());
        properties.put("deviceId", addItEvent.getSourceItemUID());
        properties.put("deviceType", ep.name());

        Instance createInstance = impl.createInstance(null, properties);
        adapter.addSidToInstance(addItEvent.getSourceItemUID(), createInstance);

        // Notify configuration UI
        JSONObject onEventMSG = new JSONObject();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("id", addItEvent.getSourceItemUID());
            jsonObj.put("type", addItEvent.getItemType().name());
            jsonObj.put("deviceType", ep.name());
            jsonObj.put("paired", properties.get("isPaired"));
            onEventMSG.put("newObject", jsonObj);
            onEventMSG.put("TARGET", UbikitAdapter.CONFIG_TARGET);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendToClientService.send(onEventMSG.toString());
    }

    @Override
    public void onEvent(ItemAddingFailedEvent addFailedEvent) {
        logger.debug("!ItemAddingFailedEvent! from "
                + addFailedEvent.getSourceItemUID() + " Error Code = "
                + addFailedEvent.getErrorCode() + ", Reason = "
                + addFailedEvent.getReason());
        JSONObject onEventMSG = new JSONObject();
        JSONObject pairingFailedMsg = new JSONObject();
        try {
            pairingFailedMsg.put("id", addFailedEvent.getSourceItemUID());
            pairingFailedMsg.put("capabilities",
                    adapter.getItemCapabilities(addFailedEvent.getSourceItemUID()));
            pairingFailedMsg.put("code", addFailedEvent.getErrorCode());
            pairingFailedMsg.put("reason", addFailedEvent.getReason());
            onEventMSG.put("pairingFailed", pairingFailedMsg);
            onEventMSG.put("TARGET", UbikitAdapter.CONFIG_TARGET);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendToClientService.send(onEventMSG.toString());
    }

    @Override
    public void onEvent(ItemDroppedEvent itemDroppedEvent) {
        logger.debug("!ItemDroppedEvent! from "
                + itemDroppedEvent.getSourceItemUID());
    }


}
