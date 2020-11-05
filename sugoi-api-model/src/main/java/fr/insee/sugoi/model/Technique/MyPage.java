package fr.insee.sugoi.model.Technique;

import java.util.List;

public class MyPage<T> {
    private static final int TAILLE_RECHERCHE_DEFAUT = 20;

    private List<T> results;
    private int totalElements;
    private int nextStart;
    private boolean hasMoreResult = false;
    private int pageSize = TAILLE_RECHERCHE_DEFAUT;
    private byte[] searchCookie;

    public List<T> getResults() {
        return this.results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public int getTotalElements() {
        return this.totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getNextStart() {
        return this.nextStart;
    }

    public void setNextStart(int nextStart) {
        this.nextStart = nextStart;
    }

    public boolean isHasMoreResult() {
        return this.hasMoreResult;
    }

    public void setHasMoreResult(boolean hasMoreResult) {
        this.hasMoreResult = hasMoreResult;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public byte[] getSearchCookie() {
        return this.searchCookie;
    }

    public void setSearchCookie(byte[] searchCookie) {
        this.searchCookie = searchCookie;
    }

}
