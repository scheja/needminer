package de.janscheurenbrand.needminer.preprocessors;

import java.util.List;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public interface TokenPreprocessor {
    List<String> process(List<String> tokens);
}
