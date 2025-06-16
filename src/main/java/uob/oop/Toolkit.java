package uob.oop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Toolkit {

    public static List<String> listVocabulary = null;
    public static List<double[]> listVectors = null;
    private static final String FILENAME_GLOVE = "glove.6B.50d_Reduced.csv";

    public static final String[] STOPWORDS = {
        "a",
        "able",
        "about",
        "across",
        "after",
        "all",
        "almost",
        "also",
        "am",
        "among",
        "an",
        "and",
        "any",
        "are",
        "as",
        "at",
        "be",
        "because",
        "been",
        "but",
        "by",
        "can",
        "cannot",
        "could",
        "dear",
        "did",
        "do",
        "does",
        "either",
        "else",
        "ever",
        "every",
        "for",
        "from",
        "get",
        "got",
        "had",
        "has",
        "have",
        "he",
        "her",
        "hers",
        "him",
        "his",
        "how",
        "however",
        "i",
        "if",
        "in",
        "into",
        "is",
        "it",
        "its",
        "just",
        "least",
        "let",
        "like",
        "likely",
        "may",
        "me",
        "might",
        "most",
        "must",
        "my",
        "neither",
        "no",
        "nor",
        "not",
        "of",
        "off",
        "often",
        "on",
        "only",
        "or",
        "other",
        "our",
        "own",
        "rather",
        "said",
        "say",
        "says",
        "she",
        "should",
        "since",
        "so",
        "some",
        "than",
        "that",
        "the",
        "their",
        "them",
        "then",
        "there",
        "these",
        "they",
        "this",
        "tis",
        "to",
        "too",
        "twas",
        "us",
        "wants",
        "was",
        "we",
        "were",
        "what",
        "when",
        "where",
        "which",
        "while",
        "who",
        "whom",
        "why",
        "will",
        "with",
        "would",
        "yet",
        "you",
        "your",
    };

    public void loadGlove() throws IOException {
        BufferedReader myReader = null;

        try {
            listVocabulary = new ArrayList<>();
            listVectors = new ArrayList<>();
            Path filePath = getFileFromResource(FILENAME_GLOVE).toPath();

            if (!Files.exists(filePath)) {
                throw new IOException("GloVe File not in file path");
            }

            myReader = Files.newBufferedReader(filePath);
            String l;

            while ((l = myReader.readLine()) != null) {
                String[] words = l.split(",");
                String word = words[0];

                listVocabulary.add(word);

                double[] vec = new double[words.length - 1];

                for (int i = 1; i < words.length; i++) {
                    vec[i - 1] = Double.parseDouble(words[i]);
                }
                listVectors.add(vec);
            }
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            throw new IOException(e.getMessage());
        } finally {
            myReader.close();
        }
    }

    private static File getFileFromResource(String fileName)
        throws URISyntaxException {
        ClassLoader classLoader = Toolkit.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    public List<NewsArticles> loadNews() {
        List<NewsArticles> listNews = new ArrayList<>();

        File fldr;

        try {
            fldr = getFileFromResource("News");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File[] files = fldr.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; j < files.length - i - 1; j++) {
                    if (
                        files[j].getName()
                            .compareToIgnoreCase(files[j + 1].getName()) >
                        0
                    ) {
                        File temp = files[j];
                        files[j] = files[j + 1];
                        files[j + 1] = temp;
                    }
                }
            }
        }

        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    if (f.getName().toString().toLowerCase().endsWith(".htm")) {
                        try {
                            String html = htmlReader(f);
                            NewsArticles newsArticles = new NewsArticles(
                                HtmlParser.getNewsTitle(html),
                                HtmlParser.getNewsContent(html),
                                HtmlParser.getDataType(html),
                                HtmlParser.getLabel(html)
                            );
                            listNews.add(newsArticles);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } else {
            System.out.println("Empty Directory!");
        }

        return listNews;
    }

    private String htmlReader(File file) throws IOException {
        try {
            Path filepath = file.toPath();
            return Files.readString(filepath);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static List<String> getListVocabulary() {
        return listVocabulary;
    }

    public static List<double[]> getlistVectors() {
        return listVectors;
    }
}
