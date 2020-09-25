package com.zlyq.client.android.analytics.db.table;

import com.zlyq.client.android.analytics.db.utils.ZlyqFieldUtils;

public class ZlyqKeyValue {
	private String key;
	private Object value;
	
	public ZlyqKeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public ZlyqKeyValue() {}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		if(value instanceof java.util.Date || value instanceof java.sql.Date){
			return ZlyqFieldUtils.SDF.format(value);
		}
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	
}
