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

/**
 * Database class
 *
 * This class is responsible for connecting to the database and performing
 * operations on it.
 */
public class Database {
    private static Database instance;
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static CodecRegistry codecRegistry;

    /**
     * Get the instance of the database.
     */
    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Private constructor. Starts the MongoDB connection.
     */
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

            Document indexKeys = new Document("id", 1);
            IndexOptions indexOptions = new IndexOptions().unique(true);
            getCollection().createIndex(indexKeys, indexOptions);
        } catch (Exception e) {
            System.out.println("Error connecting to database");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Insert a list into the database.
     * @param list The list to insert.
     * @return True if the list was inserted, false if it already exists.
     */
    public boolean insertList(ShoppingList list) {
        MongoCollection<ShoppingList> collection = getCollection();

        try {
            collection.insertOne(list);
        } catch (com.mongodb.MongoWriteException e) {
            // duplicate list
            System.out.println("List already exists" + e.getMessage());
            if (e.getCode() == 11000) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a list from the database.
     * @param id The id of the list to get.
     * @return The list if it exists, null otherwise.
     */
    public ShoppingList getList(String id) {
        MongoCollection<ShoppingList> collection = getCollection();

        Bson filter = Filters.eq("id", id);
        FindIterable<ShoppingList> documents = collection.find(filter);

        return documents.first();
    }

    /**
     * Update a list in the database.
     * @param list The list to update.
     */
    public void updateList(ShoppingList list) {
        MongoCollection<ShoppingList> collection = getCollection();
        Bson filter = Filters.eq("id", list.getId());
        collection.replaceOne(filter, list);
    }

    /**
     * Remove a list from the database.
     * @param id The id of the list to remove.
     */
    public void removeList(String id) {
        MongoCollection<ShoppingList> collection = getCollection();

        Bson filter = Filters.eq("id", id);
        collection.deleteOne(filter);
    }

    /**
     * Get the collection of lists initiated with the correct codec.
     * @return The collection of lists.
     */
    private MongoCollection<ShoppingList> getCollection() {
        MongoCollection<ShoppingList> collection = database.getCollection("lists", ShoppingList.class).withCodecRegistry(codecRegistry);
        return collection;
    }
}
