package com.petronas.fof.spot.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.petronas.fof.spot.helpers.AutoStartHelper;
import com.petronas.fof.spot.NotificationDialog;
import com.petronas.fof.spot.R;
import com.petronas.fof.spot.services.ScanService;
import com.petronas.fof.spot.utilities.SharedPrefsUtils;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.basgeekball.awesomevalidation.ValidationStyle.UNDERLABEL;
import static com.petronas.fof.spot.AppConstants.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static com.petronas.fof.spot.AppConstants.SERVER_URL;
import static com.petronas.fof.spot.AppConstants.SHARED_PREF_NAME;
import static com.petronas.fof.spot.AppConstants.START_FOREGROUND_SERVICE;
import static com.petronas.fof.spot.AppConstants.STOP_FOREGROUND_SERVICE;
import static com.petronas.fof.spot.fcm.MyFirebaseMessagingService.FCM_IMAGE;
import static com.petronas.fof.spot.fcm.MyFirebaseMessagingService.FCM_PARAM;

public class MainActivity extends BaseActivity {

    // constants
    private static final boolean ALLOW_GPS = true;
    private static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;
    private final String connString = "HostName=PTAZSG-DEV-GRT-SPOT-IOTHUB.azure-devices.net;DeviceId=ios-test;SharedAccessKey=blabla";
    private DeviceClient client;
    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private final String TAG = "MainActivity";     // logging
    // service flags (is the service running, is the mode Learning/Tracking)
    private int serviceState = 0;
    private boolean serviceLearning = false;
    // AlarmManager related variables
    /* private PendingIntent recurringLl24 = null;
    private Intent ll24 = null;
    AlarmManager alarms = null;*/

    Timer timer = null;
    private RemindTask oneSecondTimer = null;
    private String fcmToken = null;

    // autocompletion suggestions for location editText
    private String[] autocompleteLocations = new String[]{"BE-L1", "BE-L2", "Robotics lab", "BE-L1 Toilet zone", "Pantry"};

    // RequestQueue to send data to server
    private RequestQueue queue;

    private SharedPreferences sharedPref;

    private AutoCompleteTextView departmentNameEdit;
    private EditText userNameEdit;
    private EditText userIDEdit;
    private EditText phoneNoEdit;
    private AutoCompleteTextView locTextView;
    private ArrayAdapter<CharSequence> departmentNameSpinnerAdapter;

    AwesomeValidation validator;

    private ToggleButton toggleServiceLearning;
    private ToggleButton toggleStartStop;
    private TextView updateMsg;
    private NotificationDialog notificationDialog;

    private CompoundButton.OnCheckedChangeListener toggleServiceCheckChangedListener;
    private CompoundButton.OnCheckedChangeListener toggleStartStopCheckChangedListener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(FCM_PARAM)){
                String data=bundle.getString(FCM_PARAM);
                String image=bundle.getString(FCM_IMAGE);
                notificationDialog=new NotificationDialog();
                notificationDialog.showNotificationDialog(this,data,image);
            }
        }

        validator = new AwesomeValidation(UNDERLABEL);
        validator.setContext(this);
        queue = Volley.newRequestQueue(this);

        try {
            sharedPref = EncryptedSharedPreferences.create(
                    SHARED_PREF_NAME,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException ignored) {
        }

        getFCMToken();

        if (!SharedPrefsUtils.getBooleanPreference(this, SharedPrefsUtils.PREF_KEY_APP_AUTO_START, false))
            AutoStartHelper.getInstance().getAutoStartPermission(this);

        toggleServiceLearning = findViewById(R.id.toggleServiceLearning);
        toggleStartStop = findViewById(R.id.toggleStartStop);
        updateMsg = findViewById(R.id.textOutput);

        departmentNameEdit = findViewById(R.id.departmentName);
        userNameEdit = findViewById(R.id.userName);
        userIDEdit = findViewById(R.id.userID);
        phoneNoEdit = findViewById(R.id.phoneNo);
        locTextView = findViewById(R.id.locationName);

        departmentNameSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.available_departments, R.layout.support_simple_spinner_dropdown_item);
        departmentNameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentNameEdit.setAdapter(departmentNameSpinnerAdapter);
        departmentNameEdit.setKeyListener(null);
        departmentNameEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((AutoCompleteTextView) v).showDropDown();
                return false;
            }
        });

        validator.addValidation(departmentNameEdit, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.department_name_required));
        validator.addValidation(userNameEdit, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.user_name_required));
        validator.addValidation(userIDEdit, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.user_id_required));
        validator.addValidation(phoneNoEdit, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.phone_number_required));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocompleteLocations);
        locTextView.setAdapter(adapter);

        serviceState = sharedPref.getInt("serviceState", 0);
        serviceLearning = sharedPref.getBoolean("serviceLearning", false);

        // coming back from service, activity might have been destroyed
        if (serviceState == 1) {
            toggleStartStop.setOnCheckedChangeListener(null);
            toggleStartStop.setChecked(true);
            toggleStartStop.setOnCheckedChangeListener(toggleStartStopCheckChangedListener);

            toggleServiceLearning.setOnCheckedChangeListener(null);
            toggleServiceLearning.setChecked(serviceLearning);
            toggleServiceLearning.setOnCheckedChangeListener(toggleServiceCheckChangedListener);

            updateMsg.setText("running");
        } else {
            updateMsg.setText("not running");
        }

        departmentNameEdit.setListSelection(new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.available_departments)))
                .indexOf(sharedPref.getString("departmentName", "")));
        departmentNameEdit.setText(sharedPref.getString("departmentName", ""));
        departmentNameSpinnerAdapter.getFilter().filter(null);

        userNameEdit.setText(sharedPref.getString("userName", ""));
        userIDEdit.setText(sharedPref.getString("userID", ""));
        phoneNoEdit.setText(sharedPref.getString("phoneNo", ""));
        locTextView.setText(sharedPref.getString("locationName", ""));

        toggleServiceCheckChangedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Timber.d("Toggled to Learning mode.");
                    serviceLearning = true;
                } else {
                    Timber.d("Toggled to Tracking mode.");
                    serviceLearning = false;
                    locTextView.setText("");
                }
                updateMsg.setText("not running");
                toggleStartStop.setChecked(false);
            }
        };

        toggleStartStopCheckChangedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // if checked, start the tracking service
                if (isChecked) {
                    if(validator.validate()){

                        if (!serviceLearning) {
                            locTextView.setText("");
                        } else {
                            if (locTextView.getText().toString().toLowerCase().trim().equals("")) {
                                updateMsg.setText("location name cannot be empty when learning");
                                buttonView.toggle();
                                return;
                            }
                        }

                        saveInputSynchronously();

                        // start foreground Tracking service
                        Intent scanService = new Intent(MainActivity.this, ScanService.class);
                        scanService.putExtra("departmentName", departmentNameEdit.getText().toString());
                        scanService.putExtra("userName", userNameEdit.getText().toString().toLowerCase().trim());
                        scanService.putExtra("locationName", locTextView.getText().toString().toLowerCase().trim());
                        scanService.putExtra("serverAddress", SERVER_URL);
                        scanService.putExtra("allowGPS", ALLOW_GPS);
                        scanService.putExtra("serviceLearning", serviceLearning);
                        scanService.setAction(START_FOREGROUND_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Timber.d("running startForegroundService");
                            startForegroundService(scanService);
                        } else {
                            Timber.d("running legacy startService");
                            startService(scanService);
                        }

                        updateMsg.setText("running");
                        //disable editing
                        enableEditing(false);
                    } else {
                        toggleStartStop.setOnCheckedChangeListener(null);
                        toggleStartStop.setChecked(false);
                        toggleStartStop.setOnCheckedChangeListener(toggleStartStopCheckChangedListener);
                    }
                } else {
                    Timber.d("Tracking toggled to false");
                    // cancel alarms to maintain service
                    /*if (alarms != null) {
                        alarms.cancel(recurringLl24);
                    }
                    if (timer != null) {
                        timer.cancel();
                    }*/
                    Timber.d("alarms and timers have been cancelled");

                    // stop foreground service
                    Timber.d("Stopping foreground service.");
                    Intent scanService = new Intent(MainActivity.this, ScanService.class);
                    scanService.setAction(STOP_FOREGROUND_SERVICE);
                    startService(scanService);

                    updateMsg.setText("not running");
                    //enable editing
                    enableEditing(true);
                }
            }
        };

        toggleStartStop.setOnCheckedChangeListener(toggleStartStopCheckChangedListener);
        toggleServiceLearning.setOnCheckedChangeListener(toggleServiceCheckChangedListener);
    }

    private void saveInputSynchronously() {
            String departmentName = departmentNameEdit.getText().toString();
            String userName = userNameEdit.getText().toString().toLowerCase().trim();
            String userID = userIDEdit.getText().toString().toLowerCase().trim();
            String phoneNo = phoneNoEdit.getText().toString().toLowerCase().trim();
            String locationName = locTextView.getText().toString().toLowerCase().trim();

            // save all parameters
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("departmentName", departmentName);
            editor.putString("userName", userName);
            editor.putString("userID", userID);
            editor.putString("phoneNo", phoneNo);
            editor.putString("serverAddress", SERVER_URL);
            editor.putString("locationName", locationName);
            editor.putBoolean("allowGPS", ALLOW_GPS);
            editor.commit();
    }

    private void getFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> command) {
                fcmToken = command.getResult().getToken();
                Timber.d("firebaseToken: %s", fcmToken);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("fcm_token", fcmToken);
                editor.commit();
            }
        });
    }

    private void sendDataToIOTHUB()
    {
        try
        {
            EventCallback eventCallback = new EventCallback();
           // client.sendEventAsync(/* sendMessage */, eventCallback, /* msgSentCount */);
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e);
        }
    }

    private void initClient() throws URISyntaxException, IOException
    {
        client = new DeviceClient(connString, protocol);

        try
        {
            client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
            client.open();
            MessageCallback callback = new MessageCallback();
            client.setMessageCallback(callback, null);
            client.subscribeToDeviceMethod(new DeviceMethodCallback(), getApplicationContext(), new DeviceMethodStatusCallBack(), null);
        }
        catch (Exception e)
        {
            System.err.println("Exception while opening IoTHub connection: " + e);
            client.closeNow();
            System.out.println("Shutting down...");
        }
    }

    class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {

            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
            {
                //write something
            }
            else
            {
                //failure case
            }
        }
    }

    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            System.out.println(
                    "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            return IotHubMessageResult.COMPLETE;
        }
    }

   /* private void createAlarm() {
        String loc = sharedPref.getString("locationName", "");
        String usr = sharedPref.getString("userName", "");
        String dprtmnt = sharedPref.getString("departmentName", "");
        ll24 = new Intent(MainActivity.this, AlarmReceiverLife.class);
        Timber.d( "setting departmentName to [" + dprtmnt + "]");
        ll24.putExtra("departmentName", dprtmnt);
        ll24.putExtra("userName", usr);
        ll24.putExtra("serverAddress", SERVER_URL);
        ll24.putExtra("locationName", loc);
        ll24.putExtra("allowGPS", ALLOW_GPS);
        ll24.putExtra("serviceLearning", toggleMode.isChecked());
        recurringLl24 = PendingIntent.getBroadcast(MainActivity.this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
    }*/

    // send data to server
    private void sendToServer(JSONObject data, String URL) {

        final String mRequestBody = data.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Timber.d("Request response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d( "Response Error: " + error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = new String(response.data);
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(stringRequest);
    }


    class RemindTask extends TimerTask {
        private Integer counter = 0;

        public void resetCounter() {
            counter = 0;
        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    counter++;
                    TextView updateMsg = findViewById(R.id.textOutput);
                    String currentText = updateMsg.getText().toString();
                    if (currentText.contains("ago: ")) {
                        String[] currentTexts = currentText.split("ago: ");
                        currentText = currentTexts[1];
                    }
                    updateMsg.setText(counter + " seconds ago: " + currentText);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.scheduler) {
            if (validator.validate()) {
                saveInputSynchronously();
                Intent intent = new Intent(MainActivity.this, SchedulerActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected class DeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            try {
                switch (methodName) {
                    case "setSendMessagesInterval": {
//                        int status = method_setSendMessagesInterval(methodData);
//                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                        break;
                    }
                    default: {
//                        int status = method_default(methodData);
//                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    }
                }
            }
            catch (Exception e)
            {
                int status = METHOD_THROWS;
                deviceMethodData = new DeviceMethodData(status, "Method Throws " + methodName);
            }
            return null; //deviceMethodData;
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        // make back button act like home button
        // TODO only do this when service is running
        Intent backtoHome = new Intent(Intent.ACTION_MAIN);
        backtoHome.addCategory(Intent.CATEGORY_HOME);
        backtoHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backtoHome);
    }

    @Override
    protected void onDestroy() {
        Timber.d("MainActivity onDestroy()");
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Your app does not open the location settings from background", Toast.LENGTH_SHORT).show();
                } else {
                    Timber.d( "Permission granted");
                    SharedPrefsUtils.setBooleanPreference(this, SharedPrefsUtils.PREF_KEY_SYSTEM_ALERT_WINDOW, true);
                }

            }
        }
    }

    private void enableEditing(boolean enabled) {
        if (enabled) {
            userIDEdit.setFocusableInTouchMode(enabled);
            userNameEdit.setFocusableInTouchMode(enabled);
            locTextView.setFocusableInTouchMode(enabled);
            departmentNameEdit.setFocusableInTouchMode(enabled);
            phoneNoEdit.setFocusableInTouchMode(enabled);
        } else {
            userIDEdit.setFocusable(enabled);
            userNameEdit.setFocusable(enabled);
            locTextView.setFocusable(enabled);
            departmentNameEdit.setFocusable(enabled);
            phoneNoEdit.setFocusable(enabled);
        }
    }
}
