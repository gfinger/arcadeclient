package org.makkiato.arcadeclient.data.core;

import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class ArcadeclientRepositoriesRegistrar extends AbstractRepositoryConfigurationSourceSupport {
    @Override
    public Class<? extends Annotation> getAnnotation() {
        return EnableArcadeclientRepositories.class;
    }

    @Override
    public Class<?> getConfiguration() {
        return EnableArcadeclientRepositoriesConfiguration.class;
    }

    @Override
    public RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new ArcadeclientConfigurationExtension();
    }

    @EnableArcadeclientRepositories(basePackages = {"org.makkiato"})
    private static class EnableArcadeclientRepositoriesConfiguration {}
}
