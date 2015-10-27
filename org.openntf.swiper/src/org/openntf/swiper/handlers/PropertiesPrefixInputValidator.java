package org.openntf.swiper.handlers;

import org.eclipse.jface.dialogs.IInputValidator;

import com.ibm.commons.util.StringUtil;

public class PropertiesPrefixInputValidator implements IInputValidator {

	@Override
	public String isValid(String prefix) {

		if (StringUtil.isEmpty(prefix)) {
			return "Prefix must not be Empty";
		}
		
		if (!prefix.matches("^[a-zA-Z0-9]+$")) {
			return "Prefix must be letters and numbers, no spaces or special characters";
		}
		
		return null;
	}

}
