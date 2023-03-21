package org.makkiato.arcadeclient.data.core;

import org.makkiato.arcadeclient.data.annotation.Document;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

public class ArcadeclientConfigurationExtension extends RepositoryConfigurationExtensionSupport {
    @Override
    public String getModuleName() {
        return "ArcadeDB";
    }

    @Override
    public String getModulePrefix() {
        return "arcadedb";
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return ArcadeclientRepositoryFactoryBean.class.getName();
    }

    @Override
    public Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.singleton(Document.class);
    }

    @Override
    public Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(ArcadedbCrudRepository.class);
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();

        builder.addPropertyReference("arcadedbOperations", attributes.getString("arcadedbTemplateRef"));
    }

    @Override
    public boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return metadata.isReactiveRepository();
    }
}
