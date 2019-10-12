package pl.skotar.alfresco.module.jvmconsole.internal;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

class BeanInjector {

    private final AutowireCapableBeanFactory autowireCapableBeanFactory;

    BeanInjector(AutowireCapableBeanFactory autowireCapableBeanFactory) {
        this.autowireCapableBeanFactory = autowireCapableBeanFactory;
    }

    void inject(Object instance) {
        autowireCapableBeanFactory.autowireBean(instance);
    }
}
