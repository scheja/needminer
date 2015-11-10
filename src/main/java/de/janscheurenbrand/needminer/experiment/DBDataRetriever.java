package de.janscheurenbrand.needminer.experiment;

import de.janscheurenbrand.needminer.Config;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.filter.*;
import de.janscheurenbrand.needminer.preprocessors.*;
import de.janscheurenbrand.needminer.preprocessors.tokenizer.SimpleTokenizer;
import de.janscheurenbrand.needminer.preprocessors.tokenizer.Span;
import de.janscheurenbrand.needminer.twitter.Tweet;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.Generics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class DBDataRetriever {
    private static final Logger logger = LogManager.getLogger("Experiment");
    private final String BASE_PATH = Config.BASE_PATH + "instances/";
    Database database;
    TweetDAO tweetDAO;
    List<Filter> filters;
    MaxentTagger tagger;
    Instances instances;
    DataPoints dataPoints;
    List<TextPreprocessor> textPreprocessors;
    List<TokenPreprocessor> tokenPreprocessors;


    public DBDataRetriever() {
        this.tagger = new MaxentTagger("models/german-fast.tagger");

        this.database = new Database();
        this.tweetDAO = this.database.getTweetDAO();

        this.filters = new ArrayList<>();
        this.filters.add(new LanguageFilter("de"));
        this.filters.add(new DuplicateFilter());
        this.filters.add(new TaggedFilter());

        this.dataPoints = DataPoints.TEXT;

        this.textPreprocessors = new ArrayList<>();
        //this.textPreprocessors.add(new RemoveURLsPreprocessor());
        //this.textPreprocessors.add(new RemoveUsernamesPreprocessor());

        this.tokenPreprocessors = new ArrayList<>();
        //this.tokenPreprocessors.add(new DowncasePreprocessor());
        this.tokenPreprocessors.add(new LengthPreprocessor());
        //this.tokenPreprocessors.add(new GermanStopWordRemovalPreprocessor());
    }

    public Instances build() throws Exception {
        logger.debug("Load tweets from DB");
        List<Tweet> tweets = tweetDAO.getTweetsWithFilters(this.filters);
        //tweetDAO.setCollection("tweets");
        //tweets.addAll(tweetDAO.getTweetsWithFilters(this.filters));
        logger.debug(String.format("Loaded %s tweets from DB", tweets.size()));

        setStructure();

        List<Tweet> needTweets = tweets.stream().filter(new TagRatioFilter(0.5, false)::accept).collect(Collectors.toList());
        List<Tweet> nothingTweets = tweets.stream().filter(new TagRatioFilter(0.1, true)::accept).collect(Collectors.toList());

        Collections.shuffle(nothingTweets, new Random(3));
        //appendToDataSet(0, nothingTweets);
        appendToDataSet(0, nothingTweets.subList(0, needTweets.size()));
        appendToDataSet(1, needTweets);

        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setInputFormat(this.instances);
        Instances dataFiltered = weka.filters.Filter.useFilter(instances, filter);
        //System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

        saveData(dataFiltered);

        return dataFiltered;
    }

    public void setStructure() throws IOException {
        FastVector atts = new FastVector();
        FastVector classes = new FastVector();

        classes.addElement("nothing");
        classes.addElement("need");

        atts.addElement(new Attribute("text", (FastVector) null));
        atts.addElement(new Attribute("@@class@@", classes));

        Instances m_structure = new Instances("tweets", atts, 0);
        m_structure.setClassIndex(m_structure.numAttributes() - 1);
        this.instances = m_structure;
    }

    public void appendToDataSet(int classId, List<Tweet> tweets) throws IOException {
        tweets.stream().forEach(tweet -> {
            double[] newInst = new double[2];

            String text = getTweetData(tweet);

            newInst[0] = (double) this.instances.attribute(0).addStringValue(text);
            newInst[1] = (double) classId;
            this.instances.add(new Instance(1.0, newInst));
        });
    }

    private String getTweetData(Tweet tweet) {
        String preprocessedTweet = preprocessTweet(tweet);
        switch (this.dataPoints) {
            case TEXT:
                return preprocessedTweet;
            case POS:
                return getPOS(preprocessedTweet);
            default:
                return null;
        }
    }

    private String preprocessTweet(Tweet tweet) {
        String result = tweet.getText();

        for (TextPreprocessor textPreprocessor: this.textPreprocessors) {
            result = textPreprocessor.process(result);
        }

        List<Span> tokenPositions = new SimpleTokenizer().tokenizePos(result);
        String tmp = result;
        List<String> tokens = tokenPositions.stream()
                .map(span -> tmp.substring(span.getStart(), span.getEnd()))
                .collect(Collectors.toList());

        for (TokenPreprocessor tokenPreprocessor: this.tokenPreprocessors) {
            tokens = tokenPreprocessor.process(tokens);
        }

        return String.join(" ", tokens);
    }

    private String getPOS(String text) {
        List<List<HasWord>> sentences = tokenizeText(new StringReader(text), PTBTokenizer.PTBTokenizerFactory.newWordTokenizerFactory("asciiQuotes"));
        List<List<TaggedWord>> result = tagger.process(sentences);

        List<String> tokens = new ArrayList<>();
        result.forEach(sentence -> {
            sentence.forEach(taggedWord -> {
                tokens.add(taggedWord.tag());
            });
        });

        return String.join(" ", tokens);
    }

    public List<List<HasWord>> tokenizeText(Reader r, TokenizerFactory<? extends HasWord> tokenizerFactory) {
        DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(r);
        if (tokenizerFactory != null) {
            documentPreprocessor.setTokenizerFactory(tokenizerFactory);
        }
        List<List<HasWord>> out = Generics.newArrayList();
        for (List<HasWord> item : documentPreprocessor) {
            out.add(item);
        }
        return out;
    }

    public void saveData(Instances instances) throws Exception{
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(new File(BASE_PATH + "data-"+ this.dataPoints + "-" + new Date().toInstant().toString() + ".arff"));
        saver.writeBatch();
    }

    private enum DataPoints {
        TEXT, POS;
    }

}
