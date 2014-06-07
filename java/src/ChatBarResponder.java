import javax.swing.*;
import java.awt.event.*;
import java.util.regex.Pattern;
import java.util.*;
import org.postgresql.*;

//class which responds to the message bar in the GUI
class ChatBarResponder implements ActionListener, KeyListener {

    private ChatPane chatArea;
    
    public ChatBarResponder(ChatPane chatArea) {
        super();
        this.chatArea = chatArea;
    }

    public Command[] commands = {new Command("/add", 2, "a", "/add <user> - add user to your contacts"),
                               new Command("/block", 2, "x", "/block <user> - add user to your blocked list"),
                               new Command("/delete-account", 1, "", "/delete-account - delete your account"),
                               new Command("/delete-message", 2, "m", "/delete-message <mid> - delete chosen message" ),
                               new Command("/edit", 3, "mt", "/message <mid> <new text> - modify chosen message" ),
                               new Command("/help", 1, "", "/help - list all commands"),
                               new Command("/invite", 2, "c", "/invite <user> - invite user to active chat (owner only)"),
                               new Command("/leave", 1, "", "/leave - leaves the active chat"),
                               new Command("/remove", 2, "c", "/remove <user> - removes user from contacts"),
                               new Command("/unblock", 2, "b", "/unblock <user> - removes user from blocked list"),
                               new Command("/uninvite", 2, "a", "/uninvite <user> - removes user from active chat (owner only)"),
                               new Command("/whisper", 2, "x", "/whisper <user> - creates a new chat with you and chosen user")
        };

    public void actionPerformed(ActionEvent e) {
        JTextField source = (JTextField)e.getSource();
        String text = source.getText();
        source.setText("");
        
        boolean valid = false;
        int index = -1;
        
        Messenger esql = Messenger_GUI.esql;
        
        if(Pattern.matches("/\\p{Graph}* .*",text)) {
            String[] command = text.trim().split(" ");
            for(Command c : commands) {
                ++index;
                if(c.command.equals(command[0]))
                    if(c.argc != command.length) {
                        break;
                    } else {
                        valid = true;
                        break;
                    }
                else continue;
            }
            if(!valid) {
                chatArea.systemMessage("\"" + text + "\" -- invalid command");
                chatArea.systemMessage("    Type /help for list of commands");
                return;
            }
            String ret = null;
            
            switch(index) {
            case 0:
                ret = Messenger.AddToContact(esql, MessengerUser.current.name, command[1]);
                break;
            case 1:
                ret = Messenger.AddToBlock(esql, MessengerUser.current.name, command[1]);
                break;
            case 2:
                ret = Messenger.DeleteAccount(esql, MessengerUser.current.name);
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            default:;
            }
            if(ret != null)
                System.out.println(ret);
            
            return;
        }
        
        //message found, process it
        String escapedString = text;//org.postgresql.core.BaseConnection.escapeString(text);
        String ret = Messenger.NewMessage(esql, escapedString, "", MessengerUser.current.name, 0/*Chat.activeChat.cid*/, "");
        
        System.out.println("Message: " + ret); 
        
        
        
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == e.VK_TAB){
            JTextField source = (JTextField)e.getSource();
            String text = source.getText();
            
            if(Pattern.matches("/\\p{Graph}*",text)) {
                source.setText(bestMatch(text,commands));
                return;
            }
            String[] array = text.split(" ");
            int index = Arrays.asList(commands).indexOf(array[0]);
            
            if(index < 0)
                return;
            
            Command c = commands[index];
            
            array = text.split(" ", c.argc);
            System.out.println("" + array.length);
            
            if(array.length < c.argc)
            {
                //m - message, t - message text
                //c - contact, a - active user
                //x - active user or contact
                System.out.println(c.argt.charAt(array.length-2));
                switch(c.argt.charAt(array.length-2))
                {
                case 'm':
                    source.setText(array[0] + ' ' + "");
                    break;
                case 't':
                    source.setText(array[0] + ' ' + array[1] + ' ' + "");
                    break;
                case 'c':
                    source.setText(array[0] + ' ' + bestMatch(array[1],MessengerUser.current.contacts));
                    break;
                case 'a':
                    source.setText(array[0] + ' ' + bestMatch(array[1],Chat.activeChat.userNames()));
                    break;
                case 'x':
                    source.setText(array[0] + ' ' + bestMatch(array[1],MessengerUser.current.contacts,Chat.activeChat.userNames()));
                    break;
                default:;
                }
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
    
    }
    
    public void keyTyped(KeyEvent e) {
    
    }
    
    private static String bestMatch(String s, JList jlist) {
        ListModel model = jlist.getModel();
        if(model instanceof DefaultListModel) {
            DefaultListModel<String> dlm = (DefaultListModel<String>)jlist.getModel();
            String[] array = new String[dlm.getSize()];
            for(int i = 0; i < array.length; ++i)
                array[i] = dlm.getElementAt(i);
            return bestMatch(s, array);
        }
        return "";
    }
    
    private static String bestMatch(String s, Command[] commands) {
        String[] array = new String[commands.length];
        for(int i = 0; i < array.length; ++i)
            array[i] = commands[i].command;
            return bestMatch(s,array);
    }
    
    private static String bestMatch(String s, MessengerUser[] users) {
        String[] array = new String[users.length];
        for(int i = 0; i < array.length; ++i)
            array[i] = users[i].name;
            return bestMatch(s,array);
    }
    
    private static String bestMatch(String s, MessengerUser[] users, String[] list) {
        String[] array = new String[users.length + list.length];
        for(int i = 0; i < array.length; ++i)
            array[i] = users[i].name;
            //return bestMatch(s,array);
        for(int i = array.length; i < array.length + list.length; ++i)
            array[i] = list[i];
        return bestMatch(s,array);
    }
    
    private static String bestMatch(String s, String[] strings) {
        
        Boolean[] matches = new Boolean[strings.length];
        
        for(int i = 0; i < matches.length; ++i)
            matches[i] = true;
            
        int len = s.length();
        int which = -1;
        
        for(String string : strings) {
            ++which;
            if(len > string.length()) {
                matches[which] = false;
                continue;
            }
            
            for(int i = 0; i < len; ++i) {
                if(s.charAt(i) != string.charAt(i)) {
                    matches[which] = false;
                    break;
                }
            }
        }
        
        int count = 0;
        for(Boolean match : matches)
            if(match)
                ++count;
        
                
        String[] matching = new String[count];
        int i = 0;
        
        which = -1;
        for(Boolean match : matches) {
            which++;
            if(match)
                matching[i++] = strings[which];
        }
        
        System.out.println("Matches: " + count);
        
        if(count == 0)
            return s;
        if(count == 1)
            return matching[0] + ' ';
        return longestPrefix(matching);
    }
    
    private static String longestPrefix(String[] strings) {
        int longest = 0;
        
        System.out.println("HERRO!");
        for(String s : strings)
            System.out.println(s);
        
        if(strings.length == 0)
            return "";
            
        char curr;
        
        while(true) {
            curr = strings[0].charAt(longest);
            for(String s: strings) {
                if(longest == s.length() || s.charAt(longest) != curr){
                    System.out.println("DONE AT: " + longest);
                    return s.substring(0,longest);
                    }
            }
            ++longest;
        }
    }
};
