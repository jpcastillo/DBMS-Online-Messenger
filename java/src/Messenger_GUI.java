import javax.swing.*;
//import java.awt.*; //don't want to import java.awt.List
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.AffineTransform;
import javax.swing.text.*;
import java.text.ParseException;
import java.util.regex.Pattern;
import java.net.URL;

public class Messenger_GUI extends WindowAdapter implements ActionListener{
    public static Messenger_GUI gui = null;
    public static Messenger esql;
    // Definition of global values and items that are part of the GUI.
    
    JPanel rightPanel, mainPanel;
    JMenuBar menuBar;
    JList usersList, chatsList;
    JLabel loginError, signUpError;
    
    JFormattedTextField phoneLine;
    JTextField loginUser, signUpUser;
    JPasswordField loginPass, signUpPass;
    
    JScrollPane scrollArea;
    
    ChatPane chatArea;
    
    enum ToggleButtons {LOGIN, SIGN_UP, HELP};
    EnumMap <ToggleButtons, JToggleButton> tbuttons = new EnumMap <ToggleButtons, JToggleButton>(ToggleButtons.class);
    
    public DefaultListModel<String> usersModel, chatsModel;
    
    JButton notifButton = null;
    ImageIcon activeIcon = null, inactiveIcon = null;
    
    //used to trigger Ghost text
    Condition emptyCondition = new Condition() {
        @Override
        public boolean condition(JTextField f){return f.getText().length() == 0;}
    };
        
    Condition phoneEmptyCondition = new Condition() {
        @Override
        public boolean condition(JTextField f){
            Object obj = ((JFormattedTextField)f).getValue();
            if( obj == null ) {
                return !Pattern.matches("\\+\\d\\(\\d\\d\\d\\)\\d\\d\\d-\\d\\d\\d\\d",f.getText());
            }
            boolean r = !Pattern.matches(".*\\d",(String)obj) && !Pattern.matches("\\+\\d\\(\\d\\d\\d\\)\\d\\d\\d-\\d\\d\\d\\d",f.getText());
            return r;}
    };

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.PAGE_AXIS));
        loginPanel.add(Box.createVerticalGlue());
        
        JPanel errorLine = new JPanel();
        errorLine.setLayout( new BoxLayout(errorLine, BoxLayout.LINE_AXIS));
        errorLine.add(Box.createHorizontalGlue());
        loginError = new JLabel();
        loginError.setForeground(Color.RED);
        //loginPanel.add(loginError);
        errorLine.add(loginError);
        errorLine.add(Box.createHorizontalGlue());
        loginPanel.add(errorLine);
        
        JTextField userLine = new JTextField(8);
        userLine.addKeyListener(new KeyEater());
        loginUser = userLine;
        //new GhostText(userLine, "Username", emptyCondition);
        userLine.setHorizontalAlignment(JTextField.CENTER);
        AffineTransform t = new AffineTransform();
        t.scale(2,2);
        userLine.setFont(userLine.getFont().deriveFont(t));
        userLine.setMaximumSize(new Dimension(Short.MAX_VALUE, userLine.getPreferredSize().height));
        loginPanel.add(new JLayer<JTextField>(userLine,new GhostText(userLine, "Username", emptyCondition)));
        errorLine.setMaximumSize(userLine.getMaximumSize());
        
        JPasswordField passwordLine = new JPasswordField(8);
        loginPass = passwordLine;
        passwordLine.enableInputMethods(true);
        //new GhostText(passwordLine, "Password", emptyCondition);
        passwordLine.setHorizontalAlignment(JTextField.CENTER);
        passwordLine.setFont(userLine.getFont());
        passwordLine.setMaximumSize(new Dimension(Short.MAX_VALUE, passwordLine.getPreferredSize().height));
        passwordLine.setActionCommand("login");
        passwordLine.addActionListener(this);
        loginPanel.add(new JLayer<JTextField>(passwordLine,new GhostText(passwordLine, "Password", emptyCondition)));
        
        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
        JButton loginButton = new JButton("Login");
        loginButton.setFont(userLine.getFont());
        loginButton.setActionCommand("login");
        loginButton.addActionListener(this);
        loginButton.setAlignmentX(1.0f);
        line.add(Box.createHorizontalGlue());
        line.add(loginButton);
        loginPanel.add(line);
        
        loginPanel.add(Box.createVerticalGlue());
        
        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Login!");
        //label.setHorizontalAlignment(JLabel.CENTER);
        line.add(Box.createHorizontalGlue());
        line.add(label);
        line.add(Box.createHorizontalGlue());
        loginPanel.add(line);
        
        return loginPanel;
    }
    
    private JPanel createSignUpPanel() {
        JPanel signUpPanel = new JPanel();
        signUpPanel.setLayout(new BoxLayout(signUpPanel, BoxLayout.PAGE_AXIS));
        signUpPanel.add(Box.createVerticalGlue());
        
        JPanel errorLine = new JPanel();
        errorLine.setLayout( new BoxLayout(errorLine, BoxLayout.LINE_AXIS));
        errorLine.add(Box.createHorizontalGlue());
        signUpError = new JLabel();
        signUpError.setForeground(Color.RED);
        //signUpPanel.add(signUpError);
        errorLine.add(signUpError);
        errorLine.add(Box.createHorizontalGlue());
        signUpPanel.add(errorLine);
        
        JTextField userLine = new JTextField(8);
        signUpUser = userLine;
        userLine.addKeyListener(new KeyEater());
        //new GhostText(userLine, "Username", emptyCondition);
        userLine.setHorizontalAlignment(JTextField.CENTER);
        AffineTransform t = new AffineTransform();
        t.scale(2,2);
        userLine.setFont(userLine.getFont().deriveFont(t));
        userLine.setMaximumSize(new Dimension(Short.MAX_VALUE, userLine.getPreferredSize().height));
        signUpPanel.add(new JLayer<JTextField>(userLine,new GhostText(userLine, "Username", emptyCondition)));
        errorLine.setMaximumSize(userLine.getMaximumSize());
        
        JPasswordField passwordLine = new JPasswordField(8);
        signUpPass = passwordLine;
        passwordLine.enableInputMethods(true);
        passwordLine.setHorizontalAlignment(JTextField.CENTER);
        passwordLine.setFont(userLine.getFont());
        passwordLine.setMaximumSize(new Dimension(Short.MAX_VALUE, passwordLine.getPreferredSize().height));
        signUpPanel.add(new JLayer<JTextField>(passwordLine,new GhostText(passwordLine, "Password", emptyCondition)));
        
        try{
        MaskFormatter phoneFormat = new MaskFormatter("+#(###)###-####");
        phoneFormat.setPlaceholderCharacter('_');
        phoneLine = new JFormattedTextField(phoneFormat);
        //new GhostText(phoneLine, "Phone Number");
        phoneLine.setHorizontalAlignment(JTextField.CENTER);
        phoneLine.setFont(userLine.getFont());
        phoneLine.setMaximumSize(new Dimension(Short.MAX_VALUE, phoneLine.getPreferredSize().height));
        signUpPanel.add(new JLayer<JTextField>(phoneLine,new GhostText(phoneLine, "Phone Number", phoneEmptyCondition)));
        }
        catch(ParseException ex){}
        
        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setFont(userLine.getFont());
        signUpButton.setActionCommand("sign up");
        signUpButton.addActionListener(this);
        signUpButton.setAlignmentX(1.0f);
        line.add(Box.createHorizontalGlue());
        line.add(signUpButton);
        signUpPanel.add(line);
        signUpPanel.add(Box.createVerticalGlue());
        
        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Sign Up!");
        //label.setHorizontalAlignment(JLabel.CENTER);
        line.add(Box.createHorizontalGlue());
        line.add(label);
        line.add(Box.createHorizontalGlue());
        signUpPanel.add(line);
        
        return signUpPanel;
    }
    
    private JPanel createHelpPanel() {
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.PAGE_AXIS));
        helpPanel.add(new JLabel("Type /help for a list of commands once you log in"));
        return helpPanel;
    }
    
    private JPanel createInitPanel() {
        JPanel initPanel = new JPanel();
        initPanel.setLayout(new BoxLayout(initPanel, BoxLayout.LINE_AXIS));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        //leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        leftPanel.setPreferredSize(new Dimension(1000,1000));
        
        Dimension minSize = new Dimension(0, 0);
        Dimension prefSize = new Dimension(1, 75);
        Dimension maxSize = new Dimension(Short.MAX_VALUE, 75);
        leftPanel.add(new Box.Filler(minSize, prefSize, maxSize));
        
        ButtonGroup group = new ButtonGroup();
        for(ToggleButtons b: ToggleButtons.values()) {
            JToggleButton button = new JToggleButton("<html>"+b.name()+"<html>");
            button.addActionListener(this);
            button.setActionCommand("InitPage:"+b.name());
            button.setPreferredSize(new Dimension(1000,75));
            button.setAlignmentX(0.5f);
            group.add(button);
            tbuttons.put(b, button);
            leftPanel.add(button);
        }
        tbuttons.get(ToggleButtons.LOGIN).setSelected(true);
        leftPanel.add(Box.createVerticalGlue());
        initPanel.add(leftPanel);
        
        initPanel.add(new JSeparator(SwingConstants.VERTICAL));
        
        rightPanel = new JPanel();
        CardLayout rcl = new CardLayout(20,20);
        rightPanel.setLayout(rcl);
        rightPanel.setPreferredSize(new Dimension(1618,1000));
        initPanel.add(rightPanel);
        
        JPanel loginPanel = createLoginPanel();
        rightPanel.add(loginPanel,"LOGIN");
        
        JPanel signUpPanel = createSignUpPanel();
        rightPanel.add(signUpPanel,"SIGN_UP");
        
        JPanel helpPanel = createHelpPanel();
        rightPanel.add(helpPanel,"HELP");
        
        
        
        return initPanel;
    }
    
    JPanel createChatPanel()
    {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.LINE_AXIS));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        
        chatArea = new ChatPane();
        chatArea.setEditable(false);
        scrollArea = new JScrollPane(chatArea);
        leftPanel.add(scrollArea);
        
        ChatBarResponder responder = new ChatBarResponder(chatArea);
        
        chatArea.systemMessage("Commands:");
        for(Command c : responder.commands)
            chatArea.systemMessage("    " + c.desc);
        
        JTextField chatBar = new JTextField();
        chatBar.setMaximumSize(new Dimension(Short.MAX_VALUE,chatArea.getPreferredSize().height));
        chatBar.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET );
        
        chatBar.addKeyListener(responder);
        chatBar.addActionListener(responder);
        leftPanel.add(new JLayer<JTextField>(chatBar,new GhostText(chatBar, "Add a message...", emptyCondition)));
        leftPanel.setPreferredSize(new Dimension(1618, 1000));
        chatPanel.add(leftPanel);
        chatPanel.add(new JSeparator(JSeparator.VERTICAL));
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        
        
        
        usersModel = new DefaultListModel<String>();
        usersList = new JList<String>(usersModel);
        usersList.setPreferredSize(new Dimension(1000,1618));
        
        chatsModel = new DefaultListModel<String>();
        chatsList = new JList<String>(chatsModel);
        chatsList.addMouseListener(new ChatListListener());
        chatsList.setPreferredSize(new Dimension(1000,1000));
        
        JPanel fill = new JPanel(new BorderLayout(0,0));
        fill.add(usersList,BorderLayout.CENTER);
        rightPanel.add(fill);
        rightPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        
        fill = new JPanel(new BorderLayout(0,0));
        fill.add(chatsList,BorderLayout.CENTER);
        rightPanel.add(fill);
        
        rightPanel.setPreferredSize(new Dimension(1000, 1000));
        chatPanel.add(rightPanel);
        
        return chatPanel;
    
    }
    
    public JPanel createHomePanel() {
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new CardLayout());
        
        JPanel chatPanel = createChatPanel();
        homePanel.add(chatPanel,"CHAT");
        
        
        
        return homePanel;
    }
    
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");
        JMenu chatMenu = new JMenu("Chat");
        JMenu mesgMenu = new JMenu("Message");
        
        //menuBar.add(userMenu);
        //menuBar.add(chatMenu);
        //menuBar.add(mesgMenu);
        
        menuBar.add(Box.createHorizontalGlue());
        
        notifButton = new JButton();
        notifButton.setActionCommand("notifications");
        notifButton.addActionListener(this);
        URL imgUrl = getClass().getResource("images/Exclamation_mark_red.png");
        ImageIcon dummy = new ImageIcon(imgUrl, "Dummy");
        Image img = dummy.getImage();
        Image newImg = img.getScaledInstance(32,32,java.awt.Image.SCALE_SMOOTH);
        activeIcon = new ImageIcon(newImg, "Active Notifications");
        
        imgUrl = getClass().getResource("images/Exclamation_mark_gray.png");
        dummy = new ImageIcon(imgUrl, "Dummy");
        img = dummy.getImage();
        newImg = img.getScaledInstance(32,32,java.awt.Image.SCALE_SMOOTH);
        inactiveIcon = new ImageIcon(newImg, "Logout");
        
        notifButton.setIcon(inactiveIcon);
        notifButton.setPreferredSize(new Dimension(36,36));
        menuBar.add(notifButton);
        
        JButton logoutButton = new JButton();
        logoutButton.setActionCommand("logout");
        logoutButton.addActionListener(this);
        imgUrl = getClass().getResource("images/logout.png");
        dummy = new ImageIcon(imgUrl, "Dummy");
        img = dummy.getImage();
        newImg = img.getScaledInstance(32,32,java.awt.Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(newImg, "Logout");
        logoutButton.setIcon(icon);
        logoutButton.setPreferredSize(new Dimension(36,36));//logoutButton.getIcon().getIconWidth()+4,logoutButton.getIcon().getIconHeight()+4));
        menuBar.add(logoutButton);
        
        return menuBar;
    }

    public JPanel createContentPane() {
    
        mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout());
        
        JPanel initPanel = createInitPanel();
        mainPanel.add(initPanel,"INIT");
        
        JPanel homePanel = createHomePanel();
        mainPanel.add(homePanel,"HOME");
        
        menuBar = createMenuBar();
        
        return mainPanel;
    }
    
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String action = e.getActionCommand();
        if(action.contains(":"))
        {
            String[] parts = action.split(":");
            if(parts[0].equals("InitPage"))
            {
                ((CardLayout)rightPanel.getLayout()).show(rightPanel, parts[1]);
            }
        }
        else if(action.equals("login"))
        {
            tryLogin(loginUser.getText(), new String(loginPass.getPassword()));
        }
        else if(action.equals("sign up"))
        {
            if(signUpUser.getText().length() == 0) {
                signUpError.setText("Username is Empty!");
                return;
            }
            String user = signUpUser.getText();
            String password = new String(signUpPass.getPassword());
            
            if(!phoneLine.isEditValid()) {
                signUpError.setText("Please enter a valid phone number!");
                return;
            }
            
            System.out.println("Attempting to create user!");
            System.out.println(signUpUser.getText());
            System.out.println(password);
            System.out.println(phoneLine.getText());
            System.out.println("Online");
            String ret = Messenger.CreateUser(esql,user, password, phoneLine.getText(), "Offline");
            
            if(ret == null)
                signUpError.setText("Sign Up Error");
            else
                signUpError.setText("");
                
            if(ret.equals("")) {
                clearSignUp();
                //try {
                //    Thread.sleep(1000);
                //} catch (Exception interrupted) {}
                System.out.println("\"" + signUpUser.getText() + "\", \"" + password + '"');
                tryLogin(user, password);
            } else
                System.out.println("Sign Up Error: " + ret + "|");
                
            System.out.println("DONE SIGN-UP!");
            //phoneLine.setText("");
            //phoneLine.setValue(null);
            //Component owner = (Component)((JFrame) SwingUtilities.getWindowAncestor(phoneLine)).getFocusOwner();
            //phoneLine.requestFocusInWindow();
            //owner.requestFocusInWindow();
        }
        else if(action.equals("logout"))
        {
            logout();
            //System.out.println("Logging out :3");
            //JPopupMenu menu = new JPopupMenu();
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            
            //JButton button = (JButton)source;
            //menu.show(button, 0, button.getBounds().height);
        }
        
        else if(action.equals("notifications")) {
            final Collection<Notification> notifications = Notification.getNotifications();
            final MessengerUser user = MessengerUser.current;
            if(notifications.size() == 0)
                return;
            
            final JPopupMenu menu = new JPopupMenu();
            
            ActionListener al = (new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] ids = e.getActionCommand().split(",");
                    int cid = Integer.parseInt(ids[0]);
                    System.out.println("Marking Read: " + user.name + ", " + ids[1]);
                    String ret = Messenger.MarkReadNotifications(esql, user.name, e.getActionCommand());
                    if(ret != null) {
                        //if(Pattern.matches("Error:.*", ret)) {
                        //    System.out.println(ret);
                        //}
                        System.out.println("Marked Read: " + ret);
                    }
                    menu.remove(menu.getComponentIndex((JButton)e.getSource()));
                    
                    setActiveChat(cid);
                }
            });
            
            
            for(Notification n : notifications) {
                JButton button = new JButton(n.user + " posted/modified [" + n.mid + "] in chat (" + n.cid + ")");
                button.addActionListener(al);
                button.setActionCommand("" + n.cid + "," + n.mid);
                menu.add(button);
            }
            JButton button = (JButton)source;
            menu.show(button, 0, button.getBounds().height);
        }
        
    }
    
    void tryLogin(String user, String pass) {
        String ret = Messenger.LogIn(esql,user, pass);
        System.out.println("trying login: " + ret);
        if(ret != null)
        {
            if(Pattern.matches("Error: .*",ret)) {
                clearLogin();
                loginError.setText(ret.split(" ",2)[1]);
                return;
            }
        }
        else
        {
            loginError.setText("Unable to Connect!");
            return;
        }
        
        clearLogin();
        login(user);
    }
    
    void clearLogin() {
        loginError.setText("");
        loginUser.setText("");
        loginPass.setText("");
        Component owner = (Component)((JFrame) SwingUtilities.getWindowAncestor(loginUser)).getFocusOwner();
        loginUser.requestFocusInWindow();
        loginPass.requestFocusInWindow();
        owner.requestFocusInWindow();
    }
    
    void clearSignUp() {
        signUpError.setText("");
        signUpUser.setText("");
        signUpPass.setText("");
        phoneLine.setValue(null);
        Component owner = (Component)((JFrame) SwingUtilities.getWindowAncestor(loginUser)).getFocusOwner();
        signUpUser.requestFocusInWindow();
        signUpPass.requestFocusInWindow();
        owner.requestFocusInWindow();
        
    }
    
    void login(String user) {
        MessengerUser.current = MessengerUser.getUser(user);
        
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(phoneLine);
        
        frame.setTitle("DBMS Online Messenger - " + user);
        
        startDaemons();
        
        frame.setJMenuBar(menuBar);
        ((CardLayout)mainPanel.getLayout()).show(mainPanel,"HOME");
        
        //disableActiveChat();
    }
    
    void logout() {
    
        Messenger.Logout(esql,MessengerUser.current.name);
        MessengerUser.current = null;
        Chat.activeChat = null;
        
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(phoneLine);
        
        if(frame != null) {
            frame.setTitle("DBMS Online Messenger");
        }
        
        frame.setJMenuBar(null);
        ((CardLayout)mainPanel.getLayout()).show(mainPanel,"INIT");
        
        disableActiveChat();
    }
    
    void disableActiveChat() {
        Chat.activeChat = null;
        chatArea.setStyledDocument(new DefaultStyledDocument());
        usersModel.clear();
        chatArea.systemMessage("There is currently no active chat");
    }

    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("DBMS Online Messenger");

        //Create and set up the content pane.
        Messenger_GUI demo = new Messenger_GUI();
        gui = demo;
        frame.setContentPane(demo.createContentPane());
        
        frame.addWindowListener(demo);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(160*6, 90*6); //960 x 540
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
        try{
            // use postgres JDBC driver.
            //System.out.println("Driver?");
            Class.forName ("org.postgresql.Driver").newInstance ();
            //System.out.println("Yes: " + args.length);
            // instantiate the Messenger object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            String passwd = args[3];
            esql = new Messenger (dbname, dbport, user, passwd);
        }catch(Exception e) {
		    System.err.println (e.getMessage ());
        }/*finally{
            // make sure to cleanup the created table and close the connection.
            try{
            if(esql != null) {
                System.out.print("Disconnecting from database...");
                esql.cleanup ();
                System.out.println("Done\n\nBye !");
            }//end if
            }catch (Exception e) {
                // ignored.
            }//end try
        }//end try*/
        if(esql != null)
            System.out.println("Connected!");
        else
            System.out.println("Not Connected!");
    }
    
    protected void finalize() {
        if(MessengerUser.current != null) {
            logout();
        }
    
        try{
            if(esql != null) {
                System.out.print("Disconnecting from database...");
                esql.cleanup ();
                System.out.println("Done\n\nBye !");
            }//end if
        }catch (Exception e) {
            // ignored.
        }//end try
    }
    
    public void windowClosing(WindowEvent e) {
        
        finalize();
    }
    
    private class KeyEater extends KeyAdapter{
        public void keyTyped(KeyEvent e) {
            final String legalChars = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM._'" + (char)166;
            char c = e.getKeyChar();
            if(c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE &&
                c != KeyEvent.VK_TAB && !legalChars.contains(""+c))
                e.consume();
        }
    }
    
    private boolean shouldNotify = true;
    
    private abstract class DaemonManager<T> extends SwingWorker <Void, T> {
        T lastQuery = null;
        int period = 1000;
        
        DaemonManager() {
            super();
        }
        
        DaemonManager(int period) {
            super();
            this.period = period;
        }
        
        protected Void doInBackground() {
            while(shouldNotify) {
                T ret = doQuery();
                
                tryPublish(ret);
                
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    break;
                }
            }
            return null;
        }
        
        protected void process(List<T> queries) {
            //if(queries.size() == 0)
            //    return;
            
            for(T query : queries) {
                
                if(!matches(query,lastQuery)) {
                    processQuery(query);
                }
                
                lastQuery = query;
            }
        }
        
        protected void tryPublish(T ret) {
            if(ret != null)
                publish(ret);
        }
        
        protected boolean matches(T a, T b) { return a.equals(b); }
        
        protected abstract T doQuery();
        protected abstract void processQuery(T query);
    }
    
    private void startDaemons() {
        new NotificationManager(100).execute();
        new ChatListManager(100).execute();
        new ChatUsersManager(100).execute();
        new ContactsManager(100).execute();
        new BlockedManager(100).execute();
        //new ChatHistoryManager(100).execute();
        new ExpireManager(100).execute();
    }
    
    private class NotificationManager extends DaemonManager<String> {
        public NotificationManager(int period) {super(period);}
        public NotificationManager() {super();}
    
        protected String doQuery() {
            return Messenger.ReadNotifications(esql, MessengerUser.current.name);
        }
        
        protected void processQuery(String query) {
            System.out.println(query);
            
            System.out.println("Notification: ." + query+".");
            Notification.clear();
            
            if(query.equals("")) {
                notifButton.setIcon(inactiveIcon);
                return;
            }
            else
                notifButton.setIcon(activeIcon);
                
            System.out.println(query);
            
            String[] notifications = query.split("\\|\\[\\(\\^\\#\\^\\)\\]\\|");
            
            for(String notification : notifications) {
                String[] components = notification.split("\\\\n");
                int mid = Integer.parseInt(components[0]);
                Notification n = Notification.getNotification(mid);
                n.mid = mid;
                n.cid = Integer.parseInt(components[1]);
                n.user = components[2];
                n.timestamp = components[3];
                
            }
        }
        
        @Override
        protected void tryPublish(String ret) {
            if(ret != null)
                publish(ret);
            else
                publish("");
        }
    }
    
    private class ChatListManager extends DaemonManager<String> {
        public ChatListManager(int period) {super(period);}
        public ChatListManager() {super();}
    
        protected String doQuery() {
            return Messenger.ListUserChats(esql, MessengerUser.current.name);
            //nid,mid,user,timestamp;
        }
        
        protected void processQuery(String query) {
            chatsModel.clear();
            
            String[] chats = query.split("\\|\\[\\(\\^\\#\\^\\)\\]\\|");
            
            for(String chat : chats)
                chatsModel.addElement(chat.split("\\\\n")[0]);
        }
    }
    
    private class ChatUsersManager extends DaemonManager<String> {
        Chat lastChat = Chat.activeChat;
    
        public ChatUsersManager(int period) {super(period);}
        public ChatUsersManager() {super();}
    
        protected String doQuery() {
            if(lastChat != null)
                return Messenger.ListChatMembers(esql, Chat.activeChat.cid);
            
            lastChat = Chat.activeChat;
            return null;
        }
        
        protected void processQuery(String query) {
            usersModel.clear();
            String[] users = query.split("\\|\\[\\(\\^\\#\\^\\)\\]\\|");
            MessengerUser[] activeUsers = new MessengerUser[users.length];
            
            int index = 0;
            for(String user : users) {
                String[] components = user.split("\\\\n");
                String name = components[0];
                String status = components[1];
                usersModel.addElement(name + " (" + status + ")");
                activeUsers[index] = MessengerUser.getUser(name);
                activeUsers[index++].status = status;
            }
            
            lastChat.activeUsers = activeUsers;
            lastChat = Chat.activeChat;    
        }
    }
    
    private class ContactsManager extends DaemonManager<String> {
        public ContactsManager(int period) {super(period);}
        public ContactsManager() {super();}
    
        protected String doQuery() {
            return Messenger.ListContacts(esql, MessengerUser.current.name);
        }
        
        protected void processQuery(String query) {
            String[] users = query.split(",");
            MessengerUser[] contacts = new MessengerUser[users.length];
            
            int index = 0;
            for(String user : users) {
                contacts[index++] = MessengerUser.getUser(user);
            }
            
            MessengerUser.current.contacts = contacts;
        }
    }
    
    private class BlockedManager extends DaemonManager<String> {
        public BlockedManager(int period) {super(period);}
        public BlockedManager() {super();}
    
        protected String doQuery() {
            return Messenger.ListBlocks(esql, MessengerUser.current.name);
        }
        
        protected void processQuery(String query) {
            String[] users = query.split(",");
            MessengerUser[] blocked = new MessengerUser[users.length];
            
            int index = 0;
            for(String user : users) {
                blocked[index++] = MessengerUser.getUser(user);
            }
            
            MessengerUser.current.blocked = blocked;
        }
    }
    
    private class ExpireManager extends DaemonManager<Void> {
        public ExpireManager(int period) {super(period);}
        public ExpireManager() {super();}
        
        protected Void doQuery() {
            Messenger.RemoveExpired(esql);
            return null;
        }
        protected void processQuery(Void v) {}
    }
    
    public class ChatHistoryManager extends DaemonManager<String[]> {
        Chat chat = null;
        private boolean wait = false;
    
        public ChatHistoryManager(int period, Chat chat) {super(period); this.chat = chat;}
        //public ChatHistoryManager(int period) {super(period);}
        //public ChatHistoryManager() {super();}
    
        protected String[] doQuery() {
            
            if(wait)
                return null;
            
            if(chat != null) {
                String[] ret = Messenger.GetChatHistory(esql, chat.cid, chat.lastUpdate);
                wait = true;
                return ret;
            }
            
            return null;
        }
        
        @Override 
        protected boolean matches(String[] a, String[] b) {
            if(a == null || b == null)
                if(b == a) {
                    wait = false;
                    return true;
                }
                else
                    return false;
        
            if(a.length != b.length)
                return false;
                
            for(int i = 0; i < a.length; ++i) {
                if(!a[i].equals(b[i]))
                    return false;
            }
            
            wait = false;
            return true;
        }
        
        protected void processQuery(String[] query) {
            
            chat.clear();
            
            for(String s : query){
                String[] components = s.split("\n",6);
                int mid = Integer.parseInt(components[0]);
                Message message = Message.getMessage(mid);
                message.mid = mid;
                message.user = components[1];
                message.timestamp = components[2];
                message.text = components[3];
                message.media_type = components[4];
                message.media_url = components[5];
                
                chat.updateMessage(message);
                
                System.out.println(s);
            }
            
            wait = false;
        }
    }
    
    private class ChatListListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JList list = (JList)e.getSource();
            if(e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                setActiveChat(Integer.parseInt(chatsModel.get(index)));
            }
        }
    }
    
    void setActiveChat(int cid) {
        Chat.activeChat = Chat.getChat(cid);
        chatArea.setStyledDocument(Chat.activeChat.doc);
        
        chatsList.setSelectedIndex(chatsModel.indexOf(Integer.toString(cid)));
    }
}
