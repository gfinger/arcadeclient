package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class ArcadeclientRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends RepositoryFactoryBeanSupport<T, S, ID> {
    private ArcadedbTemplate operations;

    protected ArcadeclientRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    public void setArcadedbOperations(ArcadedbTemplate operations) {
        this.operations = operations;
    }

    @Override
    public RepositoryFactorySupport createRepositoryFactory() {
        return getFactoryInstance();
    }

    protected RepositoryFactorySupport getFactoryInstance() {
        return new ArcadeclientRepositoryFactory(operations);
    }
}
