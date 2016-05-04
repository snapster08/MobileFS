package mfs.network;


public class Message {
    private String senderId;
    private int type;
    private String body;

    public Message(String senderId, int type, String body) {
        this.senderId = senderId;
        this.type = type;
        this.body = body;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Type: " +this.getType() +"\nBody: " +getBody();
    }
}
