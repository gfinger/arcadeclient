package org.makkiato.arcadeclient;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class CustomerController {
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/init")
    @ResponseBody
    public Mono<Customer> init() {
        var customer =
                Customer.builder().name("Bell Fleury").address(Address.builder().street("Orchid Road")
                        .city("San Francisco").zip("12345").build()).build();
        return customerRepository.save(customer);
    }

    @PostMapping("/save")
    @ResponseBody
    public Mono<Customer> save(@RequestBody Customer customer) {
        return customerRepository.save(customer);
    }
}
