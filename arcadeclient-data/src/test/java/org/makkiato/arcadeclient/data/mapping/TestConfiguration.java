package org.makkiato.arcadeclient.data.mapping;

import org.makkiato.arcadeclient.data.config.ArcadeclientConfigurationSupport;
import org.makkiato.arcadeclient.data.core.ArcadedbProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan(basePackageClasses = ArcadedbProperties.class)
public class TestConfiguration extends ArcadeclientConfigurationSupport {
}
