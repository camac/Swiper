/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
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
