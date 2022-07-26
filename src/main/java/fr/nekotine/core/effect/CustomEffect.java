package fr.nekotine.core.effect;

public class CustomEffect {

	private CustomEffectType _type;
	
	private int _amplifier;
	
	private int _duration;
	
	private CustomEffect(CustomEffectType type, int amplifier, int duration) {
		_type = type;
		_amplifier = amplifier;
		_duration = duration;
	}

	public CustomEffectType getType() {
		return _type;
	}

	public void setType(CustomEffectType _type) {
		this._type = _type;
	}

	public int getAmplifier() {
		return _amplifier;
	}

	public void setAmplifier(int _amplifier) {
		this._amplifier = _amplifier;
	}

	public int getDuration() {
		return _duration;
	}

	public void setDuration(int _duration) {
		this._duration = _duration;
	}
	
}
