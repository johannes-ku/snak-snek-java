package com.battlesnake.starter;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import static spark.Spark.*;

public class Snake {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static {
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON_MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    private static final Service SERVICE = new Service();

    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    public static class Handler {

        private static final Map<String, String> EMPTY = new HashMap<>();

        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.debug("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                LOG.debug("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", ""); // TODO: Your Battlesnake Username
            response.put("color", "#cc00cc");
            response.put("head", "earmuffs");
            response.put("tail", "bolt");
            return response;
        }

        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            return EMPTY;
        }

        public Map<String, String> move(JsonNode moveRequest) throws JsonProcessingException {
            MoveRequest request = JSON_MAPPER
                    .treeToValue(moveRequest, MoveRequest.class);
            LOG.info("Request: {}", JSON_MAPPER.writeValueAsString(request));

            Move move = SERVICE.move(request);

            LOG.info("Move: {}", move);
            Map<String, String> response = new HashMap<>();
            switch (move) {
                case UP:
                    response.put("move", "up");
                    break;
                case DOWN:
                    response.put("move", "down");
                    break;
                case LEFT:
                    response.put("move", "left");
                    break;
                case RIGHT:
                    response.put("move", "right");
                    break;

            }
            return response;
        }

        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }
    }

}
