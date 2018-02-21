package com.tqdev.crudapi.core;

import java.util.ArrayList;

public interface JooqSeek {

	default public boolean hasSeek(Params params) {
		return params.containsKey("seek");
	}

	default public Object[] seekAfter(int columnCount, Params params) {
		ArrayList<Object> values = new ArrayList<>();
		if (params.containsKey("seek")) {
			for (String key : params.get("seek")) {
				values.add(key);
			}
			while (values.size() < columnCount) {
				values.add(null);
			}
		}
		return values.toArray();
	}

	default public boolean hasSize(Params params) {
		return params.containsKey("size");
	}

	default public int seekSize(Params params) {
		int numberOfRows = 20;
		if (params.containsKey("size")) {
			for (String key : params.get("size")) {
				numberOfRows = Integer.valueOf(key);
			}
		}
		return numberOfRows;
	}

}