package ch.usi.si.seart.stereotype;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "Job", which we define as a component that encapsulates
 * processes executed by a {@link org.springframework.scheduling.TaskScheduler TaskScheduler}.
 *
 * <p>Classes making use of this annotation should both implement the {@link Runnable} interface,
 * as well as annotate the implemented {@link Runnable#run() run()} method with Spring's
 * {@link org.springframework.scheduling.annotation.Scheduled Scheduled} annotation.
 * While this is not a contractual requirement, it does make sure that the job is
 * eligible both for automatic, and manual scheduling.
 *
 * <p>This annotation serves as a specialization of {@link Component @Component},
 * allowing for implementation classes to be autodetected through classpath scanning.
 *
 * @author Ozren Dabić
 * @see org.springframework.stereotype.Component Component
 * @see org.springframework.stereotype.Service Service
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Job {

    /**
     * The value may indicate a suggestion for a logical component name,
     * to be turned into a Spring bean in case of an autodetected component.
     * @return the suggested component name, if any (or empty String otherwise)
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
