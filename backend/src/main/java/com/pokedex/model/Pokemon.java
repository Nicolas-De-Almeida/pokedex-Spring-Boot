package com.pokedex.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Documento da coleção "pokemons".
 * Os tipos ficam embutidos no próprio documento (desnormalização da tabela Rel_Pokemon_Type do script SQL).
 */
@Document(collection = "pokemons")
public class Pokemon {

    @Id
    @Positive(message = "O ID deve ser um número positivo")
    private Integer id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 30, message = "O nome deve ter no máximo 30 caracteres")
    @Indexed
    private String name;

    @NotNull(message = "O HP é obrigatório")
    @Min(value = 1, message = "O HP deve ser no mínimo 1")
    @Max(value = 255, message = "O HP deve ser no máximo 255")
    private Integer hp;

    @NotNull(message = "O ataque é obrigatório")
    @Min(value = 1, message = "O ataque deve ser no mínimo 1")
    @Max(value = 255, message = "O ataque deve ser no máximo 255")
    private Integer attack;

    @NotNull(message = "A defesa é obrigatória")
    @Min(value = 1, message = "A defesa deve ser no mínimo 1")
    @Max(value = 255, message = "A defesa deve ser no máximo 255")
    private Integer defense;

    @NotNull(message = "O ataque especial é obrigatório")
    @Min(value = 1, message = "O ataque especial deve ser no mínimo 1")
    @Max(value = 255, message = "O ataque especial deve ser no máximo 255")
    private Integer specialAttack;

    @NotNull(message = "A defesa especial é obrigatória")
    @Min(value = 1, message = "A defesa especial deve ser no mínimo 1")
    @Max(value = 255, message = "A defesa especial deve ser no máximo 255")
    private Integer specialDefense;

    @NotNull(message = "A velocidade é obrigatória")
    @Min(value = 1, message = "A velocidade deve ser no mínimo 1")
    @Max(value = 255, message = "A velocidade deve ser no máximo 255")
    private Integer speed;

    @NotNull(message = "Informe os tipos do Pokémon")
    @Size(min = 1, max = 2, message = "O Pokémon deve ter 1 ou 2 tipos")
    @Indexed
    private List<String> types = new ArrayList<>();

    public Pokemon() {
    }

    public Pokemon(Integer id, String name, Integer hp, Integer attack, Integer defense,
                   Integer specialAttack, Integer specialDefense, Integer speed, List<String> types) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.types = types;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }

    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }

    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) { this.defense = defense; }

    public Integer getSpecialAttack() { return specialAttack; }
    public void setSpecialAttack(Integer specialAttack) { this.specialAttack = specialAttack; }

    public Integer getSpecialDefense() { return specialDefense; }
    public void setSpecialDefense(Integer specialDefense) { this.specialDefense = specialDefense; }

    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }
}
