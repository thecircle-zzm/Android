package nl.thecirclezzm.seechangecamera.ui.chat;

public class MessageFormat {

    private final String Username;
    private final String Message;
    private final String UniqueId;

    public MessageFormat(String uniqueId, String username, String message) {
        Username = username;
        Message = message;
        UniqueId = uniqueId;
    }

    public String getUsername() {
        return Username;
    }

    public String getMessage() {
        return Message;
    }


    public String getUniqueId() {
        return UniqueId;
    }

}
