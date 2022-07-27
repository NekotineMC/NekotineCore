package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.Particle;

public class UtilParticle {
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule à afficher
	 * @param particleNumber Nombre de particules par subdivision
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber) {
		Circle2D(center, radius, particleInCircle, particle, particleNumber, 0, 0, 0, 0, null);
	}
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule à afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param extra Valeur additionnelle, généralement la vitesse
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber, double extra) {
		Circle2D(center, radius, particleInCircle, particle, particleNumber, 0, 0, 0, extra, null);
	}
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule à afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param offsetX Décalage potentiel en x
	 * @param offsetY Décalage potentiel en y
	 * @param offsetZ Décalage potentiel en z
	 * @param extra Valeur additionnelle, généralement la vitesse
	 * @param data Données additionelles pour les particules
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
		double range = (2 * Math.PI) / particleInCircle;
		for(double n = 0 ; n < 2 * Math.PI ; n+= range) {
			double x = Math.cos(n) * radius;
			double z = Math.sin(n) * radius;
			center.getWorld().spawnParticle(particle, center.clone().add(x, 0, z), particleNumber, offsetX, offsetY, offsetZ, extra, data);
			
		}
	}
	
	//
	
	/**
	 * 
	 * @param bottom Hauteur de départ du vortex
	 * @param radius Rayon des cercles
	 * @param particlePerCircle Nombre de subdivisions des cercles
	 * @param height Hauteur du vortex
	 * @param circleNumber Nombre de cercles du vortex
	 * @param particle Particule à afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param offsetX Décalage potentiel en x
	 * @param offsetY Décalage potentiel en y
	 * @param offsetZ Décalage potentiel en z
	 * @param extra Valeur additionnelle, généralement la vitesse
	 * @param data Données additionelles pour les particules
	 */
	public static void Vortex(Location bottom, double radius, int particlePerCircle, double height, int circleNumber, Particle particle, int particleNumber, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
		circleNumber++;
		double range = (double)height / circleNumber;
		for(double n = range; n < height ; n+= range) {
			Circle2D(bottom.clone().add(0, n, 0), radius, particlePerCircle, particle, particleNumber, offsetX, offsetY, offsetZ, extra, data);
		}
	}
	/**
	 * 
	 * @param bottom Hauteur de départ du vortex
	 * @param radius Rayon des cercles
	 * @param particlePerCircle Nombre de subdivisions des cercles
	 * @param height Hauteur du vortex
	 * @param circleNumber Nombre de cercles du vortex
	 * @param particle Particule à afficher
	 * @param particleNumber Nombre de particules par subdivision
	 */
	public static void Vortex(Location bottom, double radius, int particlePerCircle, double height, int circleNumber, Particle particle, int particleNumber) {
		Vortex(bottom, radius, particlePerCircle, height, circleNumber, particle, particleNumber, 0, 0, 0, 0, null);
	}
}
