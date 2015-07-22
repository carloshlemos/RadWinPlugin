package br.ueg.RadWin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class RadWinPlugin extends CordovaPlugin {

    public boolean isSynch(String action) {
        if (action.equals("getOID")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("getOID")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {

            return false;

        }
    }
}
