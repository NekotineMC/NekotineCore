package fr.nekotine.core.map.save.saver.configurationserializable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import fr.nekotine.core.map.annotation.ComposingMap;
import fr.nekotine.core.map.annotation.MapDictionaryElementType;
import fr.nekotine.core.map.element.MapDictionaryElement;
import fr.nekotine.core.tuple.Triplet;

public class ConfigurationSerializableAdapterSerializer {

	private static ConfigurationSerializableAdapterSerializer instance;
	
	public static ConfigurationSerializableAdapterSerializer getInstance() {
		if (instance == null) {
			instance = new ConfigurationSerializableAdapterSerializer();
		}
		return instance;
	}
	
	private ConfigurationSerializableAdapterSerializer() {
		ConfigurationSerialization.registerClass(ConfigurationSerializableAdapter.class, "ConfigurationSerializableAdapter");
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
		var fieldsFunctions = new LinkedList<Triplet<String,Field,Function<Object, Map<String, Object>>>>();
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
						fieldsFunctions.add(new Triplet(name,field,(Function<Object, Map<String, Object>>)obj -> {
							try {
								if (obj == null) {
									return null;
								}
								System.out.printf("obj type is %s\n",obj.getClass().getName());
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
					var nodeSerializer = makeSerializerForNode(field.getType());
					fieldsFunctions.add(new Triplet(name,field,(Function<Object, Map<String, Object>>)obj ->{
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
			System.out.printf("Processing %s:\n", node.getName());
			if (obj == null) {
				return null;
			}
			for (var triplet : fieldsFunctions) {
				System.out.printf("field %s\n", triplet.a());
				try {
				map.put(triplet.a(), triplet.c().apply(obj));
				}catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			return map;
		};
	}
	
	private Function<Map<String, Object>, Object> makeDeserializerForNode(Class<?> node){
		if (ConfigurationSerializable.class.isAssignableFrom(node)) {
			return ConfigurationSerialization::deserializeObject;
		}
		return null;
	}
}
