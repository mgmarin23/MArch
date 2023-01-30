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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;


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

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    Spinner Mean_sensor, Historical;
    Switch W_aut;
    Button bPublish;
    Number[] MeanS = {1, 6, 12, 24};
    //Number[] Hist= {1,6,12,24};

    boolean WautIsActive;

    String Mean, Historic;
    ExecutorService es;


    MqttAndroidClient mqttAndroidClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bPublish = findViewById(R.id.bPublish);
        Mean_sensor = findViewById(R.id.sp_mean);
        Historical = findViewById(R.id.sp_hist);
        boolean WaterAutoIsActive;





        // Adapter to adapt the data from array to Spinner
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_spinner_item, MeanS);
        Mean_sensor.setAdapter(adapter);

        ArrayAdapter adapter2 = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_spinner_item, MeanS);
        Historical.setAdapter(adapter2);

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
                    subscribeToTopic("topic/mario");
                    Snackbar.make(findViewById(R.id.bPublish), "Client connected and subscribed", 2000).show();// Inform the user

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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
    }


    private void subscribeToTopic(String topic) {
        try {            // Suscribirse al tópico especificado
            mqttAndroidClient.subscribe(topic, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //}}
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
            //Snackbar.make(findViewById(R.id.bPublish), "The msg is " + inputMessage.getData(), 2000).show();

            if(WautIsActive){
                try {
                    publishMessage("New Test OK Automatically");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            else {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Water Alert");
                builder.setMessage("The conditions are... Do you want to water?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    publishMessage("New Test OK");
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                try {
                                    publishMessage("New Test NOK");
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }

    };

    public void SendNotification(String mainText) {

        Snackbar.make(findViewById(R.id.bPublish),
                mainText,
                BaseTransientBottomBar.LENGTH_SHORT
        ).show();


    }
    public void publishMessage(String measurement) throws MqttException {

        MqttMessage message = new MqttMessage();
        message.setPayload(measurement.getBytes());
        mqttAndroidClient.publish("topic/rmario", message);
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
    /*
    public class ServiceGenerator{
        private  static final String BASE_URI = "https://srv_iot.diatel.upm.es/api/";
        private static Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("")
                .client(new OkHttpClient.Builder().addInterceptor((
                        new HttpLoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY)).build())
                .addConverterFactory(GsonConverterFactory.create());

        public static <S> S createService(Class <S> serviceClass) {
            Retrofit adapter = builder.build();
            return adapter.create(serviceClass);
        }

    }

     */
}