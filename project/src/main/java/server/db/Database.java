package server.db;

import ch.qos.logback.classic.LoggerContext;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import server.model.ShoppingList;

public class Database {
    private static Database instance;
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static CodecRegistry codecRegistry;

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private Database() {
        String uri = "mongodb://localhost:27017";

        try {
            // Disable MongoDB logging
            ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.ERROR);

            codecRegistry = CodecRegistries.fromRegistries(
                    CodecRegistries.fromCodecs(new ShoppingListCodec()),
                    MongoClientSettings.getDefaultCodecRegistry()
            );
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("test");

        } catch (Exception e) {
            System.out.println("Error connecting to database");
            System.out.println(e.getMessage());
        }
    }

    public boolean insertList(ShoppingList list) {
        MongoCollection<ShoppingList> collection = getCollection();

        try {
            collection.insertOne(list);
        } catch (com.mongodb.MongoWriteException e) {
            // duplicate list
            if (e.getCode() == 11000) {
                return false;
            }
        }
        return true;
    }

    public ShoppingList getList(String id) {
        MongoCollection<ShoppingList> collection = getCollection();

        Bson filter = Filters.eq("id", id);
        FindIterable<ShoppingList> documents = collection.find(filter);

        return documents.first();
    }

    public void removeList(String id) {
        MongoCollection<ShoppingList> collection = getCollection();

        Bson filter = Filters.eq("id", id);
        collection.deleteOne(filter);
    }

    private MongoCollection<ShoppingList> getCollection() {
        MongoCollection<ShoppingList> collection = database.getCollection("lists", ShoppingList.class).withCodecRegistry(codecRegistry);
        IndexOptions indexOptions = new IndexOptions().unique(true);
        collection.createIndex(new Document("fieldName", 1), indexOptions);
        return collection;
    }
}
