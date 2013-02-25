package com.ardrobot.androidopenaccessory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnection extends Connection {
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothSocket mBluetoothSocket;
	private BluetoothAdapter mBluetoothAdapter;
	
	public BluetoothConnection(String mac) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);

		try {
			mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

//			mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//			mBluetoothSocket.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public InputStream getInputStream() throws IOException {
		return mBluetoothSocket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return mBluetoothSocket.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		mBluetoothSocket.close();
	}

}
