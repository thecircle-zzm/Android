package nl.thecirclezzm.seechangecamera.ui.login.model;

public class User {

    private String username;
    private String streamingKey;

    public User(String username, String streamingKey) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getStreamingKey(){
        return streamingKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", streamingKey='" + streamingKey + '\'' +
                '}';
    }
}