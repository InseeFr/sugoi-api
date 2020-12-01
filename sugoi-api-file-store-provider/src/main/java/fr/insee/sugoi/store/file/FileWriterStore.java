package fr.insee.sugoi.store.file;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import fr.insee.sugoi.core.technics.WriterStore;
import fr.insee.sugoi.model.User;

public class FileWriterStore implements WriterStore {

    public FileWriterStore(Map<String, String> generateConfig) {
	}

	@Override
    public String deleteUser(String domain, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User createUser(User user) {
        // TODO Auto-generated method stub
        return null;
    }

}
