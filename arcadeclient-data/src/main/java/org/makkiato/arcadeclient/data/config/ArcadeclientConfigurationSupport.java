package org.makkiato.arcadeclient.data.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.core.Arcadeclient;
import org.makkiato.arcadeclient.data.core.ConnectionProperties;
import org.makkiato.arcadeclient.data.core.WebClientSupplierFactory;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.mapping.MappingArcadeclientConverter;
import org.makkiato.arcadeclient.data.operations.ArcadedbOperations;
import org.makkiato.arcadeclient.data.operations.ArcadedbTemplate;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilterImpl;
import org.makkiato.arcadeclient.data.web.client.HALeaderWebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.client.WebClientSupplierStrategy;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public abstract class ArcadeclientConfigurationSupport {
    protected Collection<String> getMappingBasePackages() {
        Package mappingBasePackage = getClass().getPackage();
        return Collections.singleton(mappingBasePackage == null ? null : mappingBasePackage.getName());
    }

    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        var initialEntitySet = new HashSet<Class<?>>();
        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    protected Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        var initialEntitySet = new HashSet<Class<?>>();
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));

        for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {

            initialEntitySet
                    .add(ClassUtils.forName(candidate.getBeanClassName(),
                            ArcadeclientConfigurationSupport.class.getClassLoader()));
        }
        return initialEntitySet;
    }

    @Bean
    public ArcadeclientMappingContext arcadeclientMappingContext(ArcadeclientManagedTypes managedTypes) {
        var mappingContext = new ArcadeclientMappingContext();
        mappingContext.setManagedTypes(managedTypes);
        return mappingContext;
    }

    @Bean
    public ArcadeclientManagedTypes arcadeclientManagedTypes() throws ClassNotFoundException {
        return ArcadeclientManagedTypes.fromIterable(getInitialEntitySet());
    }

    @Bean
    public ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter() {
        return new ArcadeclientErrorResponseFilterImpl();
    }

    @Bean
    public WebClientSupplierStrategy webClientSupplierStrategy() {
        return new HALeaderWebClientSupplierStrategy();
    }

    @Bean
    public WebClientSupplierFactory webClientFactory(ArcadeclientErrorResponseFilter arcadedbErrorResponseFilter,
            WebClientSupplierStrategy webClientSupplierStrategy) {
        return new WebClientSupplierFactory(arcadedbErrorResponseFilter, webClientSupplierStrategy);
    }

    @Bean
    public Arcadeclient arcadedbFactory(WebClientSupplierFactory webClientFactory,
            ConnectionProperties connectionProperties) {
        return new Arcadeclient(webClientFactory.getWebClientSupplierFor(connectionProperties));
    }

    @Bean
    public ExchangeFactory exchangeFactory() {
        return new ExchangeFactory();
    }

    @Bean
    public ArcadedbOperations arcadedbOperations(Arcadeclient arcadeclientFactory,
            ArcadeclientMappingContext arcadeclientMappingContext, ExchangeFactory exchangeFactory) {
        return new ArcadedbTemplate(arcadeclientFactory,
                new MappingArcadeclientConverter(arcadeclientMappingContext) {
                },
                exchangeFactory);
    }
}
