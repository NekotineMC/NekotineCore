package fr.nekotine.core.serialization.configurationserializable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import fr.nekotine.core.defaut.IDefaultProvider;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.reflexion.annotation.GenericBiTyped;
import fr.nekotine.core.serialization.configurationserializable.annotation.ComposingConfiguration;
import fr.nekotine.core.serialization.configurationserializable.annotation.MapDictKey;
import fr.nekotine.core.tuple.Pair;

public class ConfigurationSerializableAdapterSerializer implements IConfigurationSerializableAdapterContainer{
	
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
	
	private static final BiFunction<Object,Object,Object> biIdentity = (instance,map) -> instance;
	
	private Map<Class<?>, Function<Object, Object>> serializers = new HashMap<>();
	
	private Map<Class<?>, BiFunction<Object, Object, Object>> deserializers = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public Function<Object, Map<String,Object>> getSerializerFor(Class<?> clazz) {
		// On peut pas cast, obligé de wrap, fait chier java
		var uncastableSerializer = getSerializerForInternal(clazz);
		return (o) -> {
			return (Map<String, Object>) uncastableSerializer.apply(o);
		};
	}
	
	@Override
	public Function<Map<String,Object>, Object> getDeserializerFor(Class<?> clazz) {
		final var defaulterFinal = providerFor(clazz);
		var uncastableDeserializer = getDeserializerForInternal(clazz);
		// On peut pas cast, obligé de wrap, fait chier java
		return (map) -> {
			return uncastableDeserializer.apply(defaulterFinal.apply(map),map);
		};
	}
	
	@Override
	public BiFunction<Object,Map<String,Object>, Object> getDeserializerApplierFor(Class<?> clazz) {
		// On peut pas cast, obligé de wrap, fait chier java
		var uncastableDeserializer = getDeserializerForInternal(clazz);
		return (instance, o) -> {
			return uncastableDeserializer.apply(instance,o);
		};
	}
	
	private Function<Object, Object> providerFor(Class<?> clazz){
		var defaultProvider = Ioc.resolve(IDefaultProvider.class).getSupplier(clazz);
		Function<Object,Object> defaulter = null;
		if (defaultProvider != null) {
			defaulter = (map) -> defaultProvider.get();
		}
		if (defaulter == null && !Modifier.isAbstract(clazz.getModifiers()) && ConfigurationSerializable.class.isAssignableFrom(clazz)) {
			Constructor<?> ctor;
			try {
				ctor = clazz.getConstructor(Map.class);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Impossible de construire une instance de "+clazz.getTypeName(),e);
			}
			defaulter = (map) -> {
				try {
					return ctor.newInstance(map);
				} catch (Exception e) {
					return null;
				}
			};
		}
		return defaulter;
	}
	
	private Function<Object, Object> getSerializerForInternal(Class<?> clazz) {
		if (!serializers.containsKey(clazz)) {
			serializers.put(clazz, makeSerializerForNode(clazz));
		}
		return serializers.get(clazz);
	}
	
	private BiFunction<Object,Object, Object> getDeserializerForInternal(Class<?> clazz) {
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
			if (field.isAnnotationPresent(ComposingConfiguration.class)) {
				if (field.isAnnotationPresent(MapDictKey.class)) {
					continue; // Pas besoin de stocker le champ si c'est déja la clef d'un dictionnaire
				}
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingConfiguration.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				// special Dictionary case
				if (Map.class.isAssignableFrom(fieldType)) {
					if (field.isAnnotationPresent(GenericBiTyped.class)) {
						var annotation = field.getAnnotation(GenericBiTyped.class);
						var typeDict = annotation.b();
						var funcDict = getSerializerForInternal(typeDict);
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
					var nodeSerializer = getSerializerForInternal(field.getType());
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
					throw new RuntimeException("Serialization issue for node "+node.getTypeName(), e);
				}
			}
			return map;
		};
	}
	
	@SuppressWarnings("unchecked")
	private BiFunction<Object,Object, Object> makeDeserializerForNode(Class<?> node){
		// ConfigurationSerializable
		if (ConfigurationSerializable.class.isAssignableFrom(node) && !ConfigurationSerializableAdapted.class.isAssignableFrom(node)) {
			return (instnace,map) -> map != null && map instanceof Map<?,?> ? ConfigurationSerialization.deserializeObject((Map<String,Object>)map, (Class<? extends ConfigurationSerializable>)node) : null;
		}
		// Primitive type
		for (var t : primitiveTypes) {
			if (t.isAssignableFrom(node)) {
				return biIdentity;
			}
		}
		// Composed type (dict or Component)
		var fieldsFunctions = new LinkedList<BiConsumer<Object,Object>>();
		for (var field : node.getDeclaredFields()) {
			field.trySetAccessible();
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
			}
			else if (field.isAnnotationPresent(ComposingConfiguration.class)) {
				var fieldType = field.getType();
				field.trySetAccessible();
				var name = field.getAnnotation(ComposingConfiguration.class).value();
				if (name.isBlank()) {
					name = field.getName();
				}
				final var finalName = name;
				// special Dictionary case
				if (Map.class.isAssignableFrom(fieldType)) { // Type précis pour permettre l'héritage par l'utilisateur
					if (field.isAnnotationPresent(GenericBiTyped.class)) {
						var annotation = field.getAnnotation(GenericBiTyped.class);
						var typeDict = annotation.b();
						var funcDict = getDeserializerForInternal(typeDict);
						var provider = providerFor(typeDict);
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
								if (backing == null) {
									backing = new HashMap<>();
									field.set(parent, backing);
								}
								for (var key : fieldMap.keySet()) {
									var keyMap = (Map<String, Object>) fieldMap.get(key);
									keyMap.put("MapDictKey", key);
									var res = funcDict.apply(provider.apply(keyMap),keyMap);
									backing.put(key, res);
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
					var nodeDeserializer = getDeserializerForInternal(fieldType);
					var provider = providerFor(fieldType);
					fieldsFunctions.add((parent,map) ->{
						if (map == null || !(map instanceof Map<?,?> realMap)) {
							return;
						}
						try {
							var innerMap = realMap.get(finalName);
							var obj = nodeDeserializer.apply(provider.apply(innerMap),innerMap);
							field.set(parent, obj);
						}catch(Exception e) {
							throw new RuntimeException(e);
						}
					});
				}
			}
		}
		try {
			return (instance,map) -> {
				if (map == null) {
					return null;
				}
				try {
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
