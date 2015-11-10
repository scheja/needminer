package de.janscheurenbrand.needminer.tasks;

import de.janscheurenbrand.needminer.Config;
import de.janscheurenbrand.needminer.database.Database;
import de.janscheurenbrand.needminer.database.TweetDAO;
import de.janscheurenbrand.needminer.preprocessors.*;
import de.janscheurenbrand.needminer.preprocessors.stemmer.GermanNormalization;
import de.janscheurenbrand.needminer.preprocessors.stemmer.GermanSnowballStemmer;
import de.janscheurenbrand.needminer.preprocessors.tokenizer.Span;
import de.janscheurenbrand.needminer.preprocessors.tokenizer.SimpleTokenizer;
import de.janscheurenbrand.needminer.twitter.Tweet;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.Generics;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.RemovePercentage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 11/09/15.
 */
public class NLPTask implements TweetTask {
    HashMap<String, Integer> count = new HashMap<>();
    HashMap<String, Integer> count2 = new HashMap<>();
    MaxentTagger tagger;
    Database db;
    TweetDAO tweetDAO;
    SimpleTokenizer simpleTokenizer;
    GermanNormalization germanNormalization;
    GermanStopWordRemovalPreprocessor stopWordFilter;
    GermanSnowballStemmer stemmer;

    public static void main(String[] args) {
        NLPTask nlpTask = new NLPTask();
        //nlpTask.tokenCounts();
        try {
            nlpTask.wekaTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NLPTask() {
        tagger = new MaxentTagger("models/german-fast.tagger");
        db = new Database();
        tweetDAO = db.getTweetDAO();
        simpleTokenizer = new SimpleTokenizer();
        germanNormalization = new GermanNormalization();
        stopWordFilter = new GermanStopWordRemovalPreprocessor();
        stemmer = new GermanSnowballStemmer();
    }

    public void tokenCounts() {
        //ArrayList<Tweet> tweets = tweetDAO.getTaggedAsNeedTweets();
        ArrayList<Tweet> tweets = tweetDAO.getTaggedAsNothingTweets(0.5);
        tweets.forEach(tweet -> {
            try {
                this.call(tweet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        count.entrySet().stream().sorted((one,two) -> Integer.compare(one.getValue(),two.getValue())).forEach(System.out::println);
    }

    @Override
    public Tweet call(Tweet tweet) throws Exception {
        List<String> tokens = tokenizeTweet(tweet);

        tokens.forEach(token -> {
            count.put(token, count.getOrDefault(token, 0)+1);
        });

        List<List<HasWord>> sentences = tokenizeText(new StringReader(tweet.getText()),  PTBTokenizer.PTBTokenizerFactory.newWordTokenizerFactory("asciiQuotes"));
        List<List<TaggedWord>> result = tagger.process(sentences);
        System.out.println(result);

        return null;
    }

    private List<String> tokenizeTweet(Tweet tweet) {
        String text = tweet.getText();
        //System.out.println("Original");
        //System.out.println(text);

        List<Span> tokenPositions = this.simpleTokenizer.tokenizePos(text);
        List<String> tokens = tokenPositions.stream()
                .map(span -> text.substring(span.getStart(), span.getEnd()))
                .collect(Collectors.toList());
        //System.out.println("Tokens");
        //System.out.println(tokens);

        //System.out.println("Good Tokens");
        tokens = tokens.stream()
                .filter(token -> token.length() > 1)
                .collect(Collectors.toList());
        //System.out.println(tokens);

        //System.out.println("Downcase Tokens");
        tokens = tokens.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        //System.out.println(tokens);

        //System.out.println("Normalized Tokens");
        tokens = tokens.stream()
                .map(this.germanNormalization::normalize)
                .collect(Collectors.toList());
        //System.out.println(tokens);

        //System.out.println("Tokens without stop words");
        tokens = tokens.stream()
                .filter(stopWordFilter::accept)
                .collect(Collectors.toList());
        //System.out.println(tokens);

        //System.out.println("Stemmed Tokens");
        tokens = tokens.stream()
                .map(this::stem)
                .collect(Collectors.toList());
        //System.out.println(tokens);

        return tokens;
    }

    private List<String> posTweet(Tweet tweet) {
        List<List<HasWord>> sentences = tokenizeText(new StringReader(tweet.getText()), PTBTokenizer.PTBTokenizerFactory.newWordTokenizerFactory("asciiQuotes"));
        List<List<TaggedWord>> result = tagger.process(sentences);

        List<String> tokens = new ArrayList<>();
        result.forEach(sentence -> {
            sentence.forEach(taggedWord -> {
                tokens.add(taggedWord.tag());
            });
        });

        return tokens;
    }

    private String stem(String token) {
        stemmer.setCurrent(token);
        stemmer.stem();
        return stemmer.getCurrent();
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

    public void wekaTest() throws Exception {
        Instances dataFiltered = getData();
        saveData(dataFiltered);

        //Instances dataFiltered = loadData("2015-09-12T11:44:22.794Z");

        for (int i = 0; i < 1; i++) {
            runExperiment(dataFiltered);
        }
    }

    public double runExperiment(Instances dataFiltered) throws Exception {
        dataFiltered = dataFiltered.resample(new Random(1234));

        // split the data set into train and test
        RemovePercentage rmvp = new RemovePercentage();
        rmvp.setPercentage(66);
        rmvp.setInputFormat(dataFiltered);
        Instances trainDataSet = Filter.useFilter(dataFiltered, rmvp);
        //System.out.println(trainDataSet);

        rmvp = new RemovePercentage();
        rmvp.setInvertSelection(true);
        rmvp.setPercentage(33);
        rmvp.setInputFormat(dataFiltered);
        Instances testDataSet = Filter.useFilter(dataFiltered, rmvp);
        //System.out.println(testDataSet);

        Classifier cModel = new NaiveBayes();
        cModel.buildClassifier(trainDataSet);
        System.out.println(cModel);

        Evaluation eTest = new Evaluation(trainDataSet);
        eTest.evaluateModel(cModel, testDataSet);

        String strSummary = eTest.toSummaryString();
        System.out.println(strSummary);

        System.out.println(eTest.toClassDetailsString());
        System.out.println(eTest.toMatrixString());

        return eTest.correct();
    }

    public Instances getData() throws Exception{
        Instances dataRaw = getStructure();

        List<Tweet> tweets1 = tweetDAO.getTaggedAsNeedTweets(0.5);
        dataRaw = appendToDataSet(dataRaw, 1, tweets1);

        List<Tweet> tweets2 = tweetDAO.getTaggedAsNothingTweets(0.1);
        tweets2 = tweets2.subList(0, tweets1.size());
        dataRaw = appendToDataSet(dataRaw, 0, tweets2);

        tweetDAO.setCollection("tweets");
        List<Tweet> tweets3 = tweetDAO.getTaggedAsNeedTweets(0.5);
        dataRaw = appendToDataSet(dataRaw, 1, tweets3);

        List<Tweet> tweets4 = tweetDAO.getTaggedAsNothingTweets(0.1);
        tweets4 = tweets4.subList(0, tweets3.size());
        dataRaw = appendToDataSet(dataRaw, 0, tweets4);

        System.out.println(String.format("loaded %s need tweets and %s nothing tweets; loaded %s need tweets and %s nothing tweets", tweets1.size(), tweets2.size(), tweets3.size(), tweets4.size()));

        // apply the StringToWordVector
        // (see the source code of setOptions(String[]) method of the filter
        // if you want to know which command-line option corresponds to which
        // bean property)

        System.out.println(dataRaw);

        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setInputFormat(dataRaw);
        Instances dataFiltered = Filter.useFilter(dataRaw, filter);
        System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

        return dataFiltered;
    }

    public void saveData(Instances instances) throws Exception{
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        saver.setFile(new File(Config.BASE_PATH + "instances/data-" + new Date().toInstant().toString() + ".arff"));
        saver.writeBatch();
    }

    public Instances loadData(String name) throws Exception{
        BufferedReader reader =
        new BufferedReader(new FileReader(new File(Config.BASE_PATH + "instances/data-"+ name +".arff")));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances data = arff.getData();
        data.setClassIndex(0);
        return data;
    }

    public Instances getStructure() throws IOException {
        FastVector atts = new FastVector();
        FastVector classes = new FastVector();

        classes.addElement("nothing");
        classes.addElement("need");

        atts.addElement(new Attribute("text", (FastVector) null));
        atts.addElement(new Attribute("@@class@@", classes));

        Instances m_structure = new Instances("tweets", atts, 0);
        m_structure.setClassIndex(m_structure.numAttributes() - 1);
        return m_structure;
    }

    public Instances appendToDataSet(Instances data, int classId, List<Tweet> tweets) throws IOException {
        tweets.stream().forEach(tweet -> {
            double[] newInst = new double[2];
            //String text =  String.join(" ", tokenizeTweet(tweet));
            String text =  String.join(" ", posTweet(tweet));
            newInst[0] = (double) data.attribute(0).addStringValue(text);
            newInst[1] = (double) classId;
            data.add(new Instance(1.0, newInst));
        });
        return data;
    }

}
