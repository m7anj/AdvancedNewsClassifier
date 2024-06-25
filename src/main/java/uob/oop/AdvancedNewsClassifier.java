package uob.oop;

import org.apache.commons.lang3.time.StopWatch;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdvancedNewsClassifier {
    public Toolkit myTK = null;
    public static List<NewsArticles> listNews = null;
    public static List<Glove> listGlove = null;
    public List<ArticlesEmbedding> listEmbedding = null;
    public MultiLayerNetwork myNeuralNetwork = null;

    private String space = "\\s+";
    public final int BATCHSIZE = 10;

    public int embeddingSize = 0;
    private static StopWatch mySW = new StopWatch();

    public AdvancedNewsClassifier() throws IOException {
        myTK = new Toolkit();
        myTK.loadGlove();
        listNews = myTK.loadNews();
        listGlove = createGloveList();
        listEmbedding = loadData();
    }

    public static void main(String[] args) throws Exception {
        mySW.start();
        AdvancedNewsClassifier myANC = new AdvancedNewsClassifier();

        myANC.embeddingSize = myANC.calculateEmbeddingSize(myANC.listEmbedding);
        myANC.populateEmbedding();
        myANC.myNeuralNetwork = myANC.buildNeuralNetwork(2);
        myANC.predictResult(myANC.listEmbedding);
        myANC.printResults();
        mySW.stop();
        System.out.println("Total elapsed time: " + mySW.getTime());
    }

    public List<Glove> createGloveList() {
        List<Glove> listResult = new ArrayList<>();
        //TODO Task 6.1 - 5 Marks

        for (int i = 0; i < Toolkit.listVocabulary.size(); i++) {
            String word = Toolkit.listVocabulary.get(i).toString().toLowerCase();
            boolean flag;

            flag = false;
            for (String stopWord : Toolkit.STOPWORDS) {
                if (word.equals(stopWord.toLowerCase())) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                Vector vector = new Vector(Toolkit.listVectors.get(i));
                Glove glove = new Glove(word, vector);
                listResult.add(glove);
            }
        }
        return listResult;
    }


    public static List<ArticlesEmbedding> loadData() {
        List<ArticlesEmbedding> listEmbedding = new ArrayList<>();
        for (NewsArticles news : listNews) {
            ArticlesEmbedding myAE = new ArticlesEmbedding(news.getNewsTitle(), news.getNewsContent(), news.getNewsType(), news.getNewsLabel());
            listEmbedding.add(myAE);
        }
        return listEmbedding;
    }

    public int calculateEmbeddingSize(List<ArticlesEmbedding> _listEmbedding) {
        int intMedian = -1;
        //TODO Task 6.2 - 5 Marks

        List<Integer> listOfLengths = new ArrayList<>();

        for (NewsArticles article : _listEmbedding) {
            String content = article.getNewsContent().toString();
            String[] words = content.split(space);
            int wordCount = 0;

            for (String word : words) {
                for (Glove glove : listGlove) {
                    if (word.equals(glove.getVocabulary())) {
                        wordCount++;
                    }
                }
            }

            listOfLengths.add(wordCount);
            wordCount = 0;

        }

        bubbleSort(listOfLengths);

        if (listOfLengths.size() > 0) {
            if (listOfLengths.size() % 2 == 0) {
                intMedian = ((listOfLengths.get((listOfLengths.size() / 2)) + listOfLengths.get((listOfLengths.size() / 2) + 1))) / 2;
            } else if (listOfLengths.size() % 2 == 1) {
                intMedian = listOfLengths.get((listOfLengths.size()+1) / 2);
            } else {
                intMedian = -1;
            }
        } else {
            intMedian = -1;
        }
        return intMedian;
    }


    public static void bubbleSort(List<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            int k = list.get(i);
            int j = i - 1;

            while (j >= 0 && list.get(j) > k) {
                list.set(j + 1, list.get(j));
                --j;
            }
            list.set(j + 1, k);
        }
    }


    public void populateEmbedding() {
        //TODO Task 6.3 - 10 Marks

        for (ArticlesEmbedding articleEmbedding : listEmbedding) {
            try {
                articleEmbedding.getEmbedding();
            } catch (InvalidSizeException e) {
                articleEmbedding.setEmbeddingSize(embeddingSize);
            } catch (InvalidTextException e) {
                articleEmbedding.getNewsContent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                if (articleEmbedding.getEmbedding().isEmpty()) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    public DataSetIterator populateRecordReaders(int _numberOfClasses) throws Exception {
        ListDataSetIterator myDataIterator = null;
        List<DataSet> listDS = new ArrayList<>();
        INDArray inputNDArray = null;
        INDArray outputNDArray = null;

        //TODO Task 6.4 - 8 Marks

        for (ArticlesEmbedding articleEmbedding : listEmbedding) {
            if ("training".equalsIgnoreCase(articleEmbedding.getNewsType().toString())) {
                try {

                    inputNDArray = articleEmbedding.getEmbedding();

                    outputNDArray = Nd4j.zeros(1, _numberOfClasses);
                    outputNDArray.putScalar(new int[]{0, Integer.parseInt(articleEmbedding.getNewsLabel()) - 1}, 1);

                    if (!(inputNDArray.isEmpty() && outputNDArray.isEmpty())) {
                        listDS.add(new DataSet(inputNDArray, outputNDArray));
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        return new ListDataSetIterator(listDS, BATCHSIZE);
    }


    public MultiLayerNetwork buildNeuralNetwork(int _numOfClasses) throws Exception {
        DataSetIterator trainIter = populateRecordReaders(_numOfClasses);
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(42)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .updater(Adam.builder().learningRate(0.02).beta1(0.9).beta2(0.999).build())
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder().nIn(embeddingSize).nOut(15)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.HINGE)
                        .activation(Activation.SOFTMAX)
                        .nIn(15).nOut(_numOfClasses).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        for (int n = 0; n < 100; n++) {
            model.fit(trainIter);
            trainIter.reset();
        }
        return model;
    }


    public List<Integer> predictResult(List<ArticlesEmbedding> _listEmbedding) throws Exception {
        List<Integer> listResult = new ArrayList<>();
        //TODO Task 6.5 - 8 Marks

        for (ArticlesEmbedding articleEmbedding : _listEmbedding) {
            if ((articleEmbedding.getNewsType().toString()).length() == 7) {
                int[] prediction = myNeuralNetwork.predict(articleEmbedding.getEmbedding());

                if (prediction[0] == 0) {
                    listResult.add(0);
                    articleEmbedding.setNewsLabel(String.valueOf(0));
                } else if (prediction[0] == 1) {
                    listResult.add(1);
                    articleEmbedding.setNewsLabel(String.valueOf(1));
                }
            }
        }


        return listResult;
    }




    public void printResults() {

        //TODO Task 6.6 - 6.5 Marks

        int highest = -1;

        NewsArticles.DataType testing = NewsArticles.DataType.Testing;
        NewsArticles.DataType training = NewsArticles.DataType.Training;

        for (ArticlesEmbedding articleEmbedding : listEmbedding) {
            if (articleEmbedding.getNewsType().toString().equalsIgnoreCase(NewsArticles.DataType.Testing.toString())) {
                highest = Math.max(highest, Integer.parseInt(articleEmbedding.getNewsLabel()));
            }
        }

        for (int i = 0; i <= highest; i++) {
            int groupNumber = i + 1;
            System.out.println("Group " + (groupNumber));

            for (ArticlesEmbedding articleEmbedding : listEmbedding) {
                if (articleEmbedding.getNewsType().toString().equalsIgnoreCase(testing.toString())) {
                    if (Integer.parseInt(articleEmbedding.getNewsLabel()) == i) {
                        String newsTitle = articleEmbedding.getNewsTitle().trim();
                        System.out.println(newsTitle);
                    }
                }
            }
        }
    }





}
