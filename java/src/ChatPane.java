import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.Color;


class ChatPane extends JTextPane {

    public static SimpleAttributeSet system = null, user = null, message = null, m_id;

    public ChatPane() {
        super();
        
        if(system == null) {
            system = new SimpleAttributeSet();
            user = new SimpleAttributeSet();
            message = new SimpleAttributeSet();
            m_id = new SimpleAttributeSet();
            StyleConstants.setForeground(system, Color.GRAY);
            StyleConstants.setForeground(user, Color.GREEN);
            StyleConstants.setForeground(message, Color.BLACK);
            StyleConstants.setForeground(m_id, Color.BLUE);
            
            StyleConstants.setBold(user, true);
            StyleConstants.setItalic(system, true);
            //StyleConstants.setItalic(m_id,true);
            
            
            //StyleConstants.setBackground(keyWord, Color.YELLOW);
            //StyleConstants.setBold(keyWord, true);
            
            systemMessage("Welcome to DBMS-Online-Messenger");
        }
    }
    
    void systemMessage(String s)
    {
        if(Chat.activeChat != null) {
            Chat.activeChat.updateMessage(Message.systemMessage(s));
            return;
        }
            
        StyledDocument doc = getStyledDocument();
        try
        {
            doc.insertString(doc.getLength(), "\n" + s, system);
        }
        catch(Exception e) { System.out.println(e); }
    }
    
    void userMessage(String user, String message)
    {
        StyledDocument doc = getStyledDocument();
        try
        {
            doc.insertString(doc.getLength(), "\n" + user, this.user);
        }
        catch(Exception e) { System.out.println(e); }
        
        try
        {
            doc.insertString(doc.getLength(), ": " + message, this.message);
        }
        catch(Exception e) { System.out.println(e); }
    }
}
