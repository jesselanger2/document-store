package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import jakarta.xml.bind.DatatypeConverter;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    private File directory;

    public DocumentPersistenceManager(File baseDir){

        if(baseDir == null) {

            this.directory = new File(System.getProperty("user.dir"));

        }else{

            this.directory = baseDir;
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {

        if(uri == null || val == null) {

            throw new IllegalArgumentException();
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Document.class, createSerializer(uri, val));
        Gson gson = builder.setPrettyPrinting().create();
        String json = gson.toJson(val);

        File file = new File(this.directory, uri.getSchemeSpecificPart() + ".json");
        Files.createDirectories(Paths.get(file.getParent()));
        Writer writer = new FileWriter(file);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    private JsonSerializer<Document> createSerializer(URI uri, Document val) {

        if(val.getDocumentTxt() == null) {

            String uriAsString = uri.toString();
            byte[] binaryData = val.getDocumentBinaryData();

            return byteSerializer(uriAsString, binaryData);

        }else{

            Map<String, Integer> wordCountMap = val.getWordMap();
            String uriAsString = uri.toString();
            String documentTxt = val.getDocumentTxt();

            return stringSerializer(wordCountMap, uriAsString, documentTxt);
        }
    }

    private JsonSerializer<Document> byteSerializer(String uriAsString, byte[] binaryData) {

        return (document, type, jsonSerializationContext) -> {

            JsonObject jsonDocument = new JsonObject();
            jsonDocument.addProperty("uri", uriAsString);
            jsonDocument.addProperty("binaryData", DatatypeConverter.printBase64Binary(binaryData));


            return jsonDocument;
        };
    }

    private JsonSerializer<Document> stringSerializer(Map<String, Integer> wordCountMap, String uriAsString, String documentTxt) {

        return (document, type, jsonSerializationContext) -> {

            JsonObject jsonDocument = new JsonObject();
            jsonDocument.addProperty("uri", uriAsString);
            jsonDocument.addProperty("txt", documentTxt);

            Gson gson = new Gson();
            String wordCountJson = gson.toJson(wordCountMap);
            jsonDocument.addProperty("wordCountMap", wordCountJson);

            return jsonDocument;
        };
    }

    @Override
    public Document deserialize(URI uri) throws IOException {

        if(uri == null) {

            throw new IllegalArgumentException();
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DocumentImpl.class, createDeserializer(uri));
        Gson gson = builder.setPrettyPrinting().create();
        File file = new File(this.directory, uri.getSchemeSpecificPart() + ".json");
        Document deserializedDocument = gson.fromJson(new FileReader(file), DocumentImpl.class);

        return deserializedDocument;
    }

    private JsonDeserializer<Document> createDeserializer(URI uri) {

        return (json, type, jsonDeserializationContext) -> {

            JsonObject jsonObject = json.getAsJsonObject();
            String uriAsString = jsonObject.get("uri").getAsString();
            URI uriFromJson;

            try {

                uriFromJson = new URI(uriAsString);

            } catch (URISyntaxException e) {

                throw new RuntimeException(e);
            }

            if(jsonObject.has("txt")) {

                String txt = jsonObject.get("txt").getAsString();
                Map<String, Integer> wordCountMap = jsonObject.get("wordCountMap").getAsJsonObject().entrySet().stream().collect(
                        java.util.stream.Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().getAsInt()
                        )
                );

                return new DocumentImpl(uriFromJson, txt, wordCountMap);

            }else{
                JsonArray jsonArray = jsonObject.get("binaryData").getAsJsonArray();
                byte[] binaryData = new byte[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    binaryData[i] = jsonArray.get(i).getAsByte();
                }

                return new DocumentImpl(uriFromJson, binaryData);
            }
        };
    }

    @Override
    public boolean delete(URI uri) throws IOException {

        File toDelete = new File(directory, uri.getSchemeSpecificPart() + ".json");
        return toDelete.delete();
    }
}
