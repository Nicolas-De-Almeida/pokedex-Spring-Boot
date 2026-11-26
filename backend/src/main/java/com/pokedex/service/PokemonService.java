package com.pokedex.service;

import com.pokedex.exception.BusinessException;
import com.pokedex.exception.ResourceNotFoundException;
import com.pokedex.model.Pokemon;
import com.pokedex.model.Trainer;
import com.pokedex.repository.PokemonRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PokemonService {

    private static final Sort BY_ID = Sort.by("id");

    private final PokemonRepository pokemonRepository;
    private final TypeService typeService;
    private final MongoTemplate mongoTemplate;

    public PokemonService(PokemonRepository pokemonRepository, TypeService typeService, MongoTemplate mongoTemplate) {
        this.pokemonRepository = pokemonRepository;
        this.typeService = typeService;
        this.mongoTemplate = mongoTemplate;
    }

    /** Lista os Pokémon, com filtros opcionais por parte do nome e por tipo. */
    public List<Pokemon> findAll(String name, String type) {
        boolean hasName = StringUtils.hasText(name);
        boolean hasType = StringUtils.hasText(type);

        if (hasName && hasType) {
            return pokemonRepository.findByNameContainingIgnoreCaseAndTypesContaining(
                    name.trim(), typeService.canonicalName(type), BY_ID);
        }
        if (hasName) {
            return pokemonRepository.findByNameContainingIgnoreCase(name.trim(), BY_ID);
        }
        if (hasType) {
            return pokemonRepository.findByTypesContaining(typeService.canonicalName(type), BY_ID);
        }
        return pokemonRepository.findAll(BY_ID);
    }

    public Pokemon findById(Integer id) {
        return pokemonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokémon não encontrado: " + id));
    }

    public List<Pokemon> findByIds(List<Integer> ids) {
        return pokemonRepository.findByIdIn(ids, BY_ID);
    }

    public Pokemon create(Pokemon pokemon) {
        if (pokemon.getId() == null) {
            // Sem ID informado: usa o próximo número da Pokédex
            int next = pokemonRepository.findTopByOrderByIdDesc().map(p -> p.getId() + 1).orElse(1);
            pokemon.setId(next);
        } else if (pokemonRepository.existsById(pokemon.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe um Pokémon com o ID " + pokemon.getId());
        }
        prepare(pokemon);
        return pokemonRepository.save(pokemon);
    }

    public Pokemon update(Integer id, Pokemon pokemon) {
        if (!pokemonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pokémon não encontrado: " + id);
        }
        // O ID da URL prevalece: o número da Pokédex não é alterado na edição
        pokemon.setId(id);
        prepare(pokemon);
        return pokemonRepository.save(pokemon);
    }

    public void delete(Integer id) {
        Pokemon pokemon = findById(id);
        pokemonRepository.delete(pokemon);
        // Remove o Pokémon das equipes dos treinadores, mantendo a relação consistente
        mongoTemplate.updateMulti(new Query(), new Update().pull("pokemonIds", id), Trainer.class);
    }

    /** Padroniza nome e tipos e impede nomes repetidos. */
    private void prepare(Pokemon pokemon) {
        pokemon.setName(pokemon.getName().trim());
        pokemon.setTypes(typeService.normalize(pokemon.getTypes()));

        pokemonRepository.findFirstByNameIgnoreCase(pokemon.getName())
                .filter(existing -> !existing.getId().equals(pokemon.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Já existe um Pokémon chamado " + existing.getName() + " (#" + existing.getId() + ")");
                });
    }
}
