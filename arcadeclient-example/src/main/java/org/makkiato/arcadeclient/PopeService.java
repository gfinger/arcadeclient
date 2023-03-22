package org.makkiato.arcadeclient;

import org.springframework.stereotype.Service;

@Service
public class PopeService {
    private final CustomerRepository personRepository;

    public PopeService(CustomerRepository personRepository) {
        this.personRepository = personRepository;
    }
}
