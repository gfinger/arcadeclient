package org.makkiato.arcadeclient;

import org.springframework.stereotype.Service;

@Service
public class PopeService {
    private final PersonRepository personRepository;

    public PopeService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }
}
