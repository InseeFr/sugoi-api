package fr.insee.sugoi.model.Technique;

public class MyPageable {
    private static final int TAILLE_RECHERCHE_DEFAUT = 20;

    private int size = TAILLE_RECHERCHE_DEFAUT;
    private int estimatedTotalSize = 0;
    private byte[] cookie;
    private String sortKey;
    private int first = 1;
    private boolean pagingDisabled = false;

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getEstimatedTotalSize() {
        return this.estimatedTotalSize;
    }

    public void setEstimatedTotalSize(int estimatedTotalSize) {
        this.estimatedTotalSize = estimatedTotalSize;
    }

    public byte[] getCookie() {
        return this.cookie;
    }

    public void setCookie(byte[] cookie) {
        this.cookie = cookie;
    }

    public String getSortKey() {
        return this.sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public int getFirst() {
        return this.first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public boolean isPagingDisabled() {
        return this.pagingDisabled;
    }

    public void setPagingDisabled(boolean pagingDisabled) {
        this.pagingDisabled = pagingDisabled;
    }

}
