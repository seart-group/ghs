package ch.usi.si.seart.processor;

import ch.usi.si.seart.io.ExternalProcess;
import ch.usi.si.seart.stereotype.Connector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ConnectorAnnotationProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NotNull String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Connector.class)) {
            Connector annotation = bean.getClass().getAnnotation(Connector.class);
            String command = annotation.command();
            String versionFlag = annotation.versionFlag();
            try {
                new ExternalProcess(command, versionFlag).execute().ifFailedThrow(() -> {
                    String template = "The '%s' command is not installed";
                    String message = String.format(template, command);
                    return new BeanInitializationException(message);
                });
            } catch (TimeoutException ex) {
                String template = "Timed out checking '%s'";
                String message = String.format(template, command);
                throw new BeanInitializationException(message, ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                String template = "Interrupted while checking '%s'";
                String message = String.format(template, command);
                throw new BeanInitializationException(message, ex);
            }
        }
        return bean;
    }
}
