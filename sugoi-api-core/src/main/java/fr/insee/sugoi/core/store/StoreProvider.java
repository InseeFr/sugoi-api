package fr.insee.sugoi.core.store;

public interface StoreProvider {

  public Store getStoreForUserStorage(String realmName, String userStorageName);
}
