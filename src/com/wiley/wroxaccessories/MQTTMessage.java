package com.wiley.wroxaccessories;
import java.util.Map;
import java.io.ByteArrayOutputStream;
public class MQTTMessage extends MQTT {
	// TODO Make variables private and create getting/setter methods
	public int type;
	public boolean DUP;
	public int QoS;
	public boolean retain;
	public int remainingLength;
	public Map<String, String> variableHeader;
	public byte[] payload;
	public static MQTTMessage decode(final byte[] message) {
		int i = 0;
		MQTTMessage mqtt = new MQTTMessage();
		mqtt.type = (message[i] >> 4) & 0x0F; // Get message type
		mqtt.DUP = ((message[i] >> 3) & 0x01) == 0 ? false : true; // Is duplicate?
		mqtt.QoS = (message[i] >> 1) & 0x03;
		mqtt.retain = (message[i] & 0x01) == 0 ? false : true;
		i++;
		int multiplier = 1;
		int length = 0;
		byte digit = 0;
		do {
			digit = message[i++];
			length += (digit & 127) * multiplier;
			multiplier *= 128;
		} while ((digit & 128) != 0);
		mqtt.remainingLength = length;
		switch (mqtt.type) {
		case CONNECT:
			int protocol_name_len = (message[i++] << 8 | message[i++]);
			mqtt.variableHeader.put("protocol_name", new String(message, i, protocol_name_len));
			mqtt.variableHeader.put("protocol_version", message[i++]);
			mqtt.variableHeader.put("has_username", 
					((message[i++] << 7) & 0x01) == 0 ? false : true);
			mqtt.variableHeader.put("has_password", 
					((message[i] << 6) & 0x01) == 0 ? false : true);
			mqtt.variableHeader.put("will_retain", 
					((message[i] << 5) & 0x01) == 0 ? false : true);
			mqtt.variableHeader.put("will_qos", ((message[i] << 3) & 0x03));
			mqtt.variableHeader.put("will", ((message[i] << 2) & 0x01) == 0 ? false : true);
			mqtt.variableHeader.put("clean_session", 
					((message[i] << 1) & 0x01) == 0 ? false : true);
			int keep_alive_len = (message[i++] << 8 | message[i++]);
			mqtt.variableHeader.put("keep_alive", new String(message, i, keep_alive_len));
			break;
		case PUBLISH:
			int topic_name_len = (message[i++] << 8 | message[i++]);
			mqtt.variableHeader.put("topic_name", new String(message, i, topic_name_len));
			mqtt.variableHeader.put("message_id", (message[i++] << 8 | message[i++]));
			break;
		case SUBSCRIBE:
			mqtt.variableHeader.put("message_id", (message[i++] << 8 | message[i++]));
			break;
		case PINGREQ:
			// PINGREQ has no variable header
			break;
		}
		ByteArrayOutputSteam payload = new ByteArrayOutputStream();
		for(int b = i; b < message.length; b++)
			payload.write(message[b]);
		mqtt.payload = payload.toByteArray();
		return mqtt;
	}
}

