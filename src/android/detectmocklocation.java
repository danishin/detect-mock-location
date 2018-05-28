package com.danishin.detectmocklocation;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class detectmocklocation extends CordovaPlugin {
  private static final String TAG = "DetectMockLocation";
  
  private static CordovaInterface cordova;
  
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    
    this.cordova = cordova;
    
    Log.d(TAG, "Initialized");
  }
  
 @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("check")) {
      this.check(callbackContext);
        return true;
    }
    
    return false;
  }
  
  private void check(CallbackContext callbackContext) {
    if (Build.VERSION.SDK_INT < 18) {
      /* Build version < 18 */
      boolean allowMockLocationIsEnabled = !android.provider.Settings.Secure.getString(_cordovaActivity.getApplicationContext().getContentResolver(), Secure.ALLOW_MOCK_LOCATION).equals("0");
      
      Log.d(TAG, "AllowMockLocationIsEnabled: " + allowMockLocationIsEnabled)
      
      callbackContext.success(allowMockLocationIsEnabled);
    } else {
      /* Build version >= 18 */
      LocationManager locationManager = (LocationManager) this.cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
      
      LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
          Log.d(TAG, String.format("onLocationChanged: %s", location.toString()))
          
          boolean locationIsFromMockProvider = location.isFromMockProvider();
          
          Log.d(TAG, "LocationIsFromMockProvider: " + locationIsFromMockProvider)
          
          callbackContext.success(locationIsFromMockProvider);
        }
    
        public void onStatusChanged(String provider, int status, Bundle extras) {
          Log.d(TAG,  String.format("onStatusChanged: %s, %d, $s", provider, status, extras.toString()));
        }
    
        public void onProviderEnabled(String provider) {
          Log.d(TAG, "onProviderEnabled: " + provider)
        }
    
        public void onProviderDisabled(String provider) {
          Log.d(TAG, "onProviderDisabled: " + provider)
        }
      };
      
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
  }
}