import javax.swing.*;
//import java.awt.*; //don't want to import java.awt.List
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.AffineTransform;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.util.regex.Pattern;
import java.net.URL;

public class Messenger_GUI implements  ActionListener{

    // Definition of global values and items that are part of the GUI.
    int redScoreAmount = 0;
    int blueScoreAmount = 0;

    HashMap <JComponent, Enum> lookup;

    enum Panels {TITLE, SCORE, BUTTON, LAST};
    EnumMap <Panels, JPanel> panels;
    JPanel rightPanel, mainPanel;
    JMenuBar menuBar;
    
    /*enum Labels {RED, BLUE, RED_S, BLUE_S, LAST};
    EnumMap <Labels, JLabel> labels;
    JLabel redLabel, blueLabel, redScore, blueScore;
    
    enum Buttons {RED_BUTTON, BLUE_BUTTON, RESET_BUTTON, LAST};
    EnumMap <Buttons, JButton> buttons;
    JButton redButton, blueButton, resetButton;*/
    
    JFormattedTextField phoneLine;
    
    enum ToggleButtons {LOGIN, SIGN_UP, HELP};
    EnumMap <ToggleButtons, JToggleButton> tbuttons = new EnumMap <ToggleButtons, JToggleButton>(ToggleButtons.class);

    Condition emptyCondition = new Condition() {
        @Override
        public boolean condition(JTextField f){return f.getText().length() == 0;}
    };
        
    Condition phoneEmptyCondition = new Condition() {
        @Override
        public boolean condition(JTextField f){
            Object obj = ((JFormattedTextField)f).getValue();
            if( obj == null ) {
                System.out.println("NULL!");
                return !Pattern.matches("\\+\\d\\(\\d\\d\\d\\)\\d\\d\\d-\\d\\d\\d\\d",f.getText());
                }
                else
                System.out.println((String)obj);
            boolean r = !Pattern.matches(".*\\d",(String)obj) && !Pattern.matches("\\+\\d\\(\\d\\d\\d\\)\\d\\d\\d-\\d\\d\\d\\d",f.getText());
            return r;}
    };

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.PAGE_AXIS));
        loginPanel.add(Box.createVerticalGlue());
        
        
        
        JTextField userLine = new JTextField(8);
        //new GhostText(userLine, "Username", emptyCondition);
        userLine.setHorizontalAlignment(JTextField.CENTER);
        AffineTransform t = new AffineTransform();
        t.scale(2,2);
        userLine.setFont(userLine.getFont().deriveFont(t));
        userLine.setMaximumSize(new Dimension(Short.MAX_VALUE, userLine.getPreferredSize().height));
        loginPanel.add(new JLayer<JTextField>(userLine,new GhostText(userLine, "Username", emptyCondition)));
        
        JTextField passwordLine = new JPasswordField(8);
        passwordLine.enableInputMethods(true);
        //new GhostText(passwordLine, "Password", emptyCondition);
        passwordLine.setHorizontalAlignment(JTextField.CENTER);
        passwordLine.setFont(userLine.getFont());
        passwordLine.setMaximumSize(new Dimension(Short.MAX_VALUE, passwordLine.getPreferredSize().height));
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
        
        JTextField userLine = new JTextField(8);
        //new GhostText(userLine, "Username", emptyCondition);
        userLine.setHorizontalAlignment(JTextField.CENTER);
        AffineTransform t = new AffineTransform();
        t.scale(2,2);
        userLine.setFont(userLine.getFont().deriveFont(t));
        userLine.setMaximumSize(new Dimension(Short.MAX_VALUE, userLine.getPreferredSize().height));
        signUpPanel.add(new JLayer<JTextField>(userLine,new GhostText(userLine, "Username", emptyCondition)));
        
        JTextField passwordLine = new JPasswordField(8);
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
        helpPanel.add(new JLabel("Help :3"));
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
    
    private static String longestSubstring(JList jlist) {
        ListModel model = jlist.getModel();
        if(model instanceof DefaultListModel) {
            DefaultListModel dlm = (DefaultListModel)jlist.getModel();
            return longestSubstring((String[])dlm.toArray());
        }
        return "";
    }
    
    private static String longestSubstring(String[] strings) {
        
        int longest = 0;
        
        if(strings.length == 0)
            return "";
            
        char curr = strings[0].charAt(0);
        
        while(true) {
            for(String s: strings) {
                if(longest == s.length() || s.charAt(longest) != curr)
                    return s.substring(0,longest);
            }
            ++longest;
        }
    }
    
    JPanel createChatPanel()
    {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.LINE_AXIS));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        leftPanel.add(chatArea);
        
        JTextField messageField = new JTextField();
        messageField.setMaximumSize(new Dimension(Short.MAX_VALUE,chatArea.getPreferredSize().height));
        leftPanel.add(new JLayer<JTextField>(messageField,new GhostText(messageField, "Add a message...", emptyCondition)));
        leftPanel.setPreferredSize(new Dimension(1618, 1000));
        chatPanel.add(leftPanel);
        chatPanel.add(new JSeparator(JSeparator.VERTICAL));
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        
        
        
        DefaultListModel<String> usersModel = new DefaultListModel<String>();
        JList usersList = new JList<String>(usersModel);
        usersList.setPreferredSize(new Dimension(1000,1618));
        JList chatsList = new JList<String>(new DefaultListModel<String>());
        chatsList.setPreferredSize(new Dimension(1000,1000));
        usersModel.addElement("ONE");
        usersModel.addElement("TWO");
        usersModel.addElement("THREE");
        
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
        
        menuBar.add(userMenu);
        menuBar.add(chatMenu);
        menuBar.add(mesgMenu);
        
        menuBar.add(Box.createHorizontalGlue());
        
        JButton logoutButton = new JButton();
        logoutButton.setActionCommand("logout");
        logoutButton.addActionListener(this);
        URL imgUrl = getClass().getResource("images/Exclamation_mark_gray.png");
        ImageIcon dummy = new ImageIcon(imgUrl, "Dummy");
        Image img = dummy.getImage();
        Image newImg = img.getScaledInstance(32,32,java.awt.Image.SCALE_SMOOTH);
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
            System.out.println("going home! :3");
            ((JFrame) SwingUtilities.getWindowAncestor(phoneLine)).setJMenuBar(menuBar);
            ((CardLayout)mainPanel.getLayout()).show(mainPanel,"HOME");
        }
        else if(action.equals("sign up"))
        {
            //phoneLine.setText("");
            phoneLine.setValue(null);
            //Component owner = (Component)((JFrame) SwingUtilities.getWindowAncestor(phoneLine)).getFocusOwner();
            //phoneLine.requestFocusInWindow();
            //owner.requestFocusInWindow();
        }
        else if(action.equals("logout"))
        {
            //System.out.println("Logging out :3");
            //JPopupMenu menu = new JPopupMenu();
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            //menu.add(new JButton("Hello :3"));
            
            JButton button = (JButton)source;
            //menu.show(button, 0, button.getBounds().height);
        }
        
    }

    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("DBMS Online Messenger");

        //Create and set up the content pane.
        Messenger_GUI demo = new Messenger_GUI();
        frame.setContentPane(demo.createContentPane());

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
    }
}
