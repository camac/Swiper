package org.openntf.swiper.pref;

import java.util.HashMap;
import java.util.Map;

public class SwiperPreferenceChangeEvent {
	protected int type;
	protected Map<Object, Object> properties = new HashMap<Object, Object>();
	private Object sender;

	public SwiperPreferenceChangeEvent(int type, Object sender) {
		this.type = type;
		this.sender = sender;
	}

	public Object getSender() {
		return this.sender;
	}

	public int getType() {
		return this.type;
	}

	public Map<Object, Object> getProperties() {
		return this.properties;
	}

	public Object getProperty(Object key) {
		return this.properties.get(key);
	}

	public void addProperty(Object key, Object value) {
		this.properties.put(key, value);
	}
}
