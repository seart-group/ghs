package ch.usi.si.seart.stereotype;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "Connector", which we define as a component serving
 * as a wrapper for terminal-executed commands.
 *
 * <p>Classes designated as connectors have a direct dependency on the specified terminal program.
 * To ensure that this dependency is satisfied, we use a custom
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}.
 *
 * <p>This annotation serves as a specialization of {@link Component @Component},
 * allowing for implementation classes to be autodetected through classpath scanning.
 *
 * @author Ozren Dabić
 * @see ch.usi.si.seart.processor.ConnectorAnnotationProcessor ConnectorAnnotationProcessor
 * @see org.springframework.stereotype.Component Component
 * @see org.springframework.stereotype.Service Service
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Connector {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * Specifies the terminal command associated with the connector.
     *
     * @return The terminal command.
     */
    String command();

    /**
     * Specifies the flag used to check the version of the associated terminal command.
     *
     * @return The version flag string.
     */
    String versionFlag() default "--version";
}
