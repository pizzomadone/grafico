package com.flowchart.serialization;

import com.flowchart.layout.FlowchartManager;
import com.flowchart.model.*;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Handles serialization and deserialization of flowcharts to/from JSON.
 * Uses Gson with custom type adapters for polymorphic FlowBlock handling.
 */
public class FlowchartSerializer {

    private static final Gson gson = createGson();

    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(FlowBlock.class, new FlowBlockAdapter())
                .create();
    }

    /**
     * Save flowchart to a file.
     */
    public static void saveToFile(FlowchartManager manager, File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            FlowchartData data = new FlowchartData();
            data.root = manager.getRoot();
            data.version = "1.0";

            gson.toJson(data, writer);
        }
    }

    /**
     * Load flowchart from a file.
     */
    public static FlowchartManager loadFromFile(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            FlowchartData data = gson.fromJson(reader, FlowchartData.class);

            FlowchartManager manager = new FlowchartManager(data.root);
            manager.recalculateLayout();

            return manager;
        }
    }

    /**
     * Export flowchart to JSON string.
     */
    public static String toJson(FlowchartManager manager) {
        FlowchartData data = new FlowchartData();
        data.root = manager.getRoot();
        data.version = "1.0";

        return gson.toJson(data);
    }

    /**
     * Import flowchart from JSON string.
     */
    public static FlowchartManager fromJson(String json) {
        FlowchartData data = gson.fromJson(json, FlowchartData.class);

        FlowchartManager manager = new FlowchartManager(data.root);
        manager.recalculateLayout();

        return manager;
    }

    /**
     * Data container for serialization.
     */
    private static class FlowchartData {
        String version;
        FlowBlock root;
    }

    /**
     * Custom Gson adapter for polymorphic FlowBlock serialization.
     */
    private static class FlowBlockAdapter implements JsonSerializer<FlowBlock>, JsonDeserializer<FlowBlock> {

        private static final String TYPE_FIELD = "blockType";

        @Override
        public JsonElement serialize(FlowBlock src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();

            // Add type information
            result.addProperty(TYPE_FIELD, src.getClass().getSimpleName());

            // Add common properties
            result.addProperty("id", src.getId());
            result.addProperty("text", src.getText());

            // Add type-specific properties
            if (src instanceof ProcessBlock) {
                ProcessBlock block = (ProcessBlock) src;
                if (block.getNextBlock() != null) {
                    result.add("nextBlock", context.serialize(block.getNextBlock()));
                }
            } else if (src instanceof IOBlock) {
                IOBlock block = (IOBlock) src;
                if (block.getNextBlock() != null) {
                    result.add("nextBlock", context.serialize(block.getNextBlock()));
                }
            } else if (src instanceof ConditionalBlock) {
                ConditionalBlock block = (ConditionalBlock) src;
                if (block.getTrueBranch() != null) {
                    result.add("trueBranch", context.serialize(block.getTrueBranch()));
                }
                if (block.getFalseBranch() != null) {
                    result.add("falseBranch", context.serialize(block.getFalseBranch()));
                }
                if (block.getNextBlock() != null) {
                    result.add("nextBlock", context.serialize(block.getNextBlock()));
                }
            } else if (src instanceof LoopBlock) {
                LoopBlock block = (LoopBlock) src;
                if (block.getLoopBody() != null) {
                    result.add("loopBody", context.serialize(block.getLoopBody()));
                }
                if (block.getNextBlock() != null) {
                    result.add("nextBlock", context.serialize(block.getNextBlock()));
                }
            } else if (src instanceof StartEndBlock) {
                StartEndBlock block = (StartEndBlock) src;
                result.addProperty("isStart", block.isStart());
                if (block.getNextBlock() != null) {
                    result.add("nextBlock", context.serialize(block.getNextBlock()));
                }
            }

            return result;
        }

        @Override
        public FlowBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get(TYPE_FIELD).getAsString();
            String id = jsonObject.get("id").getAsString();
            String text = jsonObject.get("text").getAsString();

            FlowBlock block = null;

            switch (type) {
                case "ProcessBlock":
                    block = new ProcessBlock(text);
                    block.setId(id);
                    if (jsonObject.has("nextBlock")) {
                        FlowBlock nextBlock = context.deserialize(jsonObject.get("nextBlock"), FlowBlock.class);
                        ((ProcessBlock) block).setNextBlock(nextBlock);
                    }
                    break;

                case "IOBlock":
                    block = new IOBlock(text);
                    block.setId(id);
                    if (jsonObject.has("nextBlock")) {
                        FlowBlock nextBlock = context.deserialize(jsonObject.get("nextBlock"), FlowBlock.class);
                        ((IOBlock) block).setNextBlock(nextBlock);
                    }
                    break;

                case "ConditionalBlock":
                    block = new ConditionalBlock(text);
                    block.setId(id);
                    if (jsonObject.has("trueBranch")) {
                        FlowBlock trueBranch = context.deserialize(jsonObject.get("trueBranch"), FlowBlock.class);
                        ((ConditionalBlock) block).setTrueBranch(trueBranch);
                    }
                    if (jsonObject.has("falseBranch")) {
                        FlowBlock falseBranch = context.deserialize(jsonObject.get("falseBranch"), FlowBlock.class);
                        ((ConditionalBlock) block).setFalseBranch(falseBranch);
                    }
                    if (jsonObject.has("nextBlock")) {
                        FlowBlock nextBlock = context.deserialize(jsonObject.get("nextBlock"), FlowBlock.class);
                        ((ConditionalBlock) block).setNextBlock(nextBlock);
                    }
                    break;

                case "LoopBlock":
                    block = new LoopBlock(text);
                    block.setId(id);
                    if (jsonObject.has("loopBody")) {
                        FlowBlock loopBody = context.deserialize(jsonObject.get("loopBody"), FlowBlock.class);
                        ((LoopBlock) block).setLoopBody(loopBody);
                    }
                    if (jsonObject.has("nextBlock")) {
                        FlowBlock nextBlock = context.deserialize(jsonObject.get("nextBlock"), FlowBlock.class);
                        ((LoopBlock) block).setNextBlock(nextBlock);
                    }
                    break;

                case "StartEndBlock":
                    boolean isStart = jsonObject.get("isStart").getAsBoolean();
                    block = new StartEndBlock(text, isStart);
                    block.setId(id);
                    if (jsonObject.has("nextBlock")) {
                        FlowBlock nextBlock = context.deserialize(jsonObject.get("nextBlock"), FlowBlock.class);
                        ((StartEndBlock) block).setNextBlock(nextBlock);
                    }
                    break;

                default:
                    throw new JsonParseException("Unknown block type: " + type);
            }

            return block;
        }
    }
}
