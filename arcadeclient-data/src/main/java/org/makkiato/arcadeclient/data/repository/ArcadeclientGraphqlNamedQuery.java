package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.mapping.ArcadeclientPersistentEntity;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientPersistentProperty;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.makkiato.arcadeclient.data.operations.GenericOperations;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.util.ReactiveWrappers;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;

import static org.makkiato.arcadeclient.data.repository.ArcadeclientQueryMethod.ArcadeclientParameters;

public class ArcadeclientGraphqlNamedQuery implements RepositoryQuery {
    private final ArcadedbTemplate operations;
    private final MappingContext<? extends ArcadeclientPersistentEntity<?>, ArcadeclientPersistentProperty> mappingContext;
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final ArcadeclientQueryMethod queryMethod;

    public ArcadeclientGraphqlNamedQuery(ArcadedbTemplate operations,
                                         MappingContext<? extends ArcadeclientPersistentEntity<?>,
                                                 ArcadeclientPersistentProperty> mappingContext,
                                         QueryMethodEvaluationContextProvider evaluationContextProvider,
                                         ArcadeclientQueryMethod queryMethod) {
        this.operations = operations;
        this.mappingContext = mappingContext;
        this.evaluationContextProvider = evaluationContextProvider;
        this.queryMethod = queryMethod;
    }

    /**
     * Executes the {@link RepositoryQuery} with the given parameters.
     *
     * @param parameters must not be {@literal null}.
     * @return execution result. Can be {@literal null}.
     */
    @Override
    public Object execute(Object[] parameters) {
        if(!queryMethod.hasQueryAnnotation()) {
            return null;
        }
        var parameterAccessor =
                new ArcadeclientParameterAccessor((ArcadeclientParameters) queryMethod.getParameters(), parameters);
        var formalParameters = parameterAccessor.getParameters();
        var resolvedParameters = new HashMap<String, Object>();
        formalParameters.stream().filter(Parameter::isBindable).forEach(parameter -> {
            var index = parameter.getIndex();
            var value = parameterAccessor.getBindableValue(index);
            parameter.getName().ifPresent(parameterName -> resolvedParameters.put(parameterName, value));
            resolvedParameters.put(Integer.toString(index), value);
        });

        var queryString = String.format("{%s}", queryMethod.getQueryAnnotation().value());
        for (var entry: resolvedParameters.entrySet()) {
            queryString = StringUtils.replace(queryString, String.format("$%s", entry.getKey()),
                    String.format("\"%s\"",entry.getValue().toString()));
        }
        var objectType = queryMethod.getReturnedObjectType();
        var result = operations.select(GenericOperations.CommandLanguage.GRAPHQL, queryString, (Class)objectType);

        if(ReactiveWrappers.usesReactiveType(queryMethod.getReturnType())) {
            if (ReactiveWrappers.isMultiValueType(queryMethod.getReturnType())) {
                return result;
            } else {
                result.elementAt(0, Mono.empty());
            }
        }

        return result;
    }

    /**
     * Returns the related {@link QueryMethod}.
     *
     * @return never {@literal null}.
     */
    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
