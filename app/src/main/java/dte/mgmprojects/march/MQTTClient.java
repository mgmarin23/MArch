package dte.mgmprojects.march;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MQTTClient extends AppCompatActivity {
    final String serverUri = "tcp://test.mosquitto.org:1883";
    //final String serverUri = "tcp://172.20.10.3:1883";
    String publishTopic = "topic";
    MqttAndroidClient mqttAndroidClient;
    String clientId = "Client_";
    Calendar calendar;
    String subscriptionTopic = "topic";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMM HH:mm:ss");
    final String LWillmessage = "disconnected";
    public Activity activity;
    public MQTTClient(Activity _activity){
        this.activity = _activity;
    }

    public void SendNotification(String mainText) {

        Snackbar.make(this.activity.findViewById(R.id.bPublish),
                mainText,
                BaseTransientBottomBar.LENGTH_SHORT
        ).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }


    public void publishMessage(String measurement) throws MqttException {
        calendar = Calendar.getInstance();

        MqttMessage message = new MqttMessage();
        message.setPayload(measurement.getBytes());
        mqttAndroidClient.publish(publishTopic, message);
        SendNotification("Message Published");
        if (!mqttAndroidClient.isConnected()) {
            SendNotification("Not connected");
        }
    }

    public void subscribeToTopic()  {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}

