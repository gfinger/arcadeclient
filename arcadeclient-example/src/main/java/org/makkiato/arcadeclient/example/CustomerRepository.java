package org.makkiato.arcadeclient.example;

import org.makkiato.arcadeclient.data.repository.ArcadeclientCrudRepository;
import org.makkiato.arcadeclient.data.repository.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends ArcadeclientCrudRepository<Customer> {
    @Query("contactsOfCustomer(name:$customerName)")
    Flux<Person> findContactsOfCustomer(@Param("customerName") String name);

    @Query("customerWithContacts(name:$customerName)")
    Flux<Customer.CustomerWithContacts> findCustomerWithContacts(@Param("customerName") String name);
}
