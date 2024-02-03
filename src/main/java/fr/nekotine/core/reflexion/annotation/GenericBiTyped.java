package fr.nekotine.core.reflexion.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Permet de savoir les types génériques en runtime
 * 
 * @author XxGoldenbluexX
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenericBiTyped {
	Class<?> a();
	Class<?> b();
	
}
