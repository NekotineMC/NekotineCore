package fr.nekotine.core.map;

public abstract class MapTypeIdentifier {

	private final String _id;
	
	private final Class<? extends Map> mapType;
	
	public MapTypeIdentifier(String id, Class<? extends Map> mapTypeClass) {
		_id = id;
		mapType = mapTypeClass;
	}
	
	public String getId() {
		return _id;
	}
	
	public Class<? extends Map> getMapTypeClass(){
		return mapType;
	}
	
	public abstract Map generateTypedMap();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapTypeIdentifier mti) {
			return mti._id.contentEquals(_id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	
}
