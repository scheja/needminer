package de.janscheurenbrand.needminer.preprocessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class LengthPreprocessor implements TokenPreprocessor {
    @Override
    public List<String> process(List<String> tokens) {
        return tokens.stream()
                .filter(token -> token.length() > 1)
                .collect(Collectors.toList());
    }
}
