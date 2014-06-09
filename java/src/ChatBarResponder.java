import javax.swing.*;
import java.awt.event.*;
import java.util.regex.Pattern;
import java.util.*;
import java.text.*;
import org.postgresql.*;

import javax.swing.text.*;

//class which responds to the message bar in the GUI
class ChatBarResponder implements ActionListener, KeyListener {

    private ChatPane chatArea;
    
    public ChatBarResponder(ChatPane chatArea) {
        super();
        this.chatArea = chatArea;
    }

    public Command[] commands = {new Command("/add", 2, "a", "/add <user> - add user to your contacts"),
                               new Command("/block", 2, "x", "/block <user> - add user to your blocked list"),
                               new Command("/blocked", 1, "", "/blocked - list your blocked users"),
                               new Command("/contacts", 1, "", "/contacts - list your contacts"),
                               new Command("/delete-account", 1, "", "/delete-account - delete your account"),
                               new Command("/delete-message", 2, "m", "/delete-message <mid> - delete chosen message" ),
                               new Command("/edit", 3, "mt", "/message <mid> <new text> - modify chosen message" ),
                               new Command("/help", 1, "", "/help - list all commands"),
                               new Command("/invite", 2, "c", "/invite <user> - invite user to active chat (owner only)"),
                               new Command("/leave", 1, "", "/leave - leaves the active chat"),
                               new Command("/remove", 2, "c", "/remove <user> - removes user from your contacts"),
                               new Command("/status", 2, "?", "/status <status> - changes your user status"),
                               new Command("/unblock", 2, "b", "/unblock <user> - removes user from blocked list"),
                               new Command("/uninvite", 2, "a", "/uninvite <user> - removes user from active chat (owner only)"),
                               new Command("/whisper", 3, "x?", "/whisper <user> <message> - creates a new chat with you and chosen user")
        };

    public void actionPerformed(ActionEvent e) {
        JTextField source = (JTextField)e.getSource();
        String text = source.getText().trim();
        source.setText("");
        
        if(text.isEmpty())
            return;
        
        boolean valid = false;
        int index = -1;
        
        Messenger esql = Messenger_GUI.esql;
        
        if(Pattern.matches("/.*",text)) {
            String[] command = text.split(" ");
            for(Command c : commands) {
                ++index;
                if(c.command.equals(command[0]))
                    if(c.argc > command.length) {
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
            command = text.split(" ",commands[index].argc);
            String ret = null;
            chatArea.systemMessage(text);
            switch(index) {
            case 0: //add
                ret = Messenger.AddToContact(esql, MessengerUser.current.name, command[1]);
                break;
            case 1: //block
                ret = Messenger.AddToBlock(esql, MessengerUser.current.name, command[1]);
                break;
            case 2: //blocked
                listBlocked();
                break;
            case 3: //contacts
                listContacts();
                break;
            case 4: //delete-account
                ret = Messenger.DeleteAccount(esql, MessengerUser.current.name);
                break;
            case 5: //delete message
                ret = Messenger.DeleteMessage(esql, MessengerUser.current.name, Integer.parseInt(command[1]));
                break;
            case 6: //edit
                ret = Messenger.UpdateMessage(esql, MessengerUser.current.name, Integer.parseInt(command[1]), safeString(command[2]));
                break;
            case 7: //help
                help();
                break;
            case 8://invite
                ret = Messenger.AddToChat(esql, Chat.activeChat.cid, MessengerUser.current.name, command[1]);
                break;
            case 9://leave
                ret = Messenger.RemoveFromChat(esql, Chat.activeChat.cid, MessengerUser.current.name, MessengerUser.current.name);
                Messenger_GUI.gui.disableActiveChat();
                break;
            case 10://remove
                ret = Messenger.DelFromContacts(esql, MessengerUser.current.name, command[1]);
                if(command[1].equals(MessengerUser.current.name)) {
                    Messenger_GUI.gui.disableActiveChat();
                }
            case 11://status
                if(command[1].equals("Online") || command[1].equals("Offline")) {
                    chatArea.systemMessage("\"" + command[1] + "\" is reserved.  Please choose a different status");
                    break;
                }
                ret = Messenger.UpdateStatus(esql,MessengerUser.current.name, safeString(command[1]));
                break;
            case 12://unblock
                ret = Messenger.DelFromBlocks(esql, MessengerUser.current.name, command[1]);
                break;
            case 13://uninvite
                ret = Messenger.RemoveFromChat(esql, Chat.activeChat.cid, MessengerUser.current.name, command[1]);
                break;
            case 14://whisper
                String safeText = safeString(command[2]);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date selfdestruct = new Date(new Date().getTime() + 1000l * 60l); //
                String timestamp = format.format(selfdestruct);
                System.out.println(timestamp);
                ret = Messenger.NewMessage(esql, safeText, timestamp, MessengerUser.current.name, -1, command[1]);
                break;
            default:;
            }
            if(ret != null)
                chatArea.systemMessage(ret);
                
            
            return;
        }
        
        //message found, process it
        String safeText = safeString(text);//org.postgresql.core.BaseConnection.escapeString(text);
        if(Chat.activeChat != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date selfdestruct = new Date(new Date().getTime() + 1000l * 60l); //
            String timestamp = format.format(selfdestruct);
            System.out.println(timestamp);
            String ret = Messenger.NewMessage(esql, safeText, timestamp, MessengerUser.current.name, Chat.activeChat.cid, "");
            
            if(ret != null && Pattern.matches("Error:.*",ret)) {
                Chat.activeChat.createSystemMessage(ret);
            }
        } else {
            chatArea.systemMessage("!! There is no active chat session!");
            chatArea.systemMessage("   Select a chat on the bottom right panel,");
            chatArea.systemMessage("   or create a new chat by /whisper");
            chatArea.systemMessage("");
        }
        
    }
    
    public String safeString(String s) {
        String safeStr = s.replace("'", "''");
        safeStr = safeStr.replace("\\", "\\\\");
        return safeStr;
    }
    
    public void help() {
        chatArea.systemMessage("Commands:");
        for(Command c : commands)
            chatArea.systemMessage("    " + c.desc);
        chatArea.systemMessage("    Press [TAB] to auto-complete commands or command arguments");
    }
    
    public void listContacts() {
        chatArea.systemMessage("Contacts:");
        for(MessengerUser contact : MessengerUser.current.contacts)
            chatArea.systemMessage("----" + contact.name);
        chatArea.systemMessage("------------");
    }
    
    public void listBlocked() {
        chatArea.systemMessage("Blocked:");
        for(MessengerUser block : MessengerUser.current.blocked)
            chatArea.systemMessage("----" + block.name);
        chatArea.systemMessage("------------");
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == e.VK_TAB){
            JTextField source = (JTextField)e.getSource();
            String text = source.getText();
            
            if(Pattern.matches("/\\p{Graph}*",text)) {
                source.setText(bestMatch(text,commands));
                return;
            }
            
            if(text.length() == 0 || Pattern.matches("^[^/].*",text)) {
                //regular message, do nothing
                return;
            }
            String[] array = text.split(" ");
            int index = -1;
            
            for(Command c : commands) {
                ++index;
                if(c.command.equals(array[0]))
                    break;
            }
            
            //int index = Arrays.asList(commands).indexOf(array[0]);
            
            System.out.println("Index: " + index);
            
            if(index < 0 || index >= commands.length)
                return;
            
            Command c = commands[index];
            
            array = text.split(" ", c.argc);
            //System.out.println("" + array.length);
            
            if(array.length <= c.argc)
            {
                //m - message, t - message text
                //c - contact, a - active user
                //x - active user or contact
                //System.out.println(c.argt.charAt(array.length-2));
                
                String[] names = null;
                if(Chat.activeChat == null)
                    names = new String[0];
                else
                    names = Chat.activeChat.userNames();
                
                switch(c.argt.charAt(array.length-2))
                {
                case 'm':
                    source.setText(array[0] + ' ' + bestMatch(array[1], Message.getIds()));
                    break;
                case 't':
                    source.setText(array[0] + ' ' + array[1] + ' ' + Message.getMessage(Integer.parseInt(array[1])).text);
                    break;
                case 'c':
                    System.out.println("Options: " + MessengerUser.current.contacts.length);
                    source.setText(array[0] + ' ' + bestMatch(array[1],MessengerUser.current.contacts));
                    break;
                case 'a':
                    source.setText(array[0] + ' ' + bestMatch(array[1],names));
                    break;
                case 'x':
                    source.setText(array[0] + ' ' + bestMatch(array[1],MessengerUser.current.contacts,names));
                    break;
                case 'b':
                    source.setText(array[0] + ' ' + bestMatch(array[1],MessengerUser.current.blocked));
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
        for(int i = 0; i < users.length; ++i)
            array[i] = users[i].name;
            //return bestMatch(s,array);
        for(int i = users.length; i < array.length; ++i)
            array[i] = list[i - users.length];
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
        
        //System.out.println("Matches: " + count);
        
        if(count == 0)
            return s;
        if(count == 1)
            return matching[0] + ' ';
        return longestPrefix(matching);
    }
    
    private static String longestPrefix(String[] strings) {
        int longest = 0;
        
        //for(String s : strings)
        //    System.out.println(s);
        
        if(strings.length == 0)
            return "";
            
        char curr;
        
        while(true) {
            curr = strings[0].charAt(longest);
            for(String s: strings) {
                if(longest >= s.length() || s.charAt(longest) != curr){
                    //System.out.println("DONE AT: " + longest);
                    return s.substring(0,longest);
                    }
            }
            ++longest;
        }
    }
};
