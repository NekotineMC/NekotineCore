package fr.nekotine.core.game.phase.reason;

public enum PhaseFailureType {
	
	/**
	 * La phase est annulée, les modifications qu'elle a apportés sont annulés
	 * ({@link fr.nekotine.core.game.phase.Phase Phase}.{@link fr.nekotine.core.game.phase.Phase#handleCancelation() handleCancelation()}).
	 */
	CANCELED,
	/**
	 * La phase se termine prématurément. Contrairement à {@link #CANCELED}, l'état est laissé tel quel. 
	 */
	ABORTED;
	
}
