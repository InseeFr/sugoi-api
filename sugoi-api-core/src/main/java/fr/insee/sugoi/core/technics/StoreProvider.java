package fr.insee.sugoi.core.technics;

public interface StoreProvider {

  public Store getStoreForUserStorage(String realmName, String userStorageName);
}
