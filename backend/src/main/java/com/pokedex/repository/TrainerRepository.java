package com.pokedex.repository;

import com.pokedex.model.Trainer;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrainerRepository extends MongoRepository<Trainer, Integer> {

    List<Trainer> findByPokemonIdsContaining(Integer pokemonId, Sort sort);
}
