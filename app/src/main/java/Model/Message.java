package Model;

public class Message {

    private String id, senderId, recieverId, charRoomId;
    private String body;
    private Object created_at;

    public Message() {
    }

    public Message(String id, String senderId, String recieverId, String charRoomId, String body, Object created_at) {
        this.id = id;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.charRoomId = charRoomId;
        this.body = body;
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecieverId() {
        return recieverId;
    }

    public void setRecieverId(String recieverId) {
        this.recieverId = recieverId;
    }

    public String getCharRoomId() {
        return charRoomId;
    }

    public void setCharRoomId(String charRoomId) {
        this.charRoomId = charRoomId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Object getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Object created_at) {
        this.created_at = created_at;
    }
}
