package fr.nekotine.core.util;

import org.bukkit.Location;
import org.bukkit.Particle;

public class UtilParticle {
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule � afficher
	 * @param particleNumber Nombre de particules par subdivision
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber) {
		Circle2D(center, radius, particleInCircle, particle, particleNumber, 0, 0, 0, 0);
	}
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule � afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param extra Valeur additionnelle, g�n�ralement la vitesse
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber, double extra) {
		Circle2D(center, radius, particleInCircle, particle, particleNumber, 0, 0, 0, extra);
	}
	/**
	 * 
	 * @param center Centre du cercle
	 * @param radius Rayon du cercle
	 * @param particleInCircle Nombre de subdivisions du cercle
	 * @param particle Particule � afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param offsetX D�calage potentiel en x
	 * @param offsetY D�calage potentiel en y
	 * @param offsetZ D�calage potentiel en z
	 * @param extra Valeur additionnelle, g�n�ralement la vitesse
	 */
	public static void Circle2D(Location center, double radius, int particleInCircle, Particle particle, int particleNumber, double offsetX, double offsetY, double offsetZ, double extra) {
		double range = (2 * Math.PI) / particleInCircle;
		for(double n = 0 ; n < 2 * Math.PI ; n+= range) {
			double x = Math.cos(n) * radius;
			double z = Math.sin(n) * radius;
			center.getWorld().spawnParticle(particle, center.clone().add(x, 0, z), particleNumber, offsetX, offsetY, offsetZ, extra);
		}
	}
	
	//
	
	/**
	 * 
	 * @param bottom Hauteur de d�part du vortex
	 * @param radius Rayon des cercles
	 * @param particlePerCircle Nombre de subdivisions des cercles
	 * @param height Hauteur du vortex
	 * @param circleNumber Nombre de cercles du vortex
	 * @param particle Particule � afficher
	 * @param particleNumber Nombre de particules par subdivision
	 * @param offsetX D�calage potentiel en x
	 * @param offsetY D�calage potentiel en y
	 * @param offsetZ D�calage potentiel en z
	 * @param extra Valeur additionnelle, g�n�ralement la vitesse
	 */
	public static void Vortex(Location bottom, double radius, int particlePerCircle, double height, int circleNumber, Particle particle, int particleNumber, double offsetX, double offsetY, double offsetZ, double extra) {
		circleNumber++;
		double range = (double)height / circleNumber;
		for(double n = range; n < height ; n+= range) {
			Circle2D(bottom.clone().add(0, n, 0), radius, particlePerCircle, particle, particleNumber, offsetX, offsetY, offsetZ, extra);
		}
	}
	/**
	 * 
	 * @param bottom Hauteur de d�part du vortex
	 * @param radius Rayon des cercles
	 * @param particlePerCircle Nombre de subdivisions des cercles
	 * @param height Hauteur du vortex
	 * @param circleNumber Nombre de cercles du vortex
	 * @param particle Particule � afficher
	 * @param particleNumber Nombre de particules par subdivision
	 */
	public static void Vortex(Location bottom, double radius, int particlePerCircle, double height, int circleNumber, Particle particle, int particleNumber) {
		Vortex(bottom, radius, particlePerCircle, height, circleNumber, particle, particleNumber, 0, 0, 0, 0);
	}
}
