package de.janscheurenbrand.needminer.preprocessors;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class RemoveURLsPreprocessor implements TextPreprocessor {
    @Override
    public String process(String text) {
        return text.replaceAll("\\S+://\\S+", "");
    }
}
