/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.pref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Display;
import org.openntf.swiper.SwiperActivator;

import com.ibm.commons.util.StringUtil;

public class SwiperPreferenceManager extends PreferenceManager {

	private IPreferenceStore store;
	private List<ISwiperPreferenceListener> listeners = new ArrayList<ISwiperPreferenceListener>();
	private static SwiperPreferenceManager instance = new SwiperPreferenceManager();

	public static SwiperPreferenceManager getInstance() {
		return instance;
	}

	protected SwiperPreferenceManager() {
		this.store = SwiperActivator.getDefault().getPreferenceStore();
	}

	public IPreferenceStore getPreferenceStore() {
		return this.store;
	}

	public String getValue(String key, boolean useDefault) {
		if (useDefault) {
			return this.store.getDefaultString(key);
		}
		return this.store.getString(key);
	}

	public boolean getBooleanValue(String key, boolean useDefault) {
		String stringValue = getValue(key, useDefault);
		return StringUtil
				.equalsIgnoreCase(Boolean.TRUE.toString(), stringValue);
	}

	public void setValue(String prefKey, String newValue,
			boolean notifyListeners, int paramInt, Map<?, ?> parameters) {

		String oldVal = this.store.getString(prefKey);

		if (!StringUtil.equals(oldVal, newValue)) {

			if (notifyListeners) {
				SwiperPreferenceChangeEvent event = new SwiperPreferenceChangeEvent(
						paramInt, prefKey);
				event.addProperty("old.value", oldVal);
				event.addProperty("new.value", newValue);

				if (parameters != null) {

					Set<?> localSet = parameters.entrySet();

					Iterator<?> it = localSet.iterator();

					while (it.hasNext()) {
						Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
						event.addProperty(entry.getKey(), entry.getValue());
					}
				}

				save(prefKey, newValue);
				notifyListeners(event);

			} else {
				save(prefKey, newValue);
			}
		}
	}

	protected void save(String prefKey, String value) {
		this.store.setValue(prefKey, value);
	}

	protected void notifyListeners(final SwiperPreferenceChangeEvent event) {

		Runnable local1 = new Runnable() {

			public void run() {

				for (int i = 0; i < listeners.size(); i++) {

					ISwiperPreferenceListener listener = listeners.get(i);

					if (listener != null) {
						listener.preferenceChanged(event);
					} else {
						removePreferenceListener(listener);
					}
				}
			}
		};
		Display.getDefault().syncExec(local1);
	}

	public void addPreferenceListener(ISwiperPreferenceListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	public void removePreferenceListener(ISwiperPreferenceListener listener) {
		this.listeners.remove(listener);
	}

}
