package fr.nekotine.core.map.save.saver.configurationserializable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import fr.nekotine.core.map.MapMetadata;
import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapDictionaryElementType;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.tuple.Pair;

public class ConfigurationSerializableAdapterSerializer {

	private static ConfigurationSerializableAdapterSerializer instance;
	
	public static ConfigurationSerializableAdapterSerializer getInstance() {
		if (instance == null) {
			instance = new ConfigurationSerializableAdapterSerializer();
		}
		return instance;
	}
	
	private ConfigurationSerializableAdapterSerializer() {
		//ConfigurationSerialization.registerClass(ConfigurationSerializableAdapter.class, "ConfigurationSerializableAdapter");
		ConfigurationSerialization.registerClass(ConfigurationSerializableAdapter.class);
		ConfigurationSerialization.registerClass(MapMetadata.class);
	}
	
	private Map<Class<?>, Function<Object, Map<String, Object>>> serializers = new HashMap<>();
	
	private Map<Class<?>, Function<Map<String, Object>, Object>> deserializers = new HashMap<>();
	
	public Function<Object, Map<String, Object>> getSerializerFor(Class<?> clazz) {
		if (!serializers.containsKey(clazz)) {
			serializers.put(clazz, makeSerializerForNode(clazz));
		}
		return serializers.get(clazz);
	}
	
	public Function<Map<String, Object>, Object> getDeserializerFor(Class<?> clazz) {
		if (!deserializers.containsKey(clazz)) {
			deserializers.put(clazz, makeDeserializerForNode(clazz));
		}
		return deserializers.get(clazz);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Function<Object, Map<String, Object>> makeSerializerForNode(Class<?> node){
		if (ConfigurationSerializable.class.isAssignableFrom(node)) {
			return obj -> obj != null ? ((ConfigurationSerializable)obj).serialize() : null;
		}
		var fieldsFunctions = new LinkedList<Pair<String,Function<Object, Map<String, Object>>>>();
		for (var field : node.getDeclaredFields()) {
			if (field.isAnnotationPresent(ComposingMap.class)) {
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingMap.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				// special Dictionary case
				if (MapDictionaryElement.class == fieldType) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapDictionaryElementType.class)) {
						var annotation = field.getAnnotation(MapDictionaryElementType.class);
						var typeDict = annotation.value();
						var funcDict = makeSerializerForNode(typeDict);
						fieldsFunctions.add(new Pair(name,(Function<Object, Map<String, Object>>)obj -> {
							try {
								if (obj == null) {
									return null;
								}
								var map = new HashMap<String, Object>();
								var backingMap = ((MapDictionaryElement)(field.get(obj))).backingMap();
								for (Entry entry : (Set<Entry>)backingMap.entrySet()) {
									entry.getKey();
									map.put((String)entry.getKey(), funcDict.apply(entry.getValue()));
								}
								return map;
							}catch(Exception e) {
								throw new RuntimeException(e);
							}
							}));
					}else {
						var msg = "[ConfigurationSerializable]->Default Le champ %s dans %s est de type dictionnaire mais n'a"
								+ " pas l'annotation MapDictionaryElementType nécessaire pour sa génération.";
						throw new IllegalArgumentException(String.format(msg,name,node.getName()));
					}
				}else {
					var nodeSerializer = getSerializerFor(field.getType());
					fieldsFunctions.add(new Pair(name,(Function<Object, Map<String, Object>>)obj ->{
						if (obj == null) {
							return null;
						}
						try {
							return nodeSerializer.apply(field.get(obj));
						}catch(Exception e) {
							throw new RuntimeException(e);
						}
					}));
				}
			}
		}
		return obj -> {
			var map = new HashMap<String,Object>();
			if (obj == null) {
				return null;
			}
			for (var pair : fieldsFunctions) {
				try {
				map.put(pair.a(), pair.b().apply(obj));
				}catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			return map;
		};
	}
	
	@SuppressWarnings("unchecked")
	private Function<Map<String, Object>, Object> makeDeserializerForNode(Class<?> node){
		if (ConfigurationSerializable.class.isAssignableFrom(node)) {
			return map -> map != null ? ConfigurationSerialization.deserializeObject(map, (Class<? extends ConfigurationSerializable>)node) : null;
		}
		var fieldsFunctions = new LinkedList<BiConsumer<Object,Map<String, Object>>>();
		for (var field : node.getDeclaredFields()) {
			if (field.isAnnotationPresent(ComposingMap.class)) {
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingMap.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				final var finalName = name;
				// special Dictionary case
				if (MapDictionaryElement.class == fieldType) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapDictionaryElementType.class)) {
						var annotation = field.getAnnotation(MapDictionaryElementType.class);
						var typeDict = annotation.value();
						var funcDict = makeDeserializerForNode(typeDict);
						fieldsFunctions.add((parent,map) -> {
							try {
								if (map == null) {
									return;
								}
								var fieldMap = (Map<String, Object>) map.get(finalName);
								if (fieldMap == null) {
									return;
								}
								var dict = new MapDictionaryElement<>();
								var backing = dict.backingMap();
								for (var key : map.keySet()) {
									backing.put(key, funcDict.apply((Map<String, Object>) fieldMap.get(key)));
								}
							}catch(Exception e) {
								throw new RuntimeException(e);
							}
							});
					}else {
						var msg = "[ConfigurationSerializable]->Default Le champ %s dans %s est de type dictionnaire mais n'a"
								+ " pas l'annotation MapDictionaryElementType nécessaire pour sa génération.";
						throw new IllegalArgumentException(String.format(msg,name,node.getName()));
					}
				}else {
					var nodeDeserializer = getDeserializerFor(field.getType());
					fieldsFunctions.add((parent,map) ->{
						if (map == null) {
							return;
						}
						try {
							var obj = nodeDeserializer.apply((Map<String, Object>) map.get(finalName));
							field.set(parent, obj);
						}catch(Exception e) {
							throw new RuntimeException(e);
						}
					});
				}
			}
		}
		try {
			var ctor = node.getConstructor();
			return map -> {
				if (map == null) {
					return null;
				}
				try {
					var instance = ctor.newInstance();
					for (var func : fieldsFunctions) {
						func.accept(instance, map);
					}
					return instance;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
