package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerSettings {
    private static String host;
    private static int port;

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static void loadFromFile(String fileName) throws IOException {
        try {
            byte[] jsonData = Files.readAllBytes(Paths.get(fileName));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonData);
            host = rootNode.get("host").asText();
            port = rootNode.get("port").asInt();
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
        }

    }
}
