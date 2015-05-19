package de.janscheurenbrand.needminer.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.janscheurenbrand.needminer.twitter.Tweet;
import org.bson.*;
import org.bson.codecs.*;

import java.text.DateFormat;
import java.util.UUID;

/**
 * MongoDB Codec for serialization and deserialization via Gson
 */
public class TweetCodec implements CollectibleCodec<Tweet> {
    private Codec<Document> documentCodec;
    private Gson gson;

    public TweetCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.gson = new GsonBuilder()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL)
                .create();
    }

    @Override
    public Tweet decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return gson.fromJson(gson.toJson(document), Tweet.class);
    }

    @Override
    public void encode(BsonWriter writer, Tweet tweet, EncoderContext encoderContext) {
        Document document = gson.fromJson(gson.toJson(tweet), Document.class);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Tweet> getEncoderClass() {
        return Tweet.class;
    }

    @Override
    public Tweet generateIdIfAbsentFromDocument(Tweet tweet) {
        if (!documentHasId(tweet)) {
            tweet.setId(UUID.randomUUID().toString());
        }
        return tweet;
    }

    @Override
    public boolean documentHasId(Tweet tweet) {
        return tweet.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(Tweet tweet) {
        if (!documentHasId(tweet)) {
            throw new IllegalStateException("The tweet does not contain an id");
        }
        return new BsonString(tweet.getId());
    }
}
