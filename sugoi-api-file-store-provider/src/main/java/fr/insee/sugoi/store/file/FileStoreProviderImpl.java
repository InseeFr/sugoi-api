package fr.insee.sugoi.store.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fr.insee.sugoi.core.technics.Store;
import fr.insee.sugoi.core.technics.StoreProvider;

@Component
public class FileStoreProviderImpl implements StoreProvider {

    @Override
    public Store getStoreForUserStorage(String realmName, String userStorageName) {
        // TODO Auto-generated method stub
        return null;
    }

}
