package org.makkiato.arcadeclient.data.core;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReactiveRepositoryFactorySupport;

public class ArcadeclientRepositoryFactory extends ReactiveRepositoryFactorySupport {
    private final ArcadedbOperations connection;

    public ArcadeclientRepositoryFactory(ArcadedbOperations connection) {
        this.connection = connection;
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return null;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, connection);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleArcadedbRepository.class;
    }
}
