package de.janscheurenbrand.needminer.experiment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.DMNBtext;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SPegasos;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.instance.RemovePercentage;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main class for the representation of an experiment
 */
public class Experiment {
    private static final Logger logger = LogManager.getLogger("Experiment");

    Instances data;
    int runs;
    int folds;
    MLAlgorithm algorithm;
    Sampling sampling;
    Classifier classifier;
    double percentage;
    List<Evaluation> evaluations = new ArrayList<>();

    public Experiment() {
        this.runs = 3;
        this.folds = 10;
        this.algorithm = MLAlgorithm.SVM;
        this.classifier = getClassifier();
        this.sampling = Sampling.UNDERSAMPLING;
        this.percentage = 66.6666;
    }

    private Classifier getClassifier() {
        try {
            switch (this.algorithm) {
                case SVM:
                    return new SMO();
                case NAIVEBAYES:
                    return new NaiveBayes();
                case BAYESNET:
                    return new BayesNet();
                case RANDOMTREE:
                    return new RandomTree();
                case DMNB:
                    return new DMNBtext();
                case SPEGASOS:
                    return new SPegasos();
                case RANDOMFOREST:
                    RandomForest randomForest = new RandomForest();
                    randomForest.setNumTrees(30);
                    return randomForest;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void runWithPercentageSplit() throws Exception {
        logger.debug("Start Experiment");

        for (int i = 0; i < this.runs; i++) {
            this.data.randomize(new Random());
            //this.data = this.data.resample(new Random(123));

            // split the data set into train and test
            RemovePercentage rmvp = new RemovePercentage();
            rmvp.setPercentage(this.getPercentage());
            rmvp.setInputFormat(this.data);
            Instances trainDataSet = Filter.useFilter(this.data, rmvp);
            //System.out.println(trainDataSet);

            rmvp = new RemovePercentage();
            rmvp.setInvertSelection(true);
            rmvp.setPercentage(100 - this.getPercentage());
            rmvp.setInputFormat(this.data);
            Instances testDataSet = Filter.useFilter(this.data, rmvp);
            //System.out.println(testDataSet);

            Classifier cModel = new NaiveBayes();
            cModel.setDebug(true);
            cModel.buildClassifier(trainDataSet);
            //System.out.println(cModel);

            Evaluation eTest = new Evaluation(trainDataSet);
            eTest.evaluateModel(cModel, testDataSet);

            String strSummary = eTest.toSummaryString();
            System.out.println(strSummary);

            System.out.println(eTest.toClassDetailsString());
            System.out.println(eTest.toMatrixString());
        }
    }

    public void runWithCrossValidation() throws Exception {
        // perform cross-validation
        Random rand = new Random();
        int seed = rand.nextInt();
        for (int i = 0; i < runs; i++) {
            // randomize data
            seed = i + seed;
            rand = new Random(seed);
            Instances randData = new Instances(data);
            randData.randomize(rand);

            randData = sample(randData);

            if (randData.classAttribute().isNominal())
                randData.stratify(folds);

            Evaluation eval = new Evaluation(randData);
            for (int n = 0; n < folds; n++) {
                Instances train = randData.trainCV(folds, n);
                Instances test = randData.testCV(folds, n);
                // the above code is used by the StratifiedRemoveFolds filter, the
                // code below by the Explorer/Experimenter:
                // Instances train = randData.trainCV(folds, n, rand);

                // build and evaluate classifier
                Classifier clsCopy = Classifier.makeCopy(this.classifier);
                clsCopy.buildClassifier(train);
                eval.evaluateModel(clsCopy, test);
                plotCurve(eval, test);


            }

            // output evaluation
            System.out.println();
            System.out.println("=== Setup run " + (i + 1) + " ===");
            System.out.println("Classifier: " + this.classifier.getClass().getName() + " " + Utils.joinOptions(this.classifier.getOptions()));
            System.out.println("Dataset: " + data.relationName());
            System.out.println("Folds: " + folds);
            System.out.println("Seed: " + seed);
            System.out.println();
            System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation run " + (i + 1) + "===", false));
            System.out.println(eval.toClassDetailsString());
            System.out.println(eval.toMatrixString());

            this.evaluations.add(eval);
        }
    }

    public void plotCurve(Evaluation eval, Instances test) throws Exception {
        // generate curve
        ThresholdCurve tc = new ThresholdCurve();
        int classIndex = test.classIndex();
        Instances result = tc.getCurve(eval.predictions(), classIndex);

        // plot curve
        ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
        vmc.setROCString("(Area under ROC = " +
                Utils.doubleToString(tc.getROCArea(result), 4) + ")");
        vmc.setName(result.relationName());
        PlotData2D tempd = new PlotData2D(result);
        tempd.setPlotName(result.relationName());
        tempd.addInstanceNumberAttribute();
        // specify which points are connected
        boolean[] cp = new boolean[result.numInstances()];
        for (int j = 1; j < cp.length; j++)
            cp[j] = true;
        tempd.setConnectPoints(cp);
        // add plot
        vmc.addPlot(tempd);

        // display curve
        String plotName = vmc.getName();
        final javax.swing.JFrame jf =
                new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
        jf.setSize(500,400);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(vmc, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                jf.dispose();
            }
        });
        jf.setVisible(true);
    }

    private Instances sample(Instances data) throws Exception {
        Filter filter;
        Random random = new Random();
        int r = random.nextInt();
        switch (this.sampling) {
            case OVERSAMPLING:
                filter = new Resample();
                ((Resample) filter).setBiasToUniformClass(1.0);
                System.out.println("Resample Random Seed: " + r);
                ((Resample) filter).setRandomSeed(r);
                break;
            case SMOTE:
                filter = new SMOTE();
                System.out.println("SMOTE Random Seed: " + r);
                ((SMOTE) filter).setRandomSeed(r);
                break;
            case UNDERSAMPLING:
                filter = new SpreadSubsample();
                ((SpreadSubsample) filter).setDistributionSpread(1.0);
                break;
            default:
                filter = null;
        }
        if (filter != null) {
            filter.setInputFormat(data);
            return Filter.useFilter(data, filter);
        } else {
            return data;
        }

    }

    public Instances getData() {
        return data;
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public MLAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MLAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.classifier = getClassifier();
    }

    public Sampling getSampling() {
        return sampling;
    }

    public void setSampling(Sampling sampling) {
        this.sampling = sampling;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public enum MLAlgorithm {
        SVM, NAIVEBAYES, RANDOMFOREST, RANDOMTREE, BAYESNET, DMNB, SPEGASOS;
    }

    public enum Sampling {
        UNDERSAMPLING, OVERSAMPLING, SMOTE, NONE;
    }
}
