package com.pokedex.seed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê os comandos INSERT INTO ... VALUES (...) do Script_Pokedex.sql.
 * Não precisa de um banco relacional: o próprio script é interpretado e cada linha vira um Map coluna → valor.
 */
public final class SqlScriptParser {

    private static final Pattern INSERT = Pattern.compile(
            "INSERT\\s+INTO\\s+`?(\\w+)`?\\s*\\(([^)]*)\\)\\s*VALUES\\s*(.*?);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private SqlScriptParser() {
    }

    /** Retorna, para cada tabela, a lista de linhas inseridas. */
    public static Map<String, List<Map<String, Object>>> parse(String sql) {
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        Matcher matcher = INSERT.matcher(sql);

        while (matcher.find()) {
            String table = matcher.group(1);
            List<String> columns = new ArrayList<>();
            for (String column : matcher.group(2).split(",")) {
                columns.add(column.replace("`", "").trim());
            }

            List<Map<String, Object>> rows = tables.computeIfAbsent(table, t -> new ArrayList<>());
            for (List<Object> values : parseTuples(matcher.group(3))) {
                if (values.size() != columns.size()) {
                    throw new IllegalStateException("Linha com número de colunas inválido na tabela " + table);
                }
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    row.put(columns.get(i), values.get(i));
                }
                rows.add(row);
            }
        }
        return tables;
    }

    /** Lê "(1, 'Bulbasaur', 45), (2, 'Farfetch''d', 52)" respeitando strings entre aspas simples. */
    private static List<List<Object>> parseTuples(String text) {
        List<List<Object>> tuples = new ArrayList<>();
        List<Object> current = null;
        StringBuilder token = new StringBuilder();
        boolean inString = false;
        boolean quoted = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (inString) {
                if (c == '\'') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                        token.append('\''); // aspas escapadas ('')
                        i++;
                    } else {
                        inString = false;
                    }
                } else {
                    token.append(c);
                }
                continue;
            }

            switch (c) {
                case '(' -> {
                    current = new ArrayList<>();
                    token.setLength(0);
                    quoted = false;
                }
                case '\'' -> {
                    inString = true;
                    quoted = true;
                }
                case ',', ')' -> {
                    if (current != null) {
                        current.add(toValue(token.toString(), quoted));
                        token.setLength(0);
                        quoted = false;
                        if (c == ')') {
                            tuples.add(current);
                            current = null;
                        }
                    }
                }
                default -> {
                    if (current != null && !Character.isWhitespace(c)) {
                        token.append(c);
                    }
                }
            }
        }
        return tuples;
    }

    private static Object toValue(String raw, boolean quoted) {
        if (quoted) {
            return raw;
        }
        if (raw.equalsIgnoreCase("NULL")) {
            return null;
        }
        return Integer.valueOf(raw);
    }
}
