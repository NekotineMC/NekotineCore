package fr.nekotine.core.map.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.nekotine.core.map.command.MapElementCommandGenerator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GenerateSpecificCommandFor{
	Class<? extends MapElementCommandGenerator> value();
	String name() default "";
}
