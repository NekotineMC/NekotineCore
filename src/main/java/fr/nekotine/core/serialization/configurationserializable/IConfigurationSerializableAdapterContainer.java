package fr.nekotine.core.serialization.configurationserializable;

import java.util.Map;
import java.util.function.Function;

public interface IConfigurationSerializableAdapterContainer {

	public Function<Object, Map<String,Object>> getSerializerFor(Class<?> clazz);
	
	public Function<Map<String,Object>, Object> getDeserializerFor(Class<?> clazz);
	
}
