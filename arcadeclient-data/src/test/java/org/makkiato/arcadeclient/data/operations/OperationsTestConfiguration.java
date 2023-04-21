package org.makkiato.arcadeclient.data.operations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.makkiato.arcadeclient.data.base.Document;
import org.makkiato.arcadeclient.data.config.ArcadeclientConfigurationSupport;
import org.makkiato.arcadeclient.data.config.ArcadeclientManagedTypes;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.makkiato.arcadeclient.data.core.WebClientFactory;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.mapping.MappingArcadeclientConverter;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@ConfigurationPropertiesScan(basePackageClasses = ArcadedbProperties.class)
public class OperationsTestConfiguration {
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
    public ArcadedbOperations arcadedbOperations(ArcadeclientMappingContext arcadeclientMappingContext, ExchangeFactory exchangeFactory) {
        return new ArcadedbTemplate(null,null,
                new MappingArcadeclientConverter(arcadeclientMappingContext) {
                },
                exchangeFactory);
    }
}
