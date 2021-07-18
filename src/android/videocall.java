package cordova.plugin.videocall.videocall;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import src.cordova.plugin.videocall.RoomActivity.RoomActivity;

/**
 * This class echoes a string called from JavaScript.
 */
public class videocall extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            this.coolMethod(args.getString(0), args.getString(1), args.getString(2), args.getString(3), callbackContext);
            return true;
        }else if(action.equals("retriveData")){
            String dataReceived = args.getString(0);
            return true;
        }
        return false;
    }

    private void coolMethod(String roomName, String identity, String token, String id, CallbackContext callbackContext) {
      Intent intent = new Intent(this.cordova.getActivity(), RoomActivity.class);
      intent.putExtra("room", roomName);
      intent.putExtra("identity", "identity");
      intent.putExtra("token", token);
      intent.putExtra("id", id);
      this.cordova.getActivity().startActivity(intent);
        /*if (message != null && message.length() > 0) {
            callbackContext.success(message);

        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }*/
    }
}
