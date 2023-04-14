package org.makkiato.arcadeclient.example;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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

    @GetMapping("all")
    @ResponseBody
    public Flux<Customer> all() {
        return customerRepository.findAll();
    }

    @GetMapping("contacts")
    @ResponseBody
    public Flux<Person> contacts(String customerName) { return customerRepository.findContactsOfCustomer(customerName);}
    @GetMapping("customerWithContacts")
    @ResponseBody
    public Flux<Customer.CustomerWithContacts> customerWithContacts(String customerName) { return customerRepository.findCustomerWithContacts(customerName);}
}
