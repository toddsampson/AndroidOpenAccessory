package com.ardrobot.androidopenaccessory;

import java.util.HashMap;
import java.util.Map;

public class MQTTMessage {
	// TODO Make variables private and create getting/setter methods
	public int type;
	public boolean DUP;
	public int QoS;
	public boolean retain;
	public int remainingLength;

	// Note: This is String, Object, not String, String as in the aoabook
	public Map<String, Object> variableHeader = new HashMap<String, Object>();

	public byte[] payload;
}

