package com.pokedex.repository;

import com.pokedex.model.PokemonType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PokemonTypeRepository extends MongoRepository<PokemonType, Integer> {

    Optional<PokemonType> findByNameIgnoreCase(String name);
}
