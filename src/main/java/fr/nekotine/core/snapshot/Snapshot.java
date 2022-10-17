package fr.nekotine.core.snapshot;

/**
 * Outil permettant de sauvegarder l'état d'un objet.
 * @author XxGoldenbluexX
 *
 * @param <Type>
 */
public interface Snapshot<Type> {

	/**
	 * Sauvegarde le contenu de l'objet selon les spécifications de ce type de snapshot.
	 * Contrairement à une copie profonde, le contenu de la capture est partagé avec celui de l'objet dont est issu la capture.
	 * Le contenu de la capture est donc suceptible de changer au cours du temps.
	 * @param item
	 * @return la même instance de cette snapshot.
	 */
	public Snapshot<Type> snapshot(Type item);
	
	/**
	 * Sauvegarde le contenu de l'objet selon les spécifications de ce type de snapshot.
	 * Contrairement à une copie de surface, le contenu de la capture est unique et non partagé avec celui de l'objet dont est issu la capture.
	 * Le contenu de la capture n'est donc pas suceptible de changer au cours du temps.
	 * @param item
	 * @return la même instance de cette snapshot.
	 */
	public Snapshot<Type> deepSnapshot(Type item);
	
	/**
	 * Applique le contenu de la capture sur l'objet donné.
	 * @param item
	 */
	public void patch(Type item);
	
}
