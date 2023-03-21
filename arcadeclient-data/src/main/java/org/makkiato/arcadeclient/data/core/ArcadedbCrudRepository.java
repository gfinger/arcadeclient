package org.makkiato.arcadeclient.data.core;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@NoRepositoryBean
public interface ArcadedbCrudRepository<T, ID> extends ReactiveCrudRepository<T, ID> {

}
