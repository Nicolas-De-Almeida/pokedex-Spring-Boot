package com.pokedex.controller;

import com.pokedex.model.Pokemon;
import com.pokedex.model.Trainer;
import com.pokedex.service.TrainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping
    public List<Trainer> findAll() {
        return trainerService.findAll();
    }

    @GetMapping("/{id}")
    public Trainer findById(@PathVariable Integer id) {
        return trainerService.findById(id);
    }

    /** Equipe do treinador com os documentos completos dos Pokémon */
    @GetMapping("/{id}/pokemons")
    public List<Pokemon> findTeam(@PathVariable Integer id) {
        return trainerService.findTeam(id);
    }
}
