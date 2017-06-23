package org.slerp.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Dto implements Map<Object, Object> {
	private Map<Object, Object> map = new LinkedHashMap<Object, Object>();
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	static {
		jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	public Dto() {
	}

	public Dto(Object other) {
		this(writeTo(other));
	}

	public Dto(Dto other) {
		this(other.toString());
	}

	@SuppressWarnings("unchecked")
	public Dto(String other) {
		try {
			this.map = (Map<Object, Object>) jsonMapper.readValue(other, HashMap.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public Long getLong(Object key) {
		return Long.valueOf(map.get(key).toString());
	}

	public Integer getInt(Object key) {
		return Integer.valueOf(map.get(key).toString());
	}

	public Short getShort(Object key) {
		return Short.valueOf(map.get(key).toString());
	}

	public Double getDouble(Object key) {
		return Double.valueOf(map.get(key).toString());
	}

	public Float getFloat(Object key) {
		return Float.valueOf(map.get(key).toString());
	}

	public BigInteger getBigInt(Object key) {
		return BigInteger.valueOf(getLong(key));
	}

	public BigDecimal getBigDecimal(Object key) {
		return BigDecimal.valueOf(getDouble(key));
	}

	public String getString(Object key) {
		return map.get(key).toString();
	}

	public Object putIfAbsent(Object key, Object value) {
		return map.putIfAbsent(key, value);
	}

	public Dto put(Object key, Object value) {
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

	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return map.entrySet();
	}

	public List<Dto> getList(String key) {
		try {
			String result = jsonMapper.writeValueAsString(this.map.get(key));
			List<Dto> dtoList = jsonMapper.readValue(result, new TypeReference<List<Dto>>() {
			});
			return dtoList;
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		} catch (IOException e) {
			throw new CoreException(e);
		}
	}

	public Dto getDto(String key) {
		try {
			String result = jsonMapper.writeValueAsString(this.map.get(key));
			Dto dto = jsonMapper.readValue(result.getBytes(), Dto.class);
			return dto;
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		} catch (IOException e) {
			throw new CoreException(e);
		}
	}

	public <T> T convertTo(Class<T> classToSerialize) throws Throwable {
		return jsonMapper.readValue(toString(), classToSerialize);
	}

	public static String writeTo(Object object) {
		try {
			String result = jsonMapper.writeValueAsString(object);
			return result;
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		}
	}

	public String toString() {
		try {
			String result = jsonMapper.writeValueAsString(this.map);
			return result;
		} catch (JsonProcessingException e) {
			throw new CoreException(e);
		}
	}
}
