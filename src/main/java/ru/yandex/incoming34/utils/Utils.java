package ru.yandex.incoming34.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Utils {

    private static JsonNode foundNode;

    public static Optional<String> findNode(JsonNode root, List<String> nodes) {
        foundNode = null;
        return doFindNode(root, nodes);

    }

    private static Optional<String> doFindNode(JsonNode root, List<String> nodes) {
        if (!nodes.isEmpty()) {
            final String nodeName = nodes.remove(0);
            foundNode = root.get(nodeName);
            if(Objects.nonNull(foundNode)) doFindNode(foundNode, nodes);
        }
        return Objects.nonNull(foundNode) ? Optional.of(foundNode.toString()) : Optional.empty();
    }
}
