import javax.swing.text.*;
import java.util.Hashtable;

class Chat {
    public static Chat activeChat;
    public static Hashtable<Integer,Chat> chats = new Hashtable<Integer,Chat>();
    public MessengerUser[] activeUsers = new MessengerUser[0];
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
    
    public static Chat getChat(int cid) {
        if(!chats.containsKey(cid)) {
            Chat chat = new Chat();
            chat.cid = cid;
            chats.put(cid, chat);
        }
        
        return chats.get(cid);
    }
    
    void systemMessage(String s)
    {
        //StyledDocument doc = getStyledDocument();
        try
        {
            doc.insertString(doc.getLength(), "\n" + s, ChatPane.system);
        }
        catch(Exception e) { System.out.println(e); }
    }
    
    void userMessage(String user, String message)
    {
        //StyledDocument doc = getStyledDocument();
        try
        {
            doc.insertString(doc.getLength(), "\n" + user, ChatPane.user);
        }
        catch(Exception e) { System.out.println(e); }
        
        try
        {
            doc.insertString(doc.getLength(), ": " + message, ChatPane.message);
        }
        catch(Exception e) { System.out.println(e); }
    }
}
