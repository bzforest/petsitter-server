package com.company.pet_sitter_server.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"")
              .append(list.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
              .append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        Matcher m = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        while (m.find()) {
            result.add(m.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
        }
        return result;
    }
}
