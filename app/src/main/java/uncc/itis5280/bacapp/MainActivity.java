package uncc.itis5280.bacapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
import BACtrackAPI.Commands.BLECommand;
import BACtrackAPI.Constants.BACTrackDeviceType;
import BACtrackAPI.Constants.BACtrackUnit;
import BACtrackAPI.Constants.Errors;
import BACtrackAPI.Exceptions.BluetoothLENotSupportedException;
import BACtrackAPI.Exceptions.BluetoothNotEnabledException;
import BACtrackAPI.Exceptions.LocationServicesNotEnabledException;
import uncc.itis5280.bacapp.databinding.ActivityMainBinding;
import uncc.itis5280.bacapp.ui.bac.BACTrackFragment;

public class MainActivity extends AppCompatActivity implements BACTrackFragment.IListener {
    private ActivityMainBinding binding;
    NavController navController;

    private static String TAG = "JWT";
    private static final byte PERMISSIONS_FOR_SCAN = 100;

    private BACtrackAPI mAPI;
    private Context context;
    private boolean waitingForBlow;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bac_track, R.id.navigation_history, R.id.navigation_profile)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        requestBlePermissions(this, 001);
    }

    // Bluetooth Permissions
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }

    // Initiate BAC Tracker
    protected void initBacTrackAPI() {
        String apiKey = "712d405434c64ec0aec98949490172";

        try {
            Log.d(TAG, "initBacTrackAPI: " + this.toString());
            mAPI = new BACtrackAPI(this, mCallbacks, apiKey);
            context = this;
        } catch (BluetoothLENotSupportedException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing bs");
            e.printStackTrace();
            setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
        } catch (BluetoothNotEnabledException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing be");
            e.printStackTrace();
            setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
        } catch (LocationServicesNotEnabledException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing");
            e.printStackTrace();
            setStatus(R.string.TEXT_ERR_LOCATIONS_NOT_ENABLED);
        }
    }

    protected boolean isBluetoothEnabled() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return (bluetoothManager.getAdapter().isEnabled());
    }

    protected static boolean areLocationServicesAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        }
        else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    @Override
    public void connectNearestClicked() {
        if (mAPI == null) {
            initBacTrackAPI();
        }

        if (mAPI != null && !mAPI.isConnected()) {
            setStatus("Connecting...");
            mAPI.connectToNearestBreathalyzer();
        } else if (mAPI.isConnected()){
            mAPI.disconnect();
        }
    }

    @Override
    public void startBacTestClicked() {
        boolean result = false;

        if (mAPI != null) {
            bundle.putBoolean("countdownVisibility", false);
            bundle.putBoolean("blowVisibility", false);
            bundle.putBoolean("analyzeVisibility", false);
            bundle.putBoolean("resultVisibility", false);
            result = mAPI.startCountdown();
        }

        if (!result)
            Log.e(TAG, "mAPI.startCountdown() failed");
        else
            Log.d(TAG, "Blow process start requested");
    }

    private void setStatus(int resourceId) {
        String status = this.getResources().getString(resourceId);
        Log.d(TAG, "setStatus: " + status);
        setStatus(status);
    }

    private void setStatus(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(mAPI.isConnected()) {
//                    bundle.putString("connectionStatus", "Connected");
//                    bundle.putString("buttonStatus", "DISCONNECT DEVICE");
//                } else {
//                    bundle.putString("connectionStatus", status);
//                    bundle.putString("buttonStatus", "CONNECT TO NEAREST DEVICE");
//                }

                if(!mAPI.isConnected()) {
                    bundle.putString("connectionStatus", status);
                    bundle.putString("buttonStatus", "CONNECT TO NEAREST DEVICE");
                }

                if(status.contains("Countdown")) {
                    bundle.putBoolean("countdownVisibility", true);
                    bundle.putString("direction", "Wait...");
                    bundle.putString("countdownStatus", status.substring(11));
                } else if (status.contains("Blow")) {
                    bundle.putString("countdownStatus", "0");
                    bundle.putString("direction", "Blow Now!");
                    bundle.putBoolean("blowVisibility", true);
                    bundle.putString("blowStatus", status.substring(5));
                } else if (status.contains("Analyzing")) {
                    bundle.putString("direction", "Wait...");
                    bundle.putBoolean("analyzeVisibility", true);
                    bundle.putString("analyzingStatus", status);
                } else if (status.contains("Done")) {
                    bundle.putString("direction", "Done!");
                    bundle.putBoolean("resultVisibility", true);
                    bundle.putString("result", status.substring(4));
                }

                navController.navigate(R.id.statusAction, bundle);

                Log.d(TAG, "run: " + "status");
                Log.d(TAG, status);
                //statusMessageTextView.setText(String.format("Status:\n%s", message));
            }
        });
    }

    private class APIKeyVerificationAlert extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return urls[0];
        }

        @Override
        protected void onPostExecute(String result) {
            AlertDialog.Builder apiApprovalAlert = new AlertDialog.Builder(context);
            apiApprovalAlert.setTitle("API Approval Failed");
            apiApprovalAlert.setMessage(result);
            apiApprovalAlert.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mAPI.disconnect();
                            setStatus(R.string.TEXT_DISCONNECTED);
                            dialog.cancel();
                        }
                    });

            apiApprovalAlert.create();
            apiApprovalAlert.show();
        }
    }

    private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks() {

        @Override
        public void BACtrackAPIKeyDeclined(String errorMessage) {
            APIKeyVerificationAlert verify = new APIKeyVerificationAlert();
            verify.execute(errorMessage);
            Log.d(TAG, "BACtrackAPIKeyDeclined: ");
        }

        @Override
        public void BACtrackAPIKeyAuthorized() {
            Log.d(TAG, "BACtrackAPIKeyAuthorized: ");
        }

        @Override
        public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType) {
            bundle.putString("connectionStatus", "Connected");
            bundle.putString("buttonStatus", "DISCONNECT DEVICE");
            bundle.putBoolean("startVisibility", true);
            setStatus("Connected");
        }

        @Override
        public void BACtrackDidConnect(String s) {
            bundle.putString("connectionStatus", "Discovering services...");
            setStatus("Discovering services...");
        }

        @Override
        public void BACtrackDisconnected() {
            if (areLocationServicesAvailable(context) && isBluetoothEnabled()) {
                setStatus(R.string.TEXT_DISCONNECTED);
                bundle.putBoolean("startVisibility", false);
                bundle.putBoolean("countdownVisibility", false);
                bundle.putBoolean("blowVisibility", false);
                bundle.putBoolean("analyzeVisibility", false);
                bundle.putBoolean("resultVisibility", false);
            }
        }

        @Override
        public void BACtrackConnectionTimeout() {
            setStatus("Connection timed out");
        }

        @Override
        public void BACtrackFoundBreathalyzer(BACtrackAPI.BACtrackDevice device) {
            setStatus("Found breathalyzer");
            Log.d(TAG, "Found breathalyzer : " + device.toString());
        }

        @Override
        public void BACtrackCountdown(int currentCountdownCount) {
            setStatus(getString(R.string.TEXT_COUNTDOWN) + " " + currentCountdownCount);
        }

        @Override
        public void BACtrackStart() {
            waitingForBlow = true;
            setStatus("Blow!");
        }

        @Override
        public void BACtrackBlow(float breathVolumeRemaining) {
            if (waitingForBlow)
                setStatus(String.format("Blow Keep blowing(%d%%)", 100 - (int)(100.0 * breathVolumeRemaining)));
        }

        @Override
        public void BACtrackAnalyzing() {
            waitingForBlow = false;
            setStatus(R.string.TEXT_ANALYZING);
        }

        @Override
        public void BACtrackResults(float measuredBac) {
            setStatus("Done" + measuredBac);
            bundle.putFloat("measuredBac", measuredBac);
        }

        @Override
        public void BACtrackFirmwareVersion(String version) {
            setStatus(getString(R.string.TEXT_FIRMWARE_VERSION) + " " + version);
        }

        @Override
        public void BACtrackSerial(String serialHex) {
            setStatus(getString(R.string.TEXT_SERIAL_NUMBER) + " " + serialHex);
        }

        @Override
        public void BACtrackUseCount(int useCount) {
            Log.d(TAG, "UseCount: " + useCount);
            // C6/C8 bug in hardware does not allow getting use count
            if (useCount == 4096) {
                setStatus("Cannot retrieve use count for C6/C8 devices");
            }
            else {
                setStatus(getString(R.string.TEXT_USE_COUNT) + " " + useCount);
            }
        }

        @Override
        public void BACtrackBatteryVoltage(float voltage) {
            //
        }

        @Override
        public void BACtrackBatteryLevel(int level) {
            //setBatteryStatus(getString(R.string.TEXT_BATTERY_LEVEL) + " " + level);
        }

        @Override
        public void BACtrackError(int errorCode) {
            waitingForBlow = false;

            switch (errorCode)
            {
                case Errors.ERROR_TIME_OUT:
                    setStatus(R.string.TEXT_ERR_TIMEOUT_ERROR);
                    break;
                case Errors.ERROR_BLOW_ERROR:
                    setStatus(R.string.TEXT_ERR_BLOW_ERROR);
                    break;
                case Errors.ERROR_LOW_BATTERY:
                    setStatus(R.string.TEXT_ERR_LOW_BATTERY);
                    break;
                case Errors.ERROR_MAX_BAC_EXCEEDED_ERROR:
                    setStatus(R.string.TEXT_ERR_EXCEEDED_MAX_BAC);
                    break;
                default:
                    setStatus(R.string.TEXT_ERR_UNKNOWN_PREFIX);
            }
        }

        @Override
        public void BACtrackUnits(BACtrackUnit units) {

        }
    };

}