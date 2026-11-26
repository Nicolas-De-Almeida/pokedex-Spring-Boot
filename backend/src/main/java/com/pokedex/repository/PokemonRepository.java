package com.pokedex.repository;

import com.pokedex.model.Pokemon;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PokemonRepository extends MongoRepository<Pokemon, Integer> {

    List<Pokemon> findByNameContainingIgnoreCase(String name, Sort sort);

    List<Pokemon> findByTypesContaining(String type, Sort sort);

    List<Pokemon> findByNameContainingIgnoreCaseAndTypesContaining(String name, String type, Sort sort);

    List<Pokemon> findByIdIn(Collection<Integer> ids, Sort sort);

    Optional<Pokemon> findFirstByNameIgnoreCase(String name);

    Optional<Pokemon> findTopByOrderByIdDesc();
}
