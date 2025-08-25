package dev.openfeature.sdk.testutils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.openfeature.sdk.MutableStructure;
import dev.openfeature.sdk.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VariantsMapDeserializer extends JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Map<String, Object> variants = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String variantKey = field.getKey();
            JsonNode variantNode = field.getValue();

            // Convert the variant value to OpenFeature Value
            Object variantValue = null;
            try {
                variantValue = convertToValue(variantNode);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
            variants.put(variantKey, variantValue);
        }

        return variants;
    }

    private Object convertToValue(JsonNode node) throws InstantiationException {
        // If the node has a "value" property, use that
        if (node.isObject() && node.has("value")) {
            return convertJsonNodeToValue(node.get("value"));
        }

        // Otherwise, treat the entire node as the value
        return convertJsonNodeToValue(node);
    }

    private Object convertJsonNodeToValue(JsonNode node) throws InstantiationException {
        if (node.isNull()) {
            return null;
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(convertJsonNodeToValue(item));
            }
            return list;
        } else if (node.isObject()) {
            Map<String, Value> map = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                Object o = convertJsonNodeToValue(field.getValue());
                if (o instanceof Value) {
                    map.put(field.getKey(), (Value) o);
                } else {
                    map.put(field.getKey(), new Value(o));
                }
            }
            return new Value(new MutableStructure(map));
        }

        throw new IllegalArgumentException("Unsupported JSON node type: " + node.getNodeType());
    }
}
