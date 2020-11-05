package fr.insee.sugoi.model.Technique;

public class Store {

    private WriterStore writer;

    private ReaderStore reader;

    public Store(ReaderStore readerStore, WriterStore writerStore) {
        this.writer = writerStore;
        this.reader = readerStore;
    }

    public WriterStore getWriter() {
        return this.writer;
    }

    public void setWriter(WriterStore writer) {
        this.writer = writer;
    }

    public ReaderStore getReader() {
        return this.reader;
    }

    public void setReader(ReaderStore reader) {
        this.reader = reader;
    }

}
