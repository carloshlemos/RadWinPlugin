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
    public PluginResult execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getOID")) {

            JSONObject params = new JSONObject(data.getString(0));

            String address = params.getString("address");
            String community = params.getString("community");
            String strOIDLatitude = params.getString("strOIDLatitude");
            String strOIDLongitude = params.getString("strOIDLongitude");

            //String OID = "Hello, " + address + ", " + community + ", " + strOID;
            String OIDLatitude = this.snmpGet(address, community, strOIDLatitude, callbackContext);
            String OIDLongitude = this.snmpGet(address, community, strOIDLongitude, callbackContext);

            if (OIDLatitude != null && OIDLongitude != null) {
                JSONObject JSONresult = new JSONObject();
                try {
                    JSONresult.put("OIDLatitude", OIDLatitude);
                    JSONresult.put("OIDLongitude", OIDLongitude);
                    PluginResult r = new PluginResult(PluginResult.Status.OK,
                            JSONresult);
                    return r;
                } catch (JSONException jsonEx) {
                    PluginResult r = new PluginResult(
                            PluginResult.Status.JSON_EXCEPTION);
                    return r;
                }
            }
        }
        return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
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
                PluginResult r = new PluginResult(
                        PluginResult.Status.JSON_EXCEPTION);
                callbackContext.error("Feeling like a TimeOut occured.");
                r.setKeepCallback(true);
                callbackContext.sendPluginResult(r);
                return null;
            }

            snmp.close();

        } catch (Exception e) {
            PluginResult r = new PluginResult(
                    PluginResult.Status.JSON_EXCEPTION);
            callbackContext.error("Error: " + e.getMessage());
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return null;
        }
        return str;
    }

}
