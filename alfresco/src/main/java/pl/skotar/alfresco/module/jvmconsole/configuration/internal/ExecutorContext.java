package pl.skotar.alfresco.module.jvmconsole.configuration.internal;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.skotar.alfresco.module.jvmconsole.internal.Executor;

@Configuration
public class ExecutorContext {

    @Bean
    public Executor executor(AutowireCapableBeanFactory autowireCapableBeanFactory) {
        return new Executor(autowireCapableBeanFactory);
    }
}
