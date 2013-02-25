package com.ardrobot.androidopenaccessory;

import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

public class AndroidOpenAccessory {
	private static final String TAG = "AndroidOpenAccessory";

	public static final int USB_ACCESSORY = 1;
	public static final int BLUETOOTH_ACCESSORY = 3;

	public static final String SUBSCRIBE = "com.ardrobot.androidopenaccessory.SUBSCRIBE";

	private Context mContext;

	private MonitoringThread mMonitoringThread;

	private BroadcastReceiver receiver;

	public AndroidOpenAccessory(Context context) {
		mContext = context;
	}

	public void connect(int mode, Connection connection) throws IOException {
		Log.i(TAG, "Connect");
		// Start thread for monitoring this connection (in future one thread per
		// connection)
		mMonitoringThread = new MonitoringThread(mode, connection);
		Thread thread = new Thread(null, mMonitoringThread, "MonitoringThread");
		thread.start();

		// Send message
		new WriteHelper().execute(MQTT.connect());
	}

	public void publish(String topic, byte[] message) throws IOException {
//		Log.i(TAG, "publish");
//		new WriteHelper().execute(MQTT.publish(topic, message));
	}

	public String subscribe(BroadcastReceiver receiver, String topic, int id) throws IOException {
		Log.i(TAG, "Subscribe");
		// Send message
		new WriteHelper().execute(MQTT.subscribe(id, topic, MQTT.AT_MOST_ONCE));
		// Register receiver
		this.receiver = receiver;
		String sub = AndroidOpenAccessory.SUBSCRIBE + "." + topic;
		IntentFilter filter = new IntentFilter();
		filter.addAction(sub);
		mContext.registerReceiver(receiver, filter);
		return sub;
	}

	public void unsubscribe(String topic, int id) throws IOException {
		Log.i(TAG, "Unsubscribe");
		// Send unsub
		new WriteHelper().execute(MQTT.unsubscribe(id, topic));
		// TODO Should only unregister the correct action, now it removes all
		// subscriptions
		mContext.unregisterReceiver(receiver);
	}

	public void pingreq() throws IOException {
		Log.i(TAG, "Pingreq");
		new WriteHelper().execute(MQTT.ping());
	}

	public void disconnect() throws IOException {
		Log.i(TAG, "Disconnect");

		if (mMonitoringThread.mConnection != null) {
			mMonitoringThread.mConnection.close();
		}

		if (receiver != null)
			mContext.unregisterReceiver(receiver);
	}

	private class WriteHelper extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... params) {
			try {
				mMonitoringThread.mConnection.getOutputStream().write(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class MonitoringThread implements Runnable {

		Connection mConnection;

		private ArrayList<String> subscriptions;

		public MonitoringThread(int mode, Connection connection) {
			mConnection = connection;

			subscriptions = new ArrayList<String>();
		}

		public void run() {
			int ret = 0;
			byte[] buffer = new byte[16384];

			while (ret >= 0) {
				try {
					ret = mConnection.getInputStream().read(buffer);
				} catch (IOException e) {
					break;
				}

				if (ret > 0) {
					MQTTMessage msg = MQTT.decode(buffer);

					if (msg.type == MQTT.PUBLISH) {
						Intent broadcast = new Intent();
						broadcast.setAction(SUBSCRIBE + "." + msg.variableHeader.get("topic_name"));
						broadcast.putExtra(SUBSCRIBE + "." + msg.variableHeader.get("topic_name") + ".topic",
								msg.variableHeader.get("topic_name").toString());
						broadcast.putExtra(SUBSCRIBE + "." + msg.variableHeader.get("topic_name") + ".payload",
								msg.payload);
						mContext.sendBroadcast(broadcast);

						// TODO act on QoS level

					} else if (msg.type == MQTT.SUBSCRIBE) {
						String topic = new String(msg.payload);
						if (!subscriptions.contains(topic))
							subscriptions.add(topic);

						// TODO send SUBACK
						// new WriteHelper().execute(MQTT.suback)

					} else if (msg.type == MQTT.UNSUBSCRIBE) {
						String topic = new String(msg.payload);
						if (!subscriptions.contains(topic))
							subscriptions.remove(topic);
						
						// TODO Send UNSUBACK
						// new WriteHelper().execute(MQTT.unsuback)

					}

					// TODO add remaining message types
				}
			}
		}
	}
}