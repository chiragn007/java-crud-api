package com.tqdev.crudapi.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class Record extends LinkedHashMap<String, Object> {

	public static Record valueOf(Object object) {
		if (object instanceof Map<?, ?>) {
			return Record.valueOf((Map<?, ?>) object);
		}
		return null;
	}

	public static Record valueOf(Map<?, ?> map) {
		Record result = new Record();
		for (Object key : map.keySet()) {
			result.put(key.toString(), map.get(key));
		}
		return result;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
