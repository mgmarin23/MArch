package dte.mgmprojects.march;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MQTTSub implements Runnable{//This class executes a thread to handle the messages received from a specif MQTT client which is subscribed to different topics
    Handler creator;
    MqttAndroidClient Tree;
    public MQTTSub(Handler handler, MqttAndroidClient Tree) {
        this.creator = handler;
        this.Tree = Tree;
    }


    @Override
    public void run() {

        //Create and initialize each message that will be sent from the back to the foreground

        Message msg;
        Bundle msg_data;
        msg = creator.obtainMessage();
        msg_data = msg.getData();


        //Callback set for the client
        Tree.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception { //When an MQTT message arrives, it is sent to the foreground
                String measurement = new String(message.getPayload());
                msg_data.putString(topic,measurement);
                creator.dispatchMessage(msg);
                System.out.print(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }
}
