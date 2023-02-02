package dte.mgmprojects.march;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class LengthyTask implements Runnable {

    String logTag; // to clearly identify logs
    Handler creator; // the object that creates this task
    int time;

    public LengthyTask(Handler handler, int time) { // constructor
        creator = handler;
        this.time = time;
    }

    @Override
    public void run() {
        Message msg;
        Bundle msg_data;

        // Thread name and Class name
        logTag = "Thread name = " + Thread.currentThread().getName() + ", Class = " +
                this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);

        Log.d(logTag, "run() called"); // log the start of this task

        for (int i = 0; i < time; i++) {
            try {
                Thread.sleep(1000); // sleep for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(logTag, i+1 + " seconds later"); // log each second after sleeping

            // about Handler.obtainMessage():
            // https://developer.android.com/reference/android/os/Handler#obtainMessage()
            // (The retrieved message will have its handler set to "creator")
            msg = creator.obtainMessage(); // message to send to the UI thread
            // about getData(): "Obtains a Bundle of arbitrary data associated with this event,
            // lazily creating it if necessary."
            // (https://developer.android.com/reference/android/os/Message#getData())
            msg_data = msg.getData(); // message data
            msg_data.putInt("progress", i+1); // (key, value = progress)
            msg.sendToTarget(); // send the message to the target
            // the next line is an alternative to send the message:
            //creator.sendMessage(msg);
        }
    }
}