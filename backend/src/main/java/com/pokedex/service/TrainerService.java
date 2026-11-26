package com.pokedex.service;

import com.pokedex.exception.ResourceNotFoundException;
import com.pokedex.model.Pokemon;
import com.pokedex.model.Trainer;
import com.pokedex.repository.TrainerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final PokemonService pokemonService;

    public TrainerService(TrainerRepository trainerRepository, PokemonService pokemonService) {
        this.trainerRepository = trainerRepository;
        this.pokemonService = pokemonService;
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll(Sort.by("id"));
    }

    public Trainer findById(Integer id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treinador não encontrado: " + id));
    }

    /** Treinadores que possuem um determinado Pokémon. */
    public List<Trainer> findByPokemon(Integer pokemonId) {
        return trainerRepository.findByPokemonIdsContaining(pokemonId, Sort.by("id"));
    }

    /** Resolve as referências do treinador, na ordem da equipe (inclusive repetidos). */
    public List<Pokemon> findTeam(Integer trainerId) {
        Trainer trainer = findById(trainerId);
        Map<Integer, Pokemon> byId = pokemonService.findByIds(trainer.getPokemonIds()).stream()
                .collect(Collectors.toMap(Pokemon::getId, Function.identity()));
        return trainer.getPokemonIds().stream().map(byId::get).filter(Objects::nonNull).toList();
    }
}
