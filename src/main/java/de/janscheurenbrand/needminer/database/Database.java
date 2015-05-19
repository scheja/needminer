package de.janscheurenbrand.needminer.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Database class for creating and closing mongoDB connections
 */
public class Database {
    private static final Logger logger = LogManager.getLogger("Database");
    private final String mongoServerAddress = "localhost:27017";
    private final String mongoDBName = "needminer";

    private MongoClient client;
    private MongoDatabase database;

    public Database() {
        logger.info("Establishing database connection");
        Codec<Document> defaultDocumentCodec = MongoClient.getDefaultCodecRegistry().get(Document.class);
        TweetCodec tweetCodec = new TweetCodec(defaultDocumentCodec);
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(tweetCodec)
        );
        MongoClientOptions options = MongoClientOptions.builder()
                .codecRegistry(codecRegistry)
                .build();

        this.client = new MongoClient(mongoServerAddress, options);
        this.database = client.getDatabase(mongoDBName);
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public TweetDAO getTweetDAO() {
        return new TweetDAO(this);
    }

    public void close() {
        client.close();
    }

}
