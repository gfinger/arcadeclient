package org.makkiato.arcadeclient.data.repository;

import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.*;

import static org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.data.repository.query.QueryLookupStrategy.Key;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ArcadeclientRepositoriesRegistrar.class)
public @interface EnableArcadeclientRepositories {
    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableReactiveMongoRepositories("org.my.pkg")} instead of
     * {@code @EnableReactiveMongoRepositories(basePackages="org.my.pkg")}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
     * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
     * each package that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
     * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
     */
    Filter[] includeFilters() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     */
    Filter[] excludeFilters() default {};

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
     * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
     * for {@code PersonRepositoryImpl}.
     *
     * @return {@literal Impl} by default.
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties file. Will default to
     * {@code META-INF/mongo-named-queries.properties}.
     *
     * @return empty {@link String} by default.
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
     * {@link Key#CREATE_IF_NOT_FOUND}.
     *
     * @return {@link Key#CREATE_IF_NOT_FOUND} by default.
     */
    Key queryLookupStrategy() default Key.USE_DECLARED_QUERY;

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
     * {@link ArcadeclientRepositoryFactoryBean}.
     *
     * @return {@link ArcadeclientRepositoryFactoryBean} by default.
     */
    Class<?> repositoryFactoryBeanClass() default ArcadeclientRepositoryFactoryBean.class;

    /**
     * Configure the repository base class to be used to create repository proxies for this particular configuration.
     *
     * @return {@link DefaultRepositoryBaseClass} by default.
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    /**
     * Whether to automatically create indexes for query methods defined in the repository interface.
     *
     * @return {@literal false} by default.
     */
    boolean createIndexesForQueryMethods() default false;

    /**
     * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
     * repositories infrastructure.
     *
     * @return {@literal false} by default.
     */
    boolean considerNestedRepositories() default false;

    /**
     * Configures the name of the {@link ArcadedbTemplate} bean to be used with the repositories detected.
     *
     * @return {@literal arcadedbConnection} by default.
     */
    String arcadedbTemplateRef() default "arcadedbConnection";
}
