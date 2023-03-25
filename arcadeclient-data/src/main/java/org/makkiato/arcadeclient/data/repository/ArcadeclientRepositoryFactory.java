package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.operations.ArcadedbOperations;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReactiveRepositoryFactorySupport;

public class ArcadeclientRepositoryFactory extends ReactiveRepositoryFactorySupport {
    private final ArcadedbOperations operations;

    private final MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> mappingContext;

    public ArcadeclientRepositoryFactory(ArcadedbOperations operations) {
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return null;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, operations, metadata.getDomainType());
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleArcadeclientRepository.class;
    }
}
