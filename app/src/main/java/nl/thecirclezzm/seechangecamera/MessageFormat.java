package nl.thecirclezzm.seechangecamera;

public class MessageFormat {

    private String Username;
    private String Message;
    private String Room;
    private String UniqueId;

    public MessageFormat(String uniqueId, String username, String message, String room) {
        Username = username;
        Message = message;
        Room = room;
        UniqueId = uniqueId;
    }

    public String getUsername() {
        return Username;
    }

    public String getMessage() {
        return Message;
    }

    public String getRoom() {
        return Room;
    }

    public String getUniqueId() {
        return UniqueId;
    }

}
