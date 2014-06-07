import javax.swing.text.*;

class Chat {
    public static Chat activeChat;
    public MessengerUser[] activeUsers;
    public MessengerUser owner;
    String lastUpdate;
    
    int cid;
    
    StyledDocument doc = new DefaultStyledDocument();
    
    public String[] userNames() {
        String[] names = new String[activeUsers.length];
        for(int i = 0; i < activeUsers.length; ++i) {
            names[i] = activeUsers[i].name;
        }
        return names;
    }
}
