package org.makkiato.arcadeclient.data.operations;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.makkiato.arcadeclient.data.config.ArcadeclientManagedTypes;
import org.makkiato.arcadeclient.data.mapping.ArcadeclientMappingContext;
import org.makkiato.arcadeclient.data.mapping.MappingArcadeclientConverter;
import org.makkiato.arcadeclient.data.web.request.ExchangeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsTestConfiguration {
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        var entitySet = new HashSet<Class<?>>(4);
        Stream.of(Customer.class, Person.class, Address.class, IsContactOf.class)
                .forEach(entity -> entitySet.add(entity));
        return entitySet;
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
    public ArcadedbOperations arcadedbOperations(ArcadeclientMappingContext arcadeclientMappingContext,
            ExchangeFactory exchangeFactory) {
        return new ArcadedbTemplate(null, null,
                new MappingArcadeclientConverter(arcadeclientMappingContext) {
                },
                exchangeFactory);
    }
}
