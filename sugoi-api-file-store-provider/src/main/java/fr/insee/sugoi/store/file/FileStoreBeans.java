package fr.insee.sugoi.store.file;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.UserStorage;

@Configuration
public class FileStoreBeans {

    @Bean
    @Lazy
    public FileReaderStore fileReaderStore(Realm realm, UserStorage userStorage) {
        return new FileReaderStore(generateConfig(realm, userStorage));
    }

    @Bean
    @Lazy
    public FileWriterStore fileWriterStore(Realm realm, UserStorage userStorage) {
        return new FileWriterStore(generateConfig(realm, userStorage));
    }

    public Map<String, String> generateConfig(Realm realm, UserStorage userStorage) {
        return null;
    }

}
