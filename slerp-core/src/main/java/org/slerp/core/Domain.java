package org.slerp.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Domain implements Map<Object, Object> {
	// Use Concurrent HashMap for thread safe
	private Map<Object, Object> map = new LinkedHashMap<Object, Object>();
	private static final Gson gsonMapper = new GsonBuilder().setPrettyPrinting().serializeNulls()
			.setDateFormat("yyyy-MM-dd").create();
	static {
//		gsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//		gsonMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
//		gsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		gsonMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
//		gsonMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
	}

	public Domain() {
	}

	public Domain(Object object) {
		this(writeTo(object));
	}

	public Domain(Domain other) {
		this(other.toString());
	}

	@SuppressWarnings("unchecked")
	public Domain(String other) {
		this.map = (Map<Object, Object>) gsonMapper.fromJson(other, HashMap.class);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {

		return map.get(key);
	}

	public Boolean getBoolean(Object key) {
		return Boolean.valueOf(getString(key));
	}

	public Long getLong(Object key) {
		return Long.valueOf(getString(key));
	}

	public Integer getInt(Object key) {
		return Integer.valueOf(getString(key));
	}

	public Short getShort(Object key) {
		return Short.valueOf(getString(key));
	}

	public Double getDouble(Object key) {
		return Double.valueOf(getString(key));
	}

	public Float getFloat(Object key) {
		return Float.valueOf(getString(key));
	}

	public BigInteger getBigInt(Object key) {
		return BigInteger.valueOf(getLong(key));
	}

	public BigDecimal getBigDecimal(Object key) {
		return BigDecimal.valueOf(getDouble(key));
	}

	public String getString(Object key) {
		return String.valueOf(map.get(key));
	}

	public Object putIfAbsent(Object key, Object value) {
		return map.putIfAbsent(key, value);
	}

	public Domain put(Object key, Object value) {
		map.put(key, value);
		return this;
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends Object, ? extends Object> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<Object> keySet() {
		return map.keySet();
	}

	public Collection<Object> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return this.map.entrySet();
	}

	public List<?> getList(String key) {
		String result = gsonMapper.toJson(this.map.get(key));
		List<?> dtoList = gsonMapper.fromJson(result, new TypeToken<ArrayList<?>>(){}.getType());
		return dtoList;
	}

	public Set<?> getSet(String key) {
		String result = gsonMapper.toJson(this.map.get(key));
		Set<?> dtoList = gsonMapper.fromJson(result, new TypeToken<HashSet<?>>(){}.getType());
		return dtoList;
	}

	public Domain getDomain(String key) {
		String result = gsonMapper.toJson(this.map.get(key));
		Domain dto = gsonMapper.fromJson(result, Domain.class);
		return dto;
	}

	public <T> T convertTo(Class<T> classToSerialize) {
		return gsonMapper.fromJson(toString(), classToSerialize);
	}

	public static String writeTo(Object object) {
		String result = gsonMapper.toJson(object);
		return result;
	}

	public void writeTo(File resultFile) {
		
		FileWriter w;
		try {
			w = new FileWriter(resultFile);
			w.write(gsonMapper.toJson(this.map));
			w.close();
		} catch (IOException e) {
			
		}
		
	}

	public String toString() {
		String result = gsonMapper.toJson(this.map);
		return result;
	}

}
