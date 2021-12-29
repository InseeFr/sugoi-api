package fr.insee.sugoi.model.technics;

import java.io.Serializable;
import java.util.Objects;

public class StoreMapping implements Serializable {

    private String sugoiName;
    private String storeName;
    private boolean isWritable;
    private ModelType modelType;

    public StoreMapping() {

    }

    public StoreMapping(String sugoiName, String storeName, ModelType modelType, boolean isWritable) {
        this.sugoiName = sugoiName;
        this.storeName = storeName;
        this.modelType = modelType;
        this.isWritable = isWritable;
    }

    public StoreMapping(String sugoiName, String storeName, boolean isWritable, ModelType modelType) {
        this.sugoiName = sugoiName;
        this.storeName = storeName;
        this.isWritable = isWritable;
        this.modelType = modelType;
    }

    // groups:memberOf,list_group,ro
    public StoreMapping(String storeMappingDescription) {
        String[] attributeSplits = storeMappingDescription.split(":");
        String[] attributeSplits1 = attributeSplits[1].split(",");
        this.sugoiName = attributeSplits[0];
        this.storeName = attributeSplits1[0];
        this.modelType = ModelType.valueOf(attributeSplits1[1].toUpperCase());
        this.isWritable = attributeSplits1[2].equalsIgnoreCase("rw");
    }

    public String getSugoiName() {
        return sugoiName;
    }

    public void setSugoiName(String sugoiName) {
        this.sugoiName = sugoiName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public boolean isWritable() {
        return isWritable;
    }



    public void setWritable(boolean writable) {
        isWritable = writable;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    @Override
    public String toString() {
        return "StoreMapping{" +
                "sugoiName='" + sugoiName + '\'' +
                ", storeName='" + storeName + '\'' +
                ", isWritable=" + isWritable +
                ", modelType=" + modelType +
                '}';
    }

    public String toStoreString() {
        String isWritableString = isWritable ? "rw" : "ro";
        return String.format(
                "%s:%s,%s,%s",
                sugoiName,
                storeName, modelType, isWritableString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreMapping that = (StoreMapping) o;
        return isWritable == that.isWritable && Objects.equals(sugoiName, that.sugoiName) && Objects.equals(storeName, that.storeName) && modelType == that.modelType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sugoiName, storeName, isWritable, modelType);
    }
}
