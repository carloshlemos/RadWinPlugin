package br.ueg.RadWin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class RadWinPlugin extends CordovaPlugin {

    public boolean isSynch(String action) {
        if (action.equals("getOID")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        if (action.equals("getOID")) {

            String OID = this.snmpGet("10.0.0.122", "public", data.getString(0), callbackContext);

            if (OID != null) {
                JSONObject JSONresult = new JSONObject();
                try {
                    JSONresult.put("oid", OID);
                    PluginResult r = new PluginResult(PluginResult.Status.OK,
                            JSONresult);
                    callbackContext.success(OID);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    return true;
                } catch (JSONException jsonEx) {
                    PluginResult r = new PluginResult(
                            PluginResult.Status.JSON_EXCEPTION);
                    callbackContext.error("error");
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    return true;
                }
            }
        }
        return false;
    }

    /*
     *  SnmpGet method

     * return Response for given OID from the Device.

     */
    private String snmpGet(String strAddress, String community, String strOID, CallbackContext callbackContext) {
        String str = "";

        try {
            OctetString community1 = new OctetString(community);
            strAddress = strAddress + "/" + 161;

            Address targetaddress = new UdpAddress(strAddress);
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            CommunityTarget comtarget = new CommunityTarget();
            comtarget.setCommunity(community1);

            comtarget.setVersion(SnmpConstants.version1);
            comtarget.setAddress(targetaddress);
            comtarget.setRetries(2);

            comtarget.setTimeout(5000);
            PDU pdu = new PDU();

            ResponseEvent response;
            Snmp snmp;

            pdu.add(new VariableBinding(new OID(strOID)));
            pdu.setType(PDU.GET);

            snmp = new Snmp(transport);
            response = snmp.get(pdu, comtarget);

            if (response != null) {
                if (response.getResponse().getErrorStatusText().equalsIgnoreCase("Success")) {

                    PDU pduresponse = response.getResponse();
                    str = pduresponse.getVariableBindings().firstElement().toString();

                    if (str.contains("=")) {
                        int len = str.indexOf("=");
                        str = str.substring(len + 1, str.length());
                    }
                }
            } else {
                callbackContext.error("Feeling like a TimeOut occured.");
            }

            snmp.close();

        } catch (Exception e) {
            callbackContext.error("Error: " + e.getMessage());
        }
        return str;
    }

}
