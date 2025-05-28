package com.octopus.ai.repository;

import com.octopus.ai.entity.Dog;
import org.springframework.data.repository.ListCrudRepository;

public interface DogRepository extends ListCrudRepository<Dog, Integer> {
}
