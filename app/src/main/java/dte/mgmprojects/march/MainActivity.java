package dte.mgmprojects.march;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
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


public class MainActivity extends AppCompatActivity {

    Spinner Mean_sensor, Historical;
    Switch W_aut;
    Button bPublish;
    Number[] MeanS = {1, 6, 12, 24};
    //Number[] Hist= {1,6,12,24};



    String Mean, Historic;
    ExecutorService es;

    MQTTClient myMQTT;
    MqttAndroidClient mqttAndroidClient;

    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bPublish = findViewById(R.id.bPublish);
        Mean_sensor = findViewById(R.id.sp_mean);
        Historical = findViewById(R.id.sp_hist);
        W_aut = findViewById(R.id.sw_waut);

        // Adapter to adapt the data from array to Spinner
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_spinner_item, MeanS);
        Mean_sensor.setAdapter(adapter);

        ArrayAdapter adapter2 = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_spinner_item, MeanS);
        Historical.setAdapter(adapter2);
        /*
        try {
            client = new MqttClient("tcp://test.mosquitto.org:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

         */


        //--------------------------------------------------------------------------------------------------------------
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://test.mosquitto.org:1883", "AndroidClient");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        /*
        try {
            client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
        }

         */
        /*
        try {
            client.publish("topic/rmario", "Hola".getBytes(StandardCharsets.UTF_8),0,false);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            client.subscribe("topic/mario");
            Snackbar.make(findViewById(R.id.bPublish), "Client connected and subscribed", 2000).show();
        } catch (Exception e) {
            e.printStackTrace();

        }

         */

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

            Snackbar.make(findViewById(R.id.bPublish), "The msg is " + inputMessage.getData(), 2000).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Water Alert");
            builder.setMessage("The conditions are... Do you want to water?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            /*Tree.publishTopic = "topic/rMario"; // If there is at least 1 sensor active, the date will be published as a separated message

                            try {
                                Tree.publishMessage("date");
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                             */

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

        };




}