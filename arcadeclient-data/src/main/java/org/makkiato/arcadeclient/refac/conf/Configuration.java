package org.makkiato.arcadeclient.refac.conf;

import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilter;
import org.makkiato.arcadeclient.data.web.ArcadeclientErrorResponseFilterImpl;
import org.makkiato.arcadeclient.refac.web.ServerInfoSupplier;
import org.makkiato.arcadeclient.refac.web.WebClientFactory;
import org.springframework.context.annotation.Bean;

import java.beans.BeanProperty;

public class Configuration {
    @Bean
    public ArcadeclientErrorResponseFilter arcadeclientErrorResponseFilter() {
        return new ArcadeclientErrorResponseFilterImpl();
    }

    @Bean
    public WebClientFactory webClientFactory(ArcadeclientErrorResponseFilter arcadeclientErrorResponseFilter, ConnectionProperties connectionProperties) {
        return new WebClientFactory(arcadeclientErrorResponseFilter, connectionProperties);
    }

    @Bean
    public ServerInfoSupplier serverInfoSupplier(WebClientFactory webClientFactory) {
        return new ServerInfoSupplier(webClientFactory);
    }
}
