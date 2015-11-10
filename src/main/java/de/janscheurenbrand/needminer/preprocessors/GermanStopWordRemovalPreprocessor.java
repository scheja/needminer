package de.janscheurenbrand.needminer.preprocessors;

import de.janscheurenbrand.needminer.preprocessors.stemmer.GermanNormalization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 11/09/15.
 */
public class GermanStopWordRemovalPreprocessor implements TokenPreprocessor {
    ArrayList<String> stopwords = new ArrayList<>();

    public GermanStopWordRemovalPreprocessor() {
        try {
            FileReader fileReader = new FileReader(getClass().getResource("/german_stop.txt").getFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() > 2 ) {
                    if (line.contains("|")) {
                        line = line.substring(0, line.indexOf("|"));
                    }
                }
                line = line.trim();
                stopwords.add(line);
            }
            bufferedReader.close();

            fileReader = new FileReader(getClass().getResource("/hashtags.txt").getFile());
            bufferedReader = new BufferedReader(fileReader);
            line = null;
            GermanNormalization normalization = new GermanNormalization();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                line = line.toLowerCase();

                stopwords.add(normalization.normalize(line));
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean accept(String s) {
        return !stopwords.contains(s);
    }

    @Override
    public List<String> process(List<String> tokens) {
        return tokens.stream()
                .filter(this::accept)
                .collect(Collectors.toList());
    }
}
