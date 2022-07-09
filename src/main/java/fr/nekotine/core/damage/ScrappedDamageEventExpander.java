package fr.nekotine.core.damage;

public class ScrappedDamageEventExpander {
	public ScrappedDamageEventExpander(boolean ignoreArmor, boolean knockback) {
		this.ignoreArmor = ignoreArmor;
	}
	
	//
	
	private boolean ignoreArmor;
	private boolean knockback;
	
	//
	
	public void SetIgnoreArmor(boolean ignoreArmor) {
		this.ignoreArmor = ignoreArmor;
	}
	public void SetKnockback(boolean knockback) {
		this.knockback = knockback;
	}
	
	//
	
	public boolean IsIgnoreArmor() {
		return ignoreArmor;
	}
	public boolean IsKnockback() {
		return knockback;
	}
}
