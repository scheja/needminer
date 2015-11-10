package de.janscheurenbrand.needminer.preprocessors;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class RemoveUsernamesPreprocessor implements TextPreprocessor {
    @Override
    public String process(String text) {
        if (text.startsWith("RT ")) {
            text = text.substring(3);
        }
        while (text.startsWith("@") && text.indexOf(" ") > 0) {
            int firstSpace = text.indexOf(" ");
            text = text.substring(firstSpace+1);
        }
        return text;
    }
}
