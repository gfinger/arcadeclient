package org.makkiato.arcadeclient.data.config;

import org.makkiato.arcadeclient.data.base.IdentifiableDocumentBase;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        componentProvider.addIncludeFilter(((metadataReader, metadataReaderFactory) ->
        {
            try {
                return IdentifiableDocumentBase.class.isAssignableFrom(
                        ClassUtils.forName(metadataReader.getClassMetadata().getClassName(),
                                ArcadeclientConfigurationSupport.class.getClassLoader()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));

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
}
