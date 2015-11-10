package de.janscheurenbrand.needminer.experiment;

import de.janscheurenbrand.needminer.Config;
import de.janscheurenbrand.needminer.util.ResultsExcelExport;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by janscheurenbrand on 13/09/15.
 */
public class ExperimentRunner {
    public static void main(String[] args) throws Exception {
        FileDataRetriever dataRetriever = new FileDataRetriever();
        Instances data = dataRetriever.load(Config.BASE_PATH + "instances/data-TEXT-preprocessed-complete-final.arff");

        List<HashMap<String, Object>> results = new ArrayList<>();

        ArrayList<Experiment.MLAlgorithm> algorithms = new ArrayList<>();
  //      algorithms.add(Experiment.MLAlgorithm.SVM);
  //      algorithms.add(Experiment.MLAlgorithm.NAIVEBAYES);
        algorithms.add(Experiment.MLAlgorithm.RANDOMFOREST);
 //       algorithms.add(Experiment.MLAlgorithm.RANDOMTREE);
 //       algorithms.add(Experiment.MLAlgorithm.BAYESNET);
 //       algorithms.add(Experiment.MLAlgorithm.DMNB);
 //       algorithms.add(Experiment.MLAlgorithm.SPEGASOS);

        ArrayList<Experiment.Sampling> samplings = new ArrayList<>();
 //       samplings.add(Experiment.Sampling.OVERSAMPLING);
 //       samplings.add(Experiment.Sampling.UNDERSAMPLING);
        samplings.add(Experiment.Sampling.SMOTE);
//        samplings.add(Experiment.Sampling.NONE);

        for (Experiment.MLAlgorithm algorithm : algorithms) {
            for (Experiment.Sampling sampling : samplings) {
                Experiment experiment = new Experiment();
                experiment.setAlgorithm(algorithm);
                experiment.setSampling(sampling);
                experiment.setData(data);

                System.out.println("Algorithm: " + algorithm);
                System.out.println("Sampling: " + sampling);
                List<Evaluation> evaluations = new ArrayList<>();

                for (int i = 0; i < 1; i++) {
                    experiment.runWithCrossValidation();
                    evaluations.addAll(experiment.getEvaluations());
                }

                double avgAccuracy = evaluations.stream().mapToDouble(evaluation -> evaluation.pctCorrect()).average().getAsDouble();
                System.out.println("avgAccuracy: " + avgAccuracy);

                double avgPrecisionNoneed = evaluations.stream().mapToDouble(evaluation -> evaluation.precision(0)).average().getAsDouble();
                System.out.println("avgPrecisionNoneed: " + avgPrecisionNoneed);

                double avgPrecisionNeed = evaluations.stream().mapToDouble(evaluation -> evaluation.precision(1)).average().getAsDouble();
                System.out.println("avgPrecisionNeed: " + avgPrecisionNeed);

                double avgRecallNoneed = evaluations.stream().mapToDouble(evaluation -> evaluation.recall(0)).average().getAsDouble();
                System.out.println("avgRecallNoneed: " + avgRecallNoneed);

                double avgRecallNeed = evaluations.stream().mapToDouble(evaluation -> evaluation.recall(1)).average().getAsDouble();
                System.out.println("avgRecallNeed: " + avgRecallNeed);

                double avgROCNoneed = evaluations.stream().mapToDouble(evaluation -> evaluation.areaUnderROC(0)).average().getAsDouble();
                System.out.println("avgROCNoneed: " + avgROCNoneed);

                double avgROCNeed = evaluations.stream().mapToDouble(evaluation -> evaluation.areaUnderROC(1)).average().getAsDouble();
                System.out.println("avgROCNeed: " + avgROCNeed);

                HashMap<String, Object> result = new HashMap<>();
                result.put("Algorithm", experiment.getAlgorithm().toString());
                result.put("Sampling", experiment.getSampling().toString());
                result.put("Accuracy", avgAccuracy);
                result.put("PrecisionNoneed", avgPrecisionNoneed);
                result.put("PrecisionNeed", avgPrecisionNeed);
                result.put("RecallNoneed", avgRecallNoneed);
                result.put("RecallNeed", avgRecallNeed);
                result.put("ROCNoneed", avgROCNoneed);
                result.put("ROCNeed", avgROCNeed);

                results.add(result);

            }
        }

        ResultsExcelExport resultsExcelExport = new ResultsExcelExport(results, new File(System.getProperty("user.home") + "/results/results-" + String.valueOf(System.currentTimeMillis()) + ".xls"));
        resultsExcelExport.export();
    }
}
