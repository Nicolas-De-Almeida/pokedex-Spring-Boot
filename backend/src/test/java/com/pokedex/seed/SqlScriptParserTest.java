package com.pokedex.seed;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlScriptParserTest {

    @Test
    void leTodasAsTabelasDoScript() throws Exception {
        String sql = new ClassPathResource("Script_Pokedex.sql").getContentAsString(StandardCharsets.UTF_8);
        Map<String, List<Map<String, Object>>> tables = SqlScriptParser.parse(sql);

        assertEquals(151, tables.get("Pokemon").size());
        assertEquals(18, tables.get("Types").size());
        assertEquals(12, tables.get("Trainers").size());

        Map<String, Object> pikachu = tables.get("Pokemon").get(24);
        assertEquals(25, pikachu.get("ID_Pokemon"));
        assertEquals("Pikachu", pikachu.get("Pokemon"));
        assertEquals(90, pikachu.get("Speed"));
    }

    @Test
    void trataAspasEscapadasEPontuacao() {
        String sql = "INSERT INTO Pokemon (`ID`, `Name`) VALUES (83, 'Farfetch''d'), (122, 'Mr. Mime, o mímico');";
        List<Map<String, Object>> rows = SqlScriptParser.parse(sql).get("Pokemon");

        assertEquals("Farfetch'd", rows.get(0).get("Name"));
        assertEquals("Mr. Mime, o mímico", rows.get(1).get("Name"));
        assertEquals(122, rows.get(1).get("ID"));
    }
}
