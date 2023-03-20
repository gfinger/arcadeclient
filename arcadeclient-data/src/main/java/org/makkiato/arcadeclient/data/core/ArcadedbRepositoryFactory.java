package org.makkiato.arcadeclient.data.core;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class ArcadedbRepositoryFactory extends RepositoryFactorySupport {

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEntityInformation'");
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTargetRepository'");
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRepositoryBaseClass'");
    }
    
}
