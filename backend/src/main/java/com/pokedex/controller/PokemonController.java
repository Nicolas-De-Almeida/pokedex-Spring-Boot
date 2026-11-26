package com.pokedex.controller;

import com.pokedex.model.Pokemon;
import com.pokedex.model.Trainer;
import com.pokedex.service.PokemonService;
import com.pokedex.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

    private final PokemonService pokemonService;
    private final TrainerService trainerService;

    public PokemonController(PokemonService pokemonService, TrainerService trainerService) {
        this.pokemonService = pokemonService;
        this.trainerService = trainerService;
    }

    /** GET /api/pokemons?name=pika&type=Electric (filtros opcionais) */
    @GetMapping
    public List<Pokemon> findAll(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String type) {
        return pokemonService.findAll(name, type);
    }

    @GetMapping("/{id}")
    public Pokemon findById(@PathVariable Integer id) {
        return pokemonService.findById(id);
    }

    /** GET /api/pokemons/type/{type}: consulta por tipo */
    @GetMapping("/type/{type}")
    public List<Pokemon> findByType(@PathVariable String type) {
        return pokemonService.findAll(null, type);
    }

    /** Treinadores que possuem o Pokémon */
    @GetMapping("/{id}/trainers")
    public List<Trainer> findTrainers(@PathVariable Integer id) {
        pokemonService.findById(id);
        return trainerService.findByPokemon(id);
    }

    @PostMapping
    public ResponseEntity<Pokemon> create(@Valid @RequestBody Pokemon pokemon) {
        Pokemon created = pokemonService.create(pokemon);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public Pokemon update(@PathVariable Integer id, @Valid @RequestBody Pokemon pokemon) {
        return pokemonService.update(id, pokemon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        pokemonService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
