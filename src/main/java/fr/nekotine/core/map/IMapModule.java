package fr.nekotine.core.map;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

import fr.nekotine.core.module.IPluginModule;

/**
 * Module synchrone pour récupérer charger et sauvegarder des cartes et leurs composants
 * 
 * @author XxGoldenbluexX
 * 
 */
public interface IMapModule extends IPluginModule {

	/**
	 * Retourne la liste des maps présentes sur le serveur.
	 * @return Une collection avec la metadata des cartes.
	 */
	public @Nonnull Collection<MapMetadata> listMaps();
	
	/**
	 * Obtient la metadata de la carte si elle est sur le serveur.
	 * @param name Le nom de la carte à trouver.
	 * @return La matedata de la carte, ou null si elle n'est pas sur le serveur.
	 */
	public @Nullable MapMetadata getMapMetadata(@Nonnull String name);
	
	/**
	 * Sauvegarde la metadata de la carte, la créée si elle n'existe pas.
	 * @param metadata La metadata de la carte
	 */
	public void saveMapMetadata(@Nonnull MapMetadata metadata);
	
	/**
	 * Obtient la structure de la carte si elle existe.
	 * @param metadata La metadata de la carte.
	 * @return La structure de la carte si elle existe.
	 */
	public @Nullable Clipboard getStructure(@Nonnull MapMetadata metadata);
	
	/**
	 * Sauvegarde la structure de la carte, la créée si elle n'existe pas.
	 * @param metadata La metadata de la carte.
	 * @param structure La structure de la carte.
	 */
	public void saveStructure(@Nonnull MapMetadata metadata, Clipboard structure);
	
	
	/**
	 * Obtient le contenu de la carte si elle existe.
	 * @param <T> Le type du contenu de la carte.
	 * @param metadata La metadata de la carte.
	 * @param mapType Le type du contenu de la carte.
	 * @return Le contenu de la carte si elle existe.
	 */
	public <T> @Nullable T getContent(@Nonnull MapMetadata metadata, @Nonnull Class<T> contentType);
	
	/**
	 * Sauvegarde le contenu de la carte, la créée si elle n'existe pas.
	 * @param <T> Le type du contenu de la carte.
	 * @param metadata La metadata de la carte.
	 */
	public <T> void saveContent(@Nonnull MapMetadata metadata, T content);
	
	/**
	 * Supprime la carte et tout son contenu si elle existe.
	 * @param metadata La metadata de la carte
	 */
	public void deleteMap(@Nonnull MapMetadata metadata);

}
