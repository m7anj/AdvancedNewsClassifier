package uob.oop;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import org.bytedeco.libfreenect._freenect_context;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.Properties;

public class ArticlesEmbedding extends NewsArticles {
    private int intSize = -1;
    private String processedText = "";

    private INDArray newsEmbedding = Nd4j.create(0);

    public ArticlesEmbedding(String _title, String _content, NewsArticles.DataType _type, String _label) {
        //TODO Task 5.1 - 1 Mark
        super(_title,_content,_type,_label);
    }

    public void setEmbeddingSize(int _size) {
        //TODO Task 5.2 - 0.5 Marks
        intSize = _size;
    }

    public int getEmbeddingSize(){
        return intSize;
    }

    private String space = ("\\s+");







    @Override
    public String getNewsContent() {
        //TODO Task 5.3 - 10 Marks

        String content = super.getNewsContent();

        String cleanedContent = textCleaning(content);
        StringBuilder lemmatizedText = new StringBuilder();
        String[] stopwords = Toolkit.STOPWORDS;

        // Source: https://stanfordnlp.github.io/CoreNLP/lemma.html - Lemmatization From Java (Example Demo)
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        CoreDocument doc = pipeline.processToCoreDocument(cleanedContent);

        for (CoreLabel tok : doc.tokens()) {
            lemmatizedText.append(tok.lemma()).append(" ");
        }

        String finalLemmatizedText = lemmatizedText.toString().trim();
        String[] words = finalLemmatizedText.split(space);
        StringBuilder finalText = new StringBuilder();

        for (String word : words) {
            boolean flag = false;
            for (String stopWord : stopwords) {
                if (word.equals(stopWord)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                finalText.append(word).append(" ");
            }
        }

        processedText = finalText.toString().toLowerCase().trim();
        return processedText.trim();
    }







    public INDArray getEmbedding() throws Exception {
        //TODO Task 5.4 - 20 Marks

        int count = 0;

        if (intSize == -1) {
            throw new InvalidSizeException("Invalid size");
        }

        if (processedText.isEmpty()) {
            throw new InvalidTextException("Invalid text");
        }

        if ((newsEmbedding.isEmpty() == false)){
            return Nd4j.vstack(newsEmbedding.mean(1));
        }

        String[] words = processedText.split(space);
        newsEmbedding = Nd4j.zeros(intSize, 50);

        for (String word : words) {
            for (Glove glove : AdvancedNewsClassifier.listGlove) {
                if ((glove.getVocabulary().equalsIgnoreCase(word)) && (count < intSize)){
                    newsEmbedding.putRow(count,Nd4j.create(glove.getVector().getAllElements()));
                    count++;
                }
            }
        }

        return Nd4j.vstack(newsEmbedding.mean(1));
    }







    /***
     * Clean the given (_content) text by removing all the characters that are not 'a'-'z', '0'-'9' and white space.
     * @param _content Text that need to be cleaned.
     * @return The cleaned text.
     */
    private static String textCleaning(String _content) {
        StringBuilder sbContent = new StringBuilder();

        for (char c : _content.toLowerCase().toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || Character.isWhitespace(c)) {
                sbContent.append(c);
            }
        }

        return sbContent.toString().trim();
    }
}
