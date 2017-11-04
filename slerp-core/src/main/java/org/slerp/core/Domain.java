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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Domain implements Map<Object, Object> {
	// Use Concurrent HashMap for thread safe
	private Map<Object, Object> map = new LinkedHashMap<Object, Object>();
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	static {
		jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
		jsonMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
		jsonMapper.setSerializationInclusion(Include.NON_NULL);
		jsonMapper.setSerializationInclusion(Include.NON_EMPTY);
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
		try {
			this.map = (Map<Object, Object>) jsonMapper.readValue(other, HashMap.class);
		} catch (JsonParseException e) {
			throw new CoreException("failed.to.parse.json", e);
		} catch (JsonMappingException e) {
			throw new CoreException("failed.to.mapping.json", e);
		} catch (IOException e) {
			throw new CoreException("failed.to.read.json.data", e);
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

	public List<Domain> getList(String key) {
		try {
			String result = jsonMapper.writeValueAsString(this.map.get(key));
			List<Domain> dtoList = jsonMapper.readValue(result, new TypeReference<List<Domain>>() {
			});
			return dtoList;
		} catch (JsonProcessingException e) {
			throw new CoreException("the.value.should.be.json.list." + key, e);
		} catch (IOException e) {
			throw new CoreException("failed.to.read.json.data", e);
		}
	}

	public Set<Domain> getSet(String key) {
		try {
			String result = jsonMapper.writeValueAsString(this.map.get(key));
			Set<Domain> dtoList = jsonMapper.readValue(result, new TypeReference<Set<Domain>>() {
			});
			return dtoList;
		} catch (JsonProcessingException e) {
			throw new CoreException("the.value.should.be.json.set." + key, e);
		} catch (IOException e) {
			throw new CoreException("failed.to.read.json.data", e);
		}
	}

	public Domain getDomain(String key) {
		try {
			String result = jsonMapper.writeValueAsString(this.map.get(key));
			Domain dto = jsonMapper.readValue(result.getBytes(), Domain.class);
			return dto;
		} catch (JsonProcessingException e) {
			throw new CoreException("the.value.should.be.json.object." + key, e);
		} catch (IOException e) {
			throw new CoreException("failed.to.read.json.data", e);
		}
	}

	public <T> T convertTo(Class<T> classToSerialize) {
		try {
			return jsonMapper.readValue(toString(), classToSerialize);
		} catch (IOException e) {
			throw new CoreException("failed.to.convert.object.from.json", e);
		}
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
