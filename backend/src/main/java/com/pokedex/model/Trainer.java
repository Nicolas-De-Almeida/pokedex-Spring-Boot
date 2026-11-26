package com.pokedex.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento da coleção "trainers".
 * A relação com os Pokémon (tabela Rel_Trainer_Pokemon) é guardada como uma lista de referências por ID.
 * IDs repetidos são mantidos: um treinador pode ter mais de um Pokémon da mesma espécie (ex.: Koga).
 */
@Document(collection = "trainers")
public class Trainer {

    @Id
    private Integer id;
    private String name;
    private Integer badges;
    private List<Integer> pokemonIds = new ArrayList<>();

    public Trainer() {
    }

    public Trainer(Integer id, String name, Integer badges, List<Integer> pokemonIds) {
        this.id = id;
        this.name = name;
        this.badges = badges;
        this.pokemonIds = pokemonIds;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getBadges() { return badges; }
    public void setBadges(Integer badges) { this.badges = badges; }

    public List<Integer> getPokemonIds() { return pokemonIds; }
    public void setPokemonIds(List<Integer> pokemonIds) { this.pokemonIds = pokemonIds; }
}
