package dte.mgmprojects.march;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
//import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    Spinner Mean_sensor, Historical;
    Switch W_aut;
    Button bPublish;
    Button Stop;
    Number[] MeanS = {1, 6, 12, 24};
    //Number[] Hist= {1,6,12,24};
    Gson gson = new Gson();


    boolean WautIsActive;

    int Mean, Historic;
    ExecutorService es;

    ProgressBar progressBar;
    MqttAndroidClient mqttAndroidClient;
    //MqttAndroidClient pubClient;
    Handler handler_pb;
    ThingsboardService tbs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bPublish = findViewById(R.id.bPublish);
        Mean_sensor = findViewById(R.id.sp_mean);
        Stop = findViewById(R.id.bTank);
        //String mean = Mean_sensor.getSelectedItem().toString();
        Historical = findViewById(R.id.sp_hist);
        boolean WaterAutoIsActive;
        progressBar = findViewById(R.id.progressBar);

        tbs = ServiceGenerator.createService(ThingsboardService.class);

        es = Executors.newSingleThreadExecutor();


        handler_pb = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                int i = inputMessage.getData().getInt("progress", -1);
                //Log.d(logTag, "Message Received with progress = " + i);
                progressBar.setProgress(i);
            }
        };
        // Adapter to adapt the data from array to Spinner
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                R.layout.spinner_view, MeanS);
        Mean_sensor.setAdapter(adapter);

        ArrayAdapter adapter2 = new ArrayAdapter(MainActivity.this,
               R.layout.spinner_view, MeanS);
        Historical.setAdapter(adapter2);
        //---------
        /*Stop.setOnClickListener(View MainActivity.this){
            public void Stop(){
        DataSend Datasend = new DataSend("Stop", 0, 0);
        String datas = gson.toJson(Datasend);
        publishMessage("topic/rarc", datas);
        }
        }
        */



        //--------------------------------------------------------------------------------------------------------------
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://test.mosquitto.org:1883", "AndroidClient");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);


        try {            // Conectar al servidor MQTT
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                //Tree.mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) { // Conectado correctamente, suscribirse al tópico 'mario'
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    subscribeToTopic("topic/arcs");
                    Snackbar.make(findViewById(R.id.bPublish), "Client connected and subscribed", 2000).show();// Inform the user

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Snackbar.make(findViewById(R.id.bPublish), "NOO connected and subscribed", 2000).show();// Inform the user

                    // Error al conectar
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        es = Executors.newSingleThreadExecutor();
        MQTTSub task = new MQTTSub(handler, mqttAndroidClient);
        es.execute(task);

        W_aut = findViewById(R.id.sw_waut);
        WautIsActive = false;


        W_aut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WautIsActive) {
                    // unregister listener and make the appropriate changes in the UI:
                    SendNotification("Water automatization OFF");
                    WautIsActive = false;
                } else {
                    // register listener and make the appropriate changes in the UI:
                    SendNotification("Water automatization ON");
                    WautIsActive = true;
                }
            }
        });

        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendNotification("Stop Tank");
                try {
                    publishMessage("topic/rarc", "Hello");
                    progressBar.setMax(15);
                    LengthyTask task = new LengthyTask(handler_pb, 0);
                    es.execute(task);
                }
                catch (MqttException e) {
                    e.printStackTrace();
                }


            }
        });



    }


    private void subscribeToTopic(String topic) {
        try {            // Suscribirse al tópico especificado
            mqttAndroidClient.subscribe(topic, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }



    Handler handler = new Handler(Looper.getMainLooper()) { //Handler for the message received from the background. Depending on the key (topic), the message will be assigned to a specif String variable
        @Override
        //Then, the attributes (Temperature, Humidity and Light) of each item are updated.
        public void handleMessage(Message inputMessage) {
            this.obtainMessage();

            if(!mqttAndroidClient.isConnected()) {

                SendNotification("No connection");
            } else {
                SendNotification("Connected");
            }


            String recfile = inputMessage.getData().getString("topic/arcs");
            DataConf Datarec = gson.fromJson(recfile, DataConf.class);
            //int Alarm =  1;
            int Alarm = Datarec.getAlarm();
            int number = Datarec.getW_min();
            int water = Datarec.getOpen_P();


            Snackbar.make(findViewById(R.id.bPublish), recfile, 5000).show();
            if (Alarm == 0){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Water Alert");
                builder1.setMessage("There is no need to water tree right now");
                AlertDialog alertDialog = builder1.create();
                alertDialog.show();

            }
            else {
                if (WautIsActive) {
                    try {
                        DataSend Datasend = new DataSend("Start", Datarec.getOpen_P(), Datarec.getW_min());
                        String datas = gson.toJson(Datasend);
                        publishMessage("topic/rarc", datas);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Water Alert");
                    builder.setMessage("New Water guideline: " + number + "min and " + water + "%")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        DataSend Datasend = new DataSend("Start", Datarec.getOpen_P(), Datarec.getW_min());
                                        String datas = gson.toJson(Datasend);
                                        publishMessage("topic/rarc", datas);

                                        progressBar.setMax(number);
                                        LengthyTask task = new LengthyTask(handler_pb, number);
                                        es.execute(task);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();

                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }

    };

    public void SendNotification(String mainText) {

        Snackbar.make(findViewById(R.id.bPublish),
                mainText,
                BaseTransientBottomBar.LENGTH_SHORT
        ).show();


    }
    public void publishMessage(String topic , String measurement) throws MqttException {

        MqttMessage message = new MqttMessage();
        message.setPayload(measurement.getBytes());
        mqttAndroidClient.publish(topic, message);
        SendNotification("Message Published");
        if (!mqttAndroidClient.isConnected()) {
            SendNotification("Not connected");
        }
    }

/*
    Retrofit retrofit = new retrofit2.Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    Retrofit service = retrofit.create(Retrofit.class);

    RequestBody message = RequestBody.create(MediaType.parse("text/plain"), "Hello World");
    Call<ResponseBody> call = service.sendMessage(message);
    call.enqueue(new Callback<ResponseBody>(){
        public void onResponse(Call <ResponseBody> call, Response <ResponseBody> response) {
            if(response.isSuccessful()){

            } else{

            }
        }
        public void onFailure(Call<ResponseBody> call, Throwable t){

        }

    });
*/

    public void send_HTTP(View view){


        JsonObject usr = new JsonObject();

        usr.addProperty("username", "m.gmarin@alumnos.upm.es");
        usr.addProperty("password", "THINGS_22_board");



        //String usr_token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtLmdtYXJpbkBhbHVtbm9zLnVwbS5lcyIsInNjb3BlcyI6WyJURU5BTlRfQURNSU4iXSwidXNlcklkIjoiZDM2ZGUxZDAtODA1Ni0xMWVkLThkMmItMDczZGU0ZTE0OTA3IiwiZW5hYmxlZCI6dHJ1ZSwiaXNQdWJsaWMiOmZhbHNlLCJ0ZW5hbnRJZCI6IjllMjQ1NGIwLTgwNTUtMTFlZC04ZDJiLTA3M2RlNGUxNDkwNyIsImN1c3RvbWVySWQiOiIxMzgxNDAwMC0xZGQyLTExYjItODA4MC04MDgwODA4MDgwODAiLCJpc3MiOiJ0aGluZ3Nib2FyZC5pbyIsImlhdCI6MTY3NTE5MjcyNCwiZXhwIjoxNjc1MjAxNzI0fQ.YbtY2bIGQXqCn4_lIX_21vdwJKj1_z6NskZiakn8qgfkXe6hCKls5Y7OLncFbFETzuk3Lq9IdkZe8ud0T5FC8A",
        final String[] token = new String[1];
        Call<JsonObject> user = tbs.getUserToken(usr);
        user.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.code() == 200){
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        metodo("Bearer " + jsonObject.getString("token"));

                        Log.d("RESPONSE::", "Starting activity with token..." +jsonObject.getString("token"));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else Log.d("RESPONSE:: ERROR", String.valueOf(response.code()));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("RESPONSE:: ERROR", "NOT WORKING");
            }
        });



    }

    public void metodo(String token){
        JsonObject msg = new JsonObject();

        int db = 2;
        int tank = 9;

        msg.addProperty("db", Integer.toString(db));
        msg.addProperty("tank", Integer.toString(tank));

        Call<Void> resp = tbs.send(token,msg);
        resp.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200){
                    try {
                        Log.d("RESPONSE::", "Starting activity with token..." );
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                } else Log.d("RESPONSE:: ERROR", String.valueOf(response.code()));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d("RESPONSE:: ERROR", "NOT WORKING");
            }
        });
    }



}