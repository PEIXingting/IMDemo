package hk.edu.cuhk.ie.iems5722.group5;

class ChatRoom {
    private String name;
    private String username;
    ChatRoom(String name, String username) {
        this.name = name;
        this.username = username;
    }
    String getName() {
        return name;
    }
    String getUsername() {
        return username;
    }
}
