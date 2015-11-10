package de.janscheurenbrand.needminer.pos;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.Generics;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * Created by janscheurenbrand on 05/09/15.
 */
public class POSTagger {
    public static void main(String[] args) {
        // Initialize the tagger
        MaxentTagger tagger = new MaxentTagger("models/german-fast.tagger");

        // The sample string
        String sample = "Das ist ein Test! Vielen Dank, dass du für mich da bist!";

        // Output the result
        System.out.println(tagger.tagString(sample));

        List<List<HasWord>> sentences = tokenizeText(new StringReader(sample),  PTBTokenizer.PTBTokenizerFactory.newWordTokenizerFactory("asciiQuotes"));

        List<List<TaggedWord>> result = tagger.process(sentences);

        System.out.println(result);

    }

    public static List<List<HasWord>> tokenizeText(Reader r,
                                                   TokenizerFactory<? extends HasWord> tokenizerFactory) {
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
}
