package org.makkiato.arcadeclient;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Component
    public static class Init implements InitializingBean {
        private final CustomerRepository customerRepository;

        public Init(CustomerRepository customerRepository) {
            this.customerRepository = customerRepository;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            customerRepository.count().block();

            var customer =
                    Customer.builder().name("Bell Fleury").address(Address.builder().street("Orchid Road")
                            .city("San Francisco").zip("12345").build()).build();

            customerRepository.save(customer).block();
        }
    }
}