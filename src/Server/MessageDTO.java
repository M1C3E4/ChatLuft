package Server;

import java.util.Objects;

public class MessageDTO {
    private final String from;
    private final String to;
    private final String message;
    private final ChatType chatType;

    public MessageDTO(String from, String to, String message, ChatType chatType) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.message = Objects.requireNonNull(message);
        this.chatType = Objects.requireNonNull(chatType);
    }

    public String toCsvLine() {
        return from + "," + to + "," + chatType + "," + message + "\n";
    }

    public static MessageDTO ofCsvLine(String line) {
        Objects.requireNonNull(line);
        String[] split = line.split(",");
        if (split.length < 4) throw new RuntimeException("Invalid CSV line: " + line);
        return new MessageDTO(split[0], split[1], readMessage(split), ChatType.valueOf(split[2]));
    }

    private static String readMessage(String[] split) {
        if (split.length > 4) {
            return readCommaSplitMessage(split);
        } else {
            return split[3];
        }
    }

    private static String readCommaSplitMessage(String[] split) {
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < split.length; i++) {
            sb.append(",").append(split[i]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "(" + from + " -> " + to + ") " + message;
    }

    public boolean isBetweenUsers(String from, String to) {
        Objects.requireNonNull(from, to);
        return this.chatType == ChatType.PRIVATE && (this.from.equals(from) && this.to.equals(to) || this.from.equals(to) && this.to.equals(from));
    }

    public boolean isSentToGroup(String groupName) {
        Objects.requireNonNull(groupName);
        return this.chatType == ChatType.GROUP && this.to.equals(groupName);
    }
}
