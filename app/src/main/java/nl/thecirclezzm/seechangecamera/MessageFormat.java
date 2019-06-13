package nl.thecirclezzm.seechangecamera;

public class MessageFormat {

    private String Username;
    private String Message;
    private String UniqueId;

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
