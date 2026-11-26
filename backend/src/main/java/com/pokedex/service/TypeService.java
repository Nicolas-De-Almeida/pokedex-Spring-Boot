package com.pokedex.service;

import com.pokedex.exception.BusinessException;
import com.pokedex.model.PokemonType;
import com.pokedex.repository.PokemonTypeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TypeService {

    private final PokemonTypeRepository typeRepository;

    public TypeService(PokemonTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    public List<PokemonType> findAll() {
        return typeRepository.findAll(Sort.by("id"));
    }

    /** Converte um nome de tipo (ignorando maiúsculas/minúsculas) para o nome cadastrado na coleção "types". */
    public String canonicalName(String name) {
        return typeRepository.findByNameIgnoreCase(name == null ? "" : name.trim())
                .map(PokemonType::getName)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Tipo inválido: " + name));
    }

    /** Valida a lista de tipos de um Pokémon, padroniza os nomes e remove repetições. */
    public List<String> normalize(List<String> names) {
        Set<String> result = new LinkedHashSet<>();
        for (String name : names) {
            result.add(canonicalName(name));
        }
        return List.copyOf(result);
    }
}
