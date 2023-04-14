package org.makkiato.arcadeclient.data.repository;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

public class ArcadeclientQueryMethod extends QueryMethod {
    private final Query queryAnnotation;
    private final Class<?> returnType;

    /**
     * Creates a new {@link QueryMethod} from the given parameters. Looks up the correct query to use for following
     * invocations of the method given.
     *
     * @param method   must not be {@literal null}.
     * @param metadata must not be {@literal null}.
     * @param factory  must not be {@literal null}.
     */
    public ArcadeclientQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.queryAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, Query.class);
        this.returnType = method.getReturnType();
    }

    @Override
    protected Parameters<ArcadeclientParameters, ArcadeclientParameter> createParameters(Method method, TypeInformation<?> domainType) {
        return new ArcadeclientParameters(method, domainType);
    }

    public boolean hasQueryAnnotation() {
        return queryAnnotation != null;
    }

    public Query getQueryAnnotation() {
        return queryAnnotation;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    static class ArcadeclientParameters extends Parameters<ArcadeclientParameters, ArcadeclientParameter> {

        protected ArcadeclientParameters(Method method, TypeInformation<?> domainType) {
            super(method, it -> new ArcadeclientParameter(it, domainType));
        }

        public ArcadeclientParameters(List<ArcadeclientParameter> parameters) {
            super(parameters);
        }

        @Override
        protected ArcadeclientParameters createFrom(List<ArcadeclientParameter> parameters) {
            return new ArcadeclientParameters(parameters);
        }
    }

    static class ArcadeclientParameter extends Parameter {

        /**
         * Creates a new {@link Parameter} for the given {@link MethodParameter}.
         *
         * @param parameter  must not be {@literal null}.
         * @param domainType
         */
        protected ArcadeclientParameter(MethodParameter parameter,
                                        Function<MethodParameter, TypeInformation<?>> domainType) {
            super(parameter);
        }

        /**
         * Creates a new {@link Parameter} for the given {@link MethodParameter} and domain {@link TypeInformation}.
         *
         * @param parameter  must not be {@literal null}.
         * @param domainType must not be {@literal null}.
         * @since 3.0.2
         */
        protected ArcadeclientParameter(MethodParameter parameter, TypeInformation<?> domainType) {
            super(parameter, domainType);
        }
    }
}
