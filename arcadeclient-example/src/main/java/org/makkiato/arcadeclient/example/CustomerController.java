package org.makkiato.arcadeclient.example;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("customer")
public class CustomerController {
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PostMapping("/save")
    @ResponseBody
    public Mono<Customer> save(@RequestBody Customer customer) {
        return customerRepository.save(customer);
    }
}
