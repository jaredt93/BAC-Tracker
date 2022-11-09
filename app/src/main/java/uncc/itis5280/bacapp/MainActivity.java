package uncc.itis5280.bacapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import BACtrackAPI.API.BACtrackAPI;
import BACtrackAPI.API.BACtrackAPICallbacks;
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

    private static String TAG = "JWT";
    private static final byte PERMISSIONS_FOR_SCAN = 100;

    private BACtrackAPI mAPI;
    private Context context;
    private boolean waitingForBlow;

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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        this.registerReceiver(mBluetoothReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Log.d(TAG, "onCreate: " + "location");
            this.registerReceiver(mLocationServicesReceiver,
                    new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        requestBlePermissions(this, 001);

        initBacTrackAPI();
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

    protected void initBacTrackAPI() {
        String apiKey = "712d405434c64ec0aec98949490172";

        try {
            Log.d(TAG, "initBacTrackAPI: " + this.toString());
            mAPI = new BACtrackAPI(this, mCallbacks, apiKey);
            context = this;
        } catch (BluetoothLENotSupportedException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing bs");
            e.printStackTrace();
            this.setStatus(R.string.TEXT_ERR_BLE_NOT_SUPPORTED);
        } catch (BluetoothNotEnabledException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing be");
            e.printStackTrace();
            this.setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
        } catch (LocationServicesNotEnabledException e) {
            Log.d(TAG, "initBacTrackAPI: " + "testing");
            e.printStackTrace();
            this.setStatus(R.string.TEXT_ERR_LOCATIONS_NOT_ENABLED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_FOR_SCAN: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mAPI == null)
                        initBacTrackAPI();
                    if (mAPI != null)   // check for success in case it was null before
                        mAPI.connectToNearestBreathalyzer();
                }
            }
        }
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        handleBluetoothOn();
                        break;
                }
            }
        }
    };

    protected void handleBluetoothOn() {
        if (mAPI == null) {
            initBacTrackAPI();
        } else if (!areLocationServicesAvailable(context)) {
            this.setStatus(R.string.TEXT_ERR_LOCATIONS_NOT_ENABLED);
        } else {
            this.setStatus(R.string.TEXT_DISCONNECTED);
        }
    }

    private final BroadcastReceiver mLocationServicesReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            Log.d(TAG, "onReceive: " + "location received");
            if (!areLocationServicesAvailable(context)) {
                setStatus(R.string.TEXT_ERR_LOCATIONS_NOT_ENABLED);
            }
            else if (mAPI == null) {
                initBacTrackAPI();
            }
            else if (!isBluetoothEnabled()){
                setStatus(R.string.TEXT_ERR_BT_NOT_ENABLED);
            }
            else {
                setStatus(R.string.TEXT_DISCONNECTED);
            }
        }
    };

    protected boolean isBluetoothEnabled()
    {
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        return (bluetoothManager.getAdapter().isEnabled());
    }


    protected static boolean areLocationServicesAvailable(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        }
        else
        {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    @Override
    public void connectNearestClicked() {
        setStatus(R.string.TEXT_CONNECTING);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FOR_SCAN);
        } else if (mAPI != null) {
            /**
             * Permission already granted, start scan.
             */
            mAPI.connectToNearestBreathalyzer();
            //connectButton.setEnabled(false);
        }
    }



    private void setStatus(int resourceId) {
        this.setStatus(this.getResources().getString(resourceId));
    }

    private void setStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + "status");
                Log.d(TAG, message);
                //statusMessageTextView.setText(String.format("Status:\n%s", message));
            }
        });
    }

    private final BACtrackAPICallbacks mCallbacks = new BACtrackAPICallbacks() {

        @Override
        public void BACtrackAPIKeyDeclined(String errorMessage) {
            //APIKeyVerificationAlert verify = new APIKeyVerificationAlert();
            //verify.execute(errorMessage);
            Log.d(TAG, "BACtrackAPIKeyDeclined: ");
        }

        @Override
        public void BACtrackAPIKeyAuthorized() {

        }

        @Override
        public void BACtrackConnected(BACTrackDeviceType bacTrackDeviceType) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setStatus(R.string.TEXT_CONNECTED);
                    //disconnectButton.setEnabled(true);
                }
            });
        }

        @Override
        public void BACtrackDidConnect(String s) {
            setStatus(R.string.TEXT_DISCOVERING_SERVICES);
        }

        @Override
        public void BACtrackDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (areLocationServicesAvailable(context) && isBluetoothEnabled())
                        setStatus(R.string.TEXT_DISCONNECTED);    // else, other routines will tell the user to enable bt/loc
//                    setBatteryStatus("");
//                    setCurrentFirmware(null);
//                    connectButton.setEnabled(true);
//                    disconnectButton.setEnabled(false);
                }
            });
        }
        @Override
        public void BACtrackConnectionTimeout() {
            setStatus("Connection timed out");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //connectButton.setEnabled(true);
                    //disconnectButton.setEnabled(false);
                }
            });
        }

        @Override
        public void BACtrackFoundBreathalyzer(BACtrackAPI.BACtrackDevice device) {
            Log.d(TAG, "Found breathalyzer : " + device.toString());
        }

        @Override
        public void BACtrackCountdown(int currentCountdownCount) {
            setStatus(getString(R.string.TEXT_COUNTDOWN) + " " + currentCountdownCount);
        }

        @Override
        public void BACtrackStart() {
            waitingForBlow = true;
            setStatus(R.string.TEXT_BLOW_NOW);
        }

        @Override
        public void BACtrackBlow(float breathVolumeRemaining) {
            if (waitingForBlow)
                setStatus(String.format("Keep Blowing (%d%%)", 100 - (int)(100.0 * breathVolumeRemaining)));
        }

        @Override
        public void BACtrackAnalyzing() {
            waitingForBlow = false;
            setStatus(R.string.TEXT_ANALYZING);
        }

        @Override
        public void BACtrackResults(float measuredBac) {
            setStatus(getString(R.string.TEXT_FINISHED) + " " + measuredBac);
        }

        @Override
        public void BACtrackFirmwareVersion(String version) {
            //setCurrentFirmware(version);
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
            if (useCount == 4096)
            {
                setStatus("Cannot retrieve use count for C6/C8 devices");
            }
            else
            {
                setStatus(getString(R.string.TEXT_USE_COUNT) + " " + useCount);
            }
        }

        @Override
        public void BACtrackBatteryVoltage(float voltage) {

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