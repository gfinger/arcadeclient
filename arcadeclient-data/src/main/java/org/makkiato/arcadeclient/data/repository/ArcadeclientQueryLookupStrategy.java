package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientPersistentEntity;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientPersistentProperty;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class ArcadeclientQueryLookupStrategy implements QueryLookupStrategy {
    private final ArcadedbTemplate operations;
    private final MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> mappingContext;
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;

    public ArcadeclientQueryLookupStrategy(ArcadedbTemplate operations,
                                           MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> mappingContext,
                                           QueryMethodEvaluationContextProvider evaluationContextProvider) {

        this.operations = operations;
        this.mappingContext = mappingContext;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    /**
     * Resolves a {@link RepositoryQuery} from the given {@link QueryMethod} that can be executed afterwards.
     *
     * @param method       will never be {@literal null}.
     * @param metadata     will never be {@literal null}.
     * @param factory      will never be {@literal null}.
     * @param namedQueries will never be {@literal null}.
     * @return
     */
    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                        NamedQueries namedQueries) {
        var queryMethod = new ArcadeclientQueryMethod(method, metadata, factory);
        if (queryMethod.hasQueryAnnotation()) {
            return new ArcadeclientGraphqlNamedQuery(operations, mappingContext, evaluationContextProvider, queryMethod);
        }
        return null;
    }
}
