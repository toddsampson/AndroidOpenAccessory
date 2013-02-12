package com.wiley.wroxaccessories;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

public class USBConnection12 extends Connection {
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	private ParcelFileDescriptor mFileDescriptor;
	private UsbAccessory mUsbAccessory; //????
	private Activity mActivity;
	public USBConnection12(UsbManager usbmanager) {
		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			mUsbAccessory = accessory;
			if (usbmanager.hasPermission(mUsbAccessory)) {
				mFileDescriptor = usbmanager.openAccessory(accessory);
				if (mFileDescriptor != null) {
					FileDescriptor fileDescriptor = mFileDescriptor.getFileDescriptor();
					mFileInputStream = new FileInputStream(fileDescriptor);
					mFileOutputStream = new FileOutputStream(fileDescriptor);
				}
			}
		}
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		mActivity.registerReceiver(mBroadcastReceiver, mIntentFilter);
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return mFileInputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return mFileOutputStream;
	}

	@Override
	public void close() throws IOException {
		if (mFileDescriptor != null) {
			mFileDescriptor.close();
		}
		mActivity.unregisterReceiver(mBroadcastReceiver);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
				
			}
		}
	};

}
