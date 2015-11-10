package de.janscheurenbrand.needminer.experiment;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Created by janscheurenbrand on 16/10/15.
 */
public class FileDataRetriever {

    public Instances load(String filename) throws Exception {
        DataSource source = new DataSource(filename);
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1)
            data.setClassIndex(0);

        return data;
    }
}
