package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.base.DocumentBase;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@NoRepositoryBean
public interface ArcadeclientCrudRepository<T extends DocumentBase> extends ReactiveCrudRepository<T, String> {

}
