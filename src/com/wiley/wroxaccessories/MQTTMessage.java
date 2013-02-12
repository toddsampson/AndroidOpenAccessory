package com.wiley.wroxaccessories;
import java.util.Map;
public class MQTTMessage {
	// TODO Make variables private and create getting/setter methods
	public int type;
	public boolean DUP;
	public int QoS;
	public boolean retain;
	public int remainingLength;
	public Map<String, String> variableHeader;
	public byte[] payload;
}

