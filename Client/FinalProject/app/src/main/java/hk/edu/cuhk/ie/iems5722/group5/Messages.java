package hk.edu.cuhk.ie.iems5722.group5;

public class Messages {
    private int type;
    private String message;
    private String username;
    private String time;
    private int current_page;
    private int total_page;
    Messages(int type, String username, String message, String time, int current_page, int total_page) {
        this.type = type;
        this.username = username;
        this.message = message;
        this.time = time;
        this.current_page = current_page;
        this.total_page = total_page;
    }
    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
    String getUsername() {
        return username;
    }
    String getTime() {
        return time;
    }
    int getCurrent_page() {
        return current_page;
    }
    int getTotal_page() {
        return total_page;
    }
}
