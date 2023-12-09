package server.db;

import crdts.AddWins;
import crdts.GCounter;
import crdts.PNCounter;
import crdts.utils.Triple;
import crdts.utils.Tuple;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import server.model.Product;
import server.model.ShoppingList;
import server.utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
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
        System.out.println("decode");
        ShoppingList shoppingList = new ShoppingList();
        reader.readStartDocument();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals("id")) {
                shoppingList.setId(reader.readString());
            } else if (fieldName.equals("name")) {
                shoppingList.setName(reader.readString());
            } else if (fieldName.equals("products")) {
                System.out.println("products will be decoded");
                Map<String, Product> products = decodeProducts(reader);
                shoppingList.setProducts(products);
            } else if (fieldName.equals("addWins")) {
                AddWins addWins = decodeAddWins(reader);
                shoppingList.setAddWins(addWins);
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
    private Map<String, Product> decodeProducts(BsonReader reader) {
        System.out.println("decodeProducts");
        Map<String, Product> productMap = new HashMap<>();

        //System.out.println("BSon type2:" + reader.readBsonType());
        System.out.println("decodeProducts primeiro while");
        String productName = null;
        PNCounter pnCounter = null;
        GCounter gCounter = null;

        reader.readStartArray();
        //System.out.println("BSon type3:" + reader.getCurrentBsonType());
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            System.out.println(reader.getCurrentBsonType());
            System.out.println("decodeProducts segundo while");
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String fieldName = reader.readName();

                if (fieldName.equals("name")) {
                    productName = reader.readString();
                } else if (fieldName.equals("pnCounter")) {
                    String id = null;
                    GCounter inc = null;
                    GCounter dec = null;
                    System.out.println("decodeProducts pncounter");
                    reader.readStartDocument();

                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        String counterType = reader.readName();
                        if (counterType.equals("id")) {
                            id = reader.readString();
                        } else if (counterType.equals("inc")) {
                            inc = readGCounter(reader);
                        } else if (counterType.equals("dec")) {
                            dec = readGCounter(reader);
                        } else if (counterType.equals("_id")) { // ignore _id field
                            reader.readObjectId();
                        }
                    }
                    reader.readEndDocument();

                    pnCounter = new PNCounter(id, inc, dec);
                } else if (fieldName.equals("gCounter")) {
                    gCounter = readGCounter(reader);

                } else {
                    reader.skipValue();
                }


                if (productName != null) {
                    Product product = new Product(productName, pnCounter, gCounter);
                    productMap.put(productName, product);
                }
            }
            reader.readEndDocument();

        }

        //System.out.println("BSon type6:" + reader.readBsonType());
        reader.readEndArray();

        return productMap;
    }

    private GCounter readGCounter(BsonReader reader) {

        String id = null;
        Map<String, Integer> counterMap = new HashMap<>();

        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals("id")) {
                id = reader.readString();
            }
            else if(fieldName.equals("counters")){
                reader.readStartDocument();

                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    String key = reader.readName();
                    int value = reader.readInt32();

                    counterMap.put(key,value);
                }

                reader.readEndDocument();
            }
            else {
                reader.skipValue();
            }
        }
        reader.readEndDocument();

        GCounter gCounter = new GCounter(id);
        gCounter.setCounters(counterMap);

        return gCounter;
    }

    private AddWins decodeAddWins(BsonReader reader) {
        reader.readStartDocument();
        AddWins addWins = new AddWins();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();

            if (fieldName.equals("id")) {
                addWins.setId(reader.readString());
            } else if (fieldName.equals("cc")) {
                HashSet<Tuple<String, Long>> cc = decodeCC(reader);
                addWins.setCc(cc);
            } else if (fieldName.equals("set")) {
                HashSet<Triple<String, String, Long>> set = decodeSet(reader);
                addWins.setSet(set);
            } else if (fieldName.equals("local_counter")) {
                addWins.setLocalCounter(reader.readInt64());
            }
        }

        reader.readEndDocument();
        return addWins;
    }

    private HashSet<Tuple<String, Long>> decodeCC(BsonReader reader) {
        HashSet<Tuple<String, Long>> cc = new HashSet<>();
        reader.readStartArray();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            reader.readStartDocument();
            String first = null;
            Long second = null;

            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String field = reader.readName();
                if (field.equals("first")) {
                    first = reader.readString();
                } else if (field.equals("second")) {
                    second = reader.readInt64();
                }
            }
            reader.readEndDocument();

            Tuple<String, Long> tuple = new Tuple<>(first, second);
            cc.add(tuple);
        }
        reader.readEndArray();
        return cc;
    }

    private HashSet<Triple<String, String, Long>> decodeSet(BsonReader reader) {
        HashSet<Triple<String, String, Long>> set = new HashSet<>();
        reader.readStartArray();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            reader.readStartDocument();
            String first = null;
            String second = null;
            Long third = null;

            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                String field = reader.readName();
                if (field.equals("first")) {
                    first = reader.readString();
                } else if (field.equals("second")) {
                    second = reader.readString();
                } else if (field.equals("third")) {
                    third = reader.readInt64();
                }
            }
            reader.readEndDocument();

            Triple<String, String, Long> triple = new Triple<>(first, second, third);
            set.add(triple);
        }
        reader.readEndArray();
        return set;
    }


    public String toString(BsonWriter writer){
        return writer.toString();
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

        writer.writeStartArray("products");
        encodeProducts(writer, shoppingList.getProducts());
        writer.writeEndArray();

        writer.writeName("addWins");
        encodesAddWins(writer, shoppingList.getAddWins());

        writer.writeEndDocument();

        System.out.println("writer: " + toString(writer));
        System.out.println("shoppingList: " + shoppingList);

    }

    private void encodeProducts(BsonWriter writer, Map<String, Product> products) {
        for (Map.Entry<String, Product> entry : products.entrySet()) {
            writer.writeStartDocument();

            writer.writeString("name", entry.getKey());

            writer.writeName("pnCounter");
            encodesPNCounter(writer, entry.getValue().getPnCounter());

            writer.writeName("gCounter");
            encodesGCounter(writer, entry.getValue().getGCounter());

            writer.writeEndDocument();
        }
    }

    private void encodesAddWins(BsonWriter writer, AddWins addWins) {
        writer.writeStartDocument();

        writer.writeString("id", addWins.getId());

        writer.writeStartArray("cc");
        for (Tuple<String, Long> tuple : addWins.getCc()) {
            writer.writeStartDocument();
            writer.writeString("first", tuple.getFirst());
            writer.writeInt64("second", tuple.getSecond());
            writer.writeEndDocument();
        }
        writer.writeEndArray();

        writer.writeStartArray("set");
        for (Triple<String, String, Long> triple : addWins.getSet()) {
            writer.writeStartDocument();
            writer.writeString("first", triple.getFirst());
            writer.writeString("second", triple.getSecond());
            writer.writeInt64("third", triple.getThird());
            writer.writeEndDocument();
        }
        writer.writeEndArray();

        writer.writeInt64("local_counter", addWins.getLocalCounter());

        writer.writeEndDocument();
    }

    private void encodesGCounter(BsonWriter writer, GCounter gcounter) {
        writer.writeStartDocument(); // Start of the GCounter document

        writer.writeString("id", gcounter.getId());

        writer.writeStartDocument("counters"); // Start of the counters sub-document
        for (Map.Entry<String, Integer> entry : gcounter.getCounters().entrySet()) {
            writer.writeInt32(entry.getKey(), entry.getValue());
        }
        writer.writeEndDocument(); // End of the counters sub-document

        writer.writeEndDocument(); // End of the GCounter document
    }


    private void encodesPNCounter(BsonWriter writer, PNCounter pncounter) {
        writer.writeStartDocument(); // Start of the PNCounter document

        writer.writeString("id", pncounter.getId());

        writer.writeName("inc");
        encodesGCounter(writer, pncounter.getInc());

        writer.writeName("dec");
        encodesGCounter(writer, pncounter.getDec());

        writer.writeEndDocument();
    }




    @Override
    public Class<ShoppingList> getEncoderClass() {
        return ShoppingList.class;
    }
}
