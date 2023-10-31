package server.db;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import server.model.ShoppingList;
import server.utils.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * ShoppingListCodec class
 */
public class ShoppingListCodec implements Codec<ShoppingList> {
    /**
     * Decode a BSON document into a ShoppingList object.
     * @param reader         the BSON reader
     * @param decoderContext the decoder context
     * @return the ShoppingList object
     */
    @Override
    public ShoppingList decode(BsonReader reader, DecoderContext decoderContext) {
        ShoppingList shoppingList = new ShoppingList();
        reader.readStartDocument();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals("id")) {
                shoppingList.setId(reader.readString());
            } else if (fieldName.equals("name")) {
                shoppingList.setName(reader.readString());
            } else if (fieldName.equals("products")) {
                Map<String, Pair<Integer, Integer>> products = decodeProducts(reader);
                shoppingList.setProducts(products);
            } else if (fieldName.equals("_id")) { // ignore _id field
                reader.readObjectId();
            }
        }

        reader.readEndDocument();
        return shoppingList;
    }

    /**
     * Decode the products field of a BSON document into a Map of products.
     * @param reader the BSON reader
     * @return the Map of products
     */
    private Map<String, Pair<Integer, Integer>> decodeProducts(BsonReader reader) {
        Map<String, Pair<Integer, Integer>> products = new HashMap<>();
        reader.readStartDocument();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String productName = reader.readName();

            // Assuming the structure is { "product_name": { "quantity": quantityValue, "quantityBought": quantityBoughtValue } }
            reader.readStartDocument();
            int quantity = 0;
            int quantityBought = 0;

            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String field = reader.readName();
                if (field.equals("quantity")) {
                    quantity = reader.readInt32();
                } else if (field.equals("quantityBought")) {
                    quantityBought = reader.readInt32();
                }
            }
            reader.readEndDocument();
            products.put(productName, new Pair<>(quantity, quantityBought));
        }
        reader.readEndDocument();
        return products;
    }

    /**
     * Encode a ShoppingList object into a BSON document.
     * @param writer the BSON writer to encode into
     * @param shoppingList the value to encode
     * @param encoderContext the encoder context
     */
    @Override
    public void encode(BsonWriter writer, ShoppingList shoppingList, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("id", shoppingList.getId());
        writer.writeString("name", shoppingList.getName());
        writer.writeName("products");
        encodeProducts(writer, shoppingList.getProducts());
        writer.writeEndDocument();
    }

    /**
     * Encode a Map of products into a BSON document.
     * @param writer the BSON writer to encode into
     * @param products the value to encode
     */
    private void encodeProducts(BsonWriter writer, Map<String, Pair<Integer, Integer>> products) {
        writer.writeStartDocument();
        for (Map.Entry<String, Pair<Integer, Integer>> entry : products.entrySet()) {
            writer.writeName(entry.getKey());
            writer.writeStartDocument();
            writer.writeInt32("quantity", entry.getValue().first);
            writer.writeInt32("quantityBought", entry.getValue().second);
            writer.writeEndDocument();
        }
        writer.writeEndDocument();
    }

    @Override
    public Class<ShoppingList> getEncoderClass() {
        return ShoppingList.class;
    }
}
