package com.first.chathura.androidjsonparsingwithjsonobjectandloadingtolistview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

    MQTTHelper mqttHelper;
    String jsonData;

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout preview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //START MQTT
        startMqtt();
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        String strJson =
                "{ " +
                        " \"countries\":[ " +

                        "{" +
                        "\"countryname\": \"India\","+
                        "\"flag\": "+ R.drawable.india + ","+
                        "\"language\": \"Hindi\","+
                        "\"capital\": \"New Delhi\"," +
                        "\"currency\": {" +
                        "\"code\": \"INR\", " +
                        "\"currencyname\": \"Rupee\" " +
                        "}" +
                        "}, " +

                        "{" +
                        "\"countryname\": \"Pakistan\","+
                        "\"flag\": "+ R.drawable.pakistan + ","+
                        "\"language\": \"Urdu\","+
                        "\"capital\": \"Islamabad\"," +
                        "\"currency\": {" +
                        "\"code\": \"PKR\", " +
                        "\"currencyname\": \"Pakistani Rupee\" " +
                        "}" +
                        "}," +

                        "{" +
                        "\"countryname\": \"Sri Lanka\","+
                        "\"flag\": "+ R.drawable.srilanka + ","+
                        "\"language\": \"Sinhala\","+
                        "\"capital\": \"Sri Jayawardenapura Kotte\"," +
                        "\"currency\": {" +
                        "\"code\": \"SKR\", " +
                        "\"currencyname\": \"Sri Lankan Rupee\" " +
                        "}" +
                        "}" +

                        "]" +
                        "} ";

        /** The parsing of the xml data is done in a non-ui thread */
        ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();

        /** Start parsing xml data */
        listViewLoaderTask.execute(strJson);
    }

    private void startMqtt() {
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
//                dataReceived.setText(mqttMessage.toString());
                jsonData =mqttMessage.toString();


                JSONObject myJson = new JSONObject(jsonData);
                JSONArray arr=myJson.getJSONArray("user1");

                // use myJson as needed, for example
                /*String name = myJson.optString("name");
                int profileIconId = myJson.optInt("profileIconId");*/
                for(int i=0; i<arr.length(); i++){
                    String uiElement =((JSONObject)arr.get(i)).getString("ui");
                    String elementId =((JSONObject)arr.get(i)).getString("id");
                    String value =((JSONObject)arr.get(i)).getString("value");
                    String dummy = (uiElement+"  "+elementId+"  "+value);
                    Log.w("ok",uiElement +"   "+elementId+"     "+value);

//                    if(uiElement.equals("gauge")){
//                        dataReceived.setText(value);
//                        gauge.setHighValue(Integer.parseInt(value));
//                    }else if(ui.equals("gauge1")){
//                        dataReceived1.setText(value);
//                        gauge1.setHighValue(Integer.parseInt(value));
//                    }else{
//                        System.out.println("Not Implemented yet");
//                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private class ListViewLoaderTask extends AsyncTask<String, Void, SimpleAdapter>{

        JSONObject jObject;
        /** Doing the parsing of xml data in a non-ui thread */
        @Override
        protected SimpleAdapter doInBackground(String... strJson) {
            try{
                jObject = new JSONObject(strJson[0]);
                CountryJSONParser countryJsonParser = new CountryJSONParser();
                countryJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("JSON Exception1",e.toString());
            }

            CountryJSONParser countryJsonParser = new CountryJSONParser();

            List<HashMap<String, String>> countries = null;

            try{
                /** Getting the parsed data as a List construct */
                countries = countryJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }

            /** Keys used in Hashmap */
            String[] from = { "country","flag","details"};

            /** Ids of views in listview_layout */
            int[] to = { R.id.tv_country,R.id.iv_flag,R.id.tv_country_details};

            /** Instantiating an adapter to store each items
             *  R.layout.listview_layout defines the layout of each item
             */
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), countries, R.layout.lv_layout, from, to);

            return adapter;
        }

        /** Invoked by the Android system on "doInBackground" is executed completely */
        /** This will be executed in ui thread */
        @Override
        protected void onPostExecute(SimpleAdapter adapter) {

            /** Getting a reference to listview of main.xml layout file */
            GridView listView = ( GridView ) findViewById(R.id.lv_countries);

            /** Setting the adapter containing the country list to listview */
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}