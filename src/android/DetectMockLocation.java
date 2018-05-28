package com.danishin.detectmocklocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class DetectMockLocation extends CordovaPlugin {
  private static final String TAG = "DetectMockLocation";
  
  /* success responses */
  private static final String MOCKED = "mocked";
  private static final String NOT_MOCKED = "not_mocked";
  
  /* error responses */
  private static final String NO_PERMISSION = "no_permission";
  
  private CordovaInterface cordova;
  
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
      checkMockLocationIsEnabled(callbackContext);
    } else {
      checkLocationIsFromMockProvider(callbackContext);
    }
  }
  
  private void checkMockLocationIsEnabled(CallbackContext callbackContext) {
    boolean allowMockLocationIsEnabled = !android.provider.Settings.Secure.getString(cordova.getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
    
    Log.d(TAG, "AllowMockLocationIsEnabled: " + allowMockLocationIsEnabled);
    
    callbackContext.success(allowMockLocationIsEnabled ? MOCKED : NOT_MOCKED);
  }
  
  @TargetApi(18)
  private void checkLocationIsFromMockProvider(final CallbackContext callbackContext) {
    boolean hasPermission = ActivityCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    
    if (!hasPermission) {
      callbackContext.error(NO_PERMISSION);
    } else {
      LocationManager locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
      assert locationManager != null;
      
      LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
          Log.d(TAG, String.format("onLocationChanged: %s", location.toString()));
          
          boolean locationIsFromMockProvider = location.isFromMockProvider();
          
          Log.d(TAG, "LocationIsFromMockProvider: " + locationIsFromMockProvider);
          
          callbackContext.success(locationIsFromMockProvider ? MOCKED : NOT_MOCKED);
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras) {
          Log.d(TAG, String.format("onStatusChanged: %s, %d, %s", provider, status, extras.toString()));
        }
        
        public void onProviderEnabled(String provider) {
          Log.d(TAG, "onProviderEnabled: " + provider);
        }
        
        public void onProviderDisabled(String provider) {
          Log.d(TAG, "onProviderDisabled: " + provider);
        }
      };
      
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
  }
}