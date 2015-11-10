package de.janscheurenbrand.needminer.preprocessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class DowncasePreprocessor implements TokenPreprocessor {
    @Override
    public List<String> process(List<String> tokens) {
        return tokens.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
