package com.pokedex.seed;

import com.pokedex.model.Pokemon;
import com.pokedex.model.PokemonType;
import com.pokedex.model.Trainer;
import com.pokedex.repository.PokemonRepository;
import com.pokedex.repository.PokemonTypeRepository;
import com.pokedex.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Popula o MongoDB a partir do Script_Pokedex.sql, adaptando o modelo relacional para documentos:
 * <ul>
 *   <li>Pokemon + Rel_Pokemon_Type + Types → coleção "pokemons" (tipos embutidos pelo nome)</li>
 *   <li>Types → coleção "types"</li>
 *   <li>Trainers + Rel_Trainer_Pokemon → coleção "trainers" (lista de IDs dos Pokémon)</li>
 * </ul>
 * Só executa quando a coleção "pokemons" está vazia, para não sobrescrever cadastros/edições feitos pela API.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final String TYPELESS = "Typeless";

    private final PokemonRepository pokemonRepository;
    private final PokemonTypeRepository typeRepository;
    private final TrainerRepository trainerRepository;
    private final Resource script;
    private final boolean enabled;
    private final boolean reset;

    public DatabaseSeeder(PokemonRepository pokemonRepository,
                          PokemonTypeRepository typeRepository,
                          TrainerRepository trainerRepository,
                          @Value("${pokedex.seed.script}") Resource script,
                          @Value("${pokedex.seed.enabled}") boolean enabled,
                          @Value("${pokedex.seed.reset}") boolean reset) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.trainerRepository = trainerRepository;
        this.script = script;
        this.enabled = enabled;
        this.reset = reset;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            return;
        }
        if (reset) {
            log.info("SEED_RESET=true: limpando as coleções");
            pokemonRepository.deleteAll();
            typeRepository.deleteAll();
            trainerRepository.deleteAll();
        }
        if (pokemonRepository.count() > 0) {
            log.info("MongoDB já populado ({} Pokémon). Seed ignorado.", pokemonRepository.count());
            return;
        }

        Map<String, List<Map<String, Object>>> tables =
                SqlScriptParser.parse(script.getContentAsString(StandardCharsets.UTF_8));

        // types
        Map<Integer, String> typeNames = new LinkedHashMap<>();
        List<PokemonType> types = new ArrayList<>();
        for (Map<String, Object> row : tables.get("Types")) {
            Integer id = (Integer) row.get("ID_Type");
            String name = (String) row.get("Type");
            typeNames.put(id, name);
            types.add(new PokemonType(id, name));
        }

        // relação Pokémon ↔ tipo
        Map<Integer, List<String>> typesByPokemon = new LinkedHashMap<>();
        for (Map<String, Object> row : tables.get("Rel_Pokemon_Type")) {
            typesByPokemon.computeIfAbsent((Integer) row.get("ID_Pokemon"), k -> new ArrayList<>())
                    .add(typeNames.get((Integer) row.get("ID_Type")));
        }

        // pokemons
        List<Pokemon> pokemons = new ArrayList<>();
        for (Map<String, Object> row : tables.get("Pokemon")) {
            Integer id = (Integer) row.get("ID_Pokemon");
            // Pokémon sem tipo no script (ex.: Mew) recebem o tipo "Typeless" (ID 0 da tabela Types)
            List<String> pokemonTypes = typesByPokemon.getOrDefault(id, List.of(TYPELESS));
            pokemons.add(new Pokemon(id, (String) row.get("Pokemon"),
                    (Integer) row.get("HP"), (Integer) row.get("Attack"), (Integer) row.get("Defense"),
                    (Integer) row.get("Special_Attack"), (Integer) row.get("Special_Defense"),
                    (Integer) row.get("Speed"), pokemonTypes));
        }

        // relação treinador ↔ Pokémon
        Map<Integer, List<Integer>> teamByTrainer = new LinkedHashMap<>();
        for (Map<String, Object> row : tables.get("Rel_Trainer_Pokemon")) {
            teamByTrainer.computeIfAbsent((Integer) row.get("ID_Trainer"), k -> new ArrayList<>())
                    .add((Integer) row.get("ID_Pokemon"));
        }

        // trainers
        List<Trainer> trainers = new ArrayList<>();
        for (Map<String, Object> row : tables.get("Trainers")) {
            Integer id = (Integer) row.get("ID_Trainer");
            trainers.add(new Trainer(id, (String) row.get("Trainer"), (Integer) row.get("Badges"),
                    teamByTrainer.getOrDefault(id, new ArrayList<>())));
        }

        typeRepository.saveAll(types);
        pokemonRepository.saveAll(pokemons);
        trainerRepository.saveAll(trainers);
        log.info("MongoDB populado a partir do Script_Pokedex.sql: {} Pokémon, {} tipos, {} treinadores",
                pokemons.size(), types.size(), trainers.size());
    }
}
