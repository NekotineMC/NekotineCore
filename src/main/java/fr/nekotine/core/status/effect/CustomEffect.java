package fr.nekotine.core.status.effect;

public class CustomEffect {

	private CustomEffectType _type;
	
	private int _amplifier;
	
	private int _duration;
	
	public CustomEffect(CustomEffectType type, int amplifier, int duration) {
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

	/**
	 * Durée de l'effet en tick server (20 par secondes)
	 * @return
	 */
	public int getDuration() {
		return _duration;
	}

	/**
	 * Durée de l'effet en tick server (20 par secondes)
	 * @param _duration
	 */
	public void setDuration(int _duration) {
		this._duration = _duration;
	}
	
	protected void tick() {
		
	}
	
}
