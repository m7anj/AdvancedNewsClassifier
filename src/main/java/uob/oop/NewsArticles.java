package uob.oop;

import org.bytedeco.hdf5.H5_alloc_stats_t;

public class NewsArticles {

    public enum DataType {
        Training,
        Testing,
    }

    private String newsTitle = "", newsContent = "";
    private DataType newsType = DataType.Testing;
    private String newsLabel = "-1";

    public NewsArticles(
        String _title,
        String _content,
        DataType _type,
        String _label
    ) {
        newsTitle = _title;
        newsContent = _content;
        newsType = _type;
        newsLabel = _label;
    }

    public String getNewsLabel() {
        return newsLabel;
    }

    public DataType getNewsType() {
        //TODO Task 2.3 - 0.5 marks
        return newsType;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsLabel(String _label) {
        newsLabel = _label;
    }

    public void setNewsType(DataType _type) {
        newsType = _type;
    }
}
