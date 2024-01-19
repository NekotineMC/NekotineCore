package fr.nekotine.core.map.save.configurationserialization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapDictKey;
import fr.nekotine.core.map.annotation.MapElementTyped;
import fr.nekotine.core.tuple.Pair;

public class ConfigurationSerializableAdapterSerializer {

	private static ConfigurationSerializableAdapterSerializer instance;
	
	public static ConfigurationSerializableAdapterSerializer getInstance() {
		if (instance == null) {
			instance = new ConfigurationSerializableAdapterSerializer();
		}
		return instance;
	}
	
	// Types qui n'ont pas besoin d'être transformé pour être serializé (un peut comme ConfigurationSerializable)
	private static final Class<?>[] primitiveTypes = new Class<?>[]{
		byte.class,
		short.class,
		int.class,
		long.class,
		float.class,
		double.class,
		boolean.class,
		char.class,
		String.class
	};
	
	private Map<Class<?>, Function<Object, Object>> serializers = new HashMap<>();
	
	private Map<Class<?>, Function<Object, Object>> deserializers = new HashMap<>();
	
	public Function<Object, Object> getSerializerFor(Class<?> clazz) {
		if (!serializers.containsKey(clazz)) {
			serializers.put(clazz, makeSerializerForNode(clazz));
		}
		return serializers.get(clazz);
	}
	
	public Function<Object, Object> getDeserializerFor(Class<?> clazz) {
		if (!deserializers.containsKey(clazz)) {
			deserializers.put(clazz, makeDeserializerForNode(clazz));
		}
		return deserializers.get(clazz);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Function<Object, Object> makeSerializerForNode(Class<?> node){
		// ConfigurationSerializable
		if (ConfigurationSerializable.class.isAssignableFrom(node)) {
			return obj -> obj != null ? ((ConfigurationSerializable)obj).serialize() : null;
		}
		// primitive types
		for (var t : primitiveTypes) {
			if (t.isAssignableFrom(node)) {
				return Function.identity();
			}
		}
		// Composed type (dict or Component)
		var fieldsFunctions = new LinkedList<Pair<String,Function<Object, Map<String, Object>>>>();
		for (var field : node.getDeclaredFields()) {
			if (field.isAnnotationPresent(ComposingMap.class)) {
				if (field.isAnnotationPresent(MapDictKey.class)) {
					continue; // Pas besoin de stocker le champ si c'est déja la clef d'un dictionnaire
				}
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingMap.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				// special Dictionary case
				if (Map.class.isAssignableFrom(fieldType)) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapElementTyped.class)) {
						var annotation = field.getAnnotation(MapElementTyped.class);
						var typeDict = annotation.value();
						var funcDict = makeSerializerForNode(typeDict); // TODO Pk pas getSerializerForNode?
						fieldsFunctions.add(new Pair(name,(Function<Object, Object>)obj -> {
							try {
								if (obj == null) {
									return null;
								}
								var map = new HashMap<String, Object>();
								var backingMap = ((Map)(field.get(obj)));
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
					fieldsFunctions.add(new Pair(name,(Function<Object, Object>)obj ->{
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
	private Function<Object, Object> makeDeserializerForNode(Class<?> node){
		// ConfigurationSerializable
		if (ConfigurationSerializable.class.isAssignableFrom(node)) {
			return map -> map != null && map instanceof Map<?,?> ? ConfigurationSerialization.deserializeObject((Map<String,Object>)map, (Class<? extends ConfigurationSerializable>)node) : null;
		}
		// Primitive type
		for (var t : primitiveTypes) {
			if (t.isAssignableFrom(node)) {
				return Function.identity();
			}
		}
		// Composed type (dict or Component)
		var fieldsFunctions = new LinkedList<BiConsumer<Object,Object>>();
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
				if (Map.class.isAssignableFrom(fieldType)) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(MapElementTyped.class)) {
						var annotation = field.getAnnotation(MapElementTyped.class);
						var typeDict = annotation.value();
						var funcDict = makeDeserializerForNode(typeDict);
						fieldsFunctions.add((parent,map) -> {
							try {
								if (map == null || !(map instanceof Map<?,?> realMap)) {
									return;
								}
								var fieldMap = (Map<String, Object>) realMap.get(finalName);
								if (fieldMap == null) {
									return;
								}
								var backing = (Map<String,Object>)field.get(parent);
								for (var key : fieldMap.keySet()) {
									var keyMap = (Map<String, Object>) fieldMap.get(key);
									keyMap.put("MapDictKey", key);
									backing.put(key, funcDict.apply(keyMap));
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
					if (field.isAnnotationPresent(MapDictKey.class)) {
						fieldsFunctions.add((parent,map) ->{
							if (map == null || !(map instanceof Map<?,?> realMap)) {
								return;
							}
							try {
								var obj = realMap.get("MapDictKey");
								field.set(parent, obj);
							}catch(Exception e) {
								throw new RuntimeException(e);
							}
						});
					}else {
						var nodeDeserializer = getDeserializerFor(field.getType());
						fieldsFunctions.add((parent,map) ->{
							if (map == null || !(map instanceof Map<?,?> realMap)) {
								return;
							}
							try {
								var obj = nodeDeserializer.apply(realMap.get(finalName));
								field.set(parent, obj);
							}catch(Exception e) {
								throw new RuntimeException(e);
							}
						});
					}
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
