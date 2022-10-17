package fr.nekotine.core.map;

public abstract class MapTypeIdentifier {

	private final String _id;
	
	private final Class<? extends GameMap> mapType;
	
	public MapTypeIdentifier(String id, Class<? extends GameMap> mapTypeClass) {
		_id = id;
		mapType = mapTypeClass;
	}
	
	public String getId() {
		return _id;
	}
	
	public Class<? extends GameMap> getMapTypeClass(){
		return mapType;
	}
	
	public abstract GameMap generateTypedMap(MapIdentifier id);
	
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
