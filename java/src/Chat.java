import javax.swing.text.*;
import java.util.*;

class Chat {
    public static Chat activeChat = null;
    public static Hashtable<Integer,Chat> chats = new Hashtable<Integer,Chat>();
    public List<Integer> messages = new ArrayList<Integer>();
    public MessengerUser[] activeUsers = new MessengerUser[0];
    public MessengerUser owner = null;
    String lastUpdate = "";
    
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
            
            Messenger_GUI.gui.new ChatHistoryManager(100,chat).execute();
        }
        
        return chats.get(cid);
    }
    
    void createSystemMessage(String s) {
        Message message = Message.systemMessage(s);
        systemMessage(message.text);
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
    
    public void updateMessage(int mid) {
        updateMessage(Message.getMessage(mid));
    }
    
    public void updateMessage(Message message) {
        System.out.println(messages.size());
        if(!messages.contains(message.mid)) { //new message
            messages.add(message.mid);
            userMessage(message.user, message.text);
        } else { //message edit
            clearDoc();
            for(int i : messages) {
                if(i < 0) { // system message
                    systemMessage(Message.getMessage(i).text);
                } else { // user message
                    Message msg = Message.getMessage(i);
                    userMessage(msg.user, msg.text);
                }
            }
        }
    }
    
    public void clearDoc() {
        
        try {
            doc.remove(0,doc.getLength());
        } catch (BadLocationException e) {}
        
    }
}
