package Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileChatLogDAO {

    private static final String CHAT_LOG_FILENAME = "chat-logs.csv";

    public FileChatLogDAO() {
        try {
            createFileIfDoesNotExist();
        } catch (IOException e) {
            throw new RuntimeException("Unable to open/create file: " + CHAT_LOG_FILENAME, e);
        }
    }

    public void saveMessage(MessageDTO messageDTO) throws IOException {
        Objects.requireNonNull(messageDTO);
        Files.write(Paths.get(CHAT_LOG_FILENAME), messageDTO.toCsvLine().getBytes(), StandardOpenOption.APPEND);
    }

    //Wyciagamy wszystkie wiadomosci wymienione pomiedzy dwoma uzytkownikami
    public List<MessageDTO> getPrivateMessages(String from, String to) {
        Objects.requireNonNull(from, to);
        return readMessagesFromFile().stream()
                .filter(messageDTO -> messageDTO.isBetweenUsers(from, to))
                .collect(Collectors.toList());
    }

    //Wyciagamy wiadomosci ktore byly wyslane do grupy, i to konkretnie do tej podanej w parametrze
    public List<MessageDTO> getGroupMessages(String toGroup) {
        Objects.requireNonNull(toGroup);
        return readMessagesFromFile().stream()
                .filter(messageDTO -> messageDTO.isSentToGroup(toGroup))
                .collect(Collectors.toList());
    }

    private List<MessageDTO> readMessagesFromFile() {
        try {
            return Files.readAllLines(Paths.get(CHAT_LOG_FILENAME)).stream()
                    .map(MessageDTO::ofCsvLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read messages from file: " + CHAT_LOG_FILENAME, e);
        }
    }

    private void createFileIfDoesNotExist() throws IOException {
        File chatLogFile = new File(CHAT_LOG_FILENAME);
        chatLogFile.createNewFile();
    }
}
