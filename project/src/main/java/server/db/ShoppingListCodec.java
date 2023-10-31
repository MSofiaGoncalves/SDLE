package server.db;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import server.model.ShoppingList;

public class ShoppingListCodec implements Codec<ShoppingList> {
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
                // TODO: decode products
                //Map<String, Pair<Integer, Integer>> products = decodeProducts(reader);
                //shoppingList.setProducts(products);
            } else if (fieldName.equals("_id")) {
                // ignore _id field
                reader.readObjectId();
            }
        }

        reader.readEndDocument();
        return shoppingList;
    }

    @Override
    public void encode(BsonWriter writer, ShoppingList shoppingList, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("id", shoppingList.getId());
        writer.writeString("name", shoppingList.getName());
        // TODO: encode products
        //encodeProducts(writer, shoppingList.getProducts());
        writer.writeEndDocument();
    }

    @Override
    public Class<ShoppingList> getEncoderClass() {
        return ShoppingList.class;
    }
}
