// code found at http://stackoverflow.com/questions/10506789/how-to-display-faint-gray-ghost-text-in-a-jtextfield
//     written by Andrew Thompson
//     modified to work with JPasswordField
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;

import java.text.*;

interface Condition{
        public boolean condition(JTextField f);
    }

public class GhostText extends LayerUI<JTextField> implements FocusListener, DocumentListener, PropertyChangeListener {
    private final JTextField textfield;
    private boolean isEmpty;
    private boolean hasFocus;
    private Color ghostColor;
    private Color foregroundColor;
    private final String ghostText;
    private char echo;
    private Condition c;
    

    protected GhostText(final JTextField textfield, String ghostText, Condition c) {
        super();
        foregroundColor = textfield.getForeground();
        this.c = c;
        this.textfield = textfield;
        this.ghostText = ghostText;
        this.ghostColor = Color.LIGHT_GRAY;
        textfield.addFocusListener(this);
        registerListeners();
        updateState();
        if (!this.textfield.hasFocus()) {
            focusLost(null);
        }
        
    }

    public void delete() {
        unregisterListeners();
        textfield.removeFocusListener(this);
    }

    private void registerListeners() {
        textfield.getDocument().addDocumentListener(this);
        textfield.addPropertyChangeListener("foreground", this);
    }

    private void unregisterListeners() {
        textfield.getDocument().removeDocumentListener(this);
        textfield.removePropertyChangeListener("foreground", this);
    }

    public Color getGhostColor() {
        return ghostColor;
    }

    public void setGhostColor(Color ghostColor) {
        this.ghostColor = ghostColor;
    }

    private void updateState() {
        isEmpty = c.condition(textfield);
        //if(!isEmpty)
        //    foregroundColor = textfield.getForeground();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (isEmpty) {
            unregisterListeners();
            hasFocus = true;
            try {
                /*if(JPasswordField.class.isInstance(textfield))
                {
                    JPasswordField pfield = (JPasswordField)textfield;
                    pfield.setEchoChar(echo);
                }
                textfield.setText("");
                textfield.setForeground(foregroundColor);*/
                textfield.setForeground(foregroundColor);
                textfield.repaint();
            } finally {
                registerListeners();
            }
        }

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (isEmpty) {
            unregisterListeners();
            hasFocus = false;
            try {
                /*if(JPasswordField.class.isInstance(textfield))
                {
                    JPasswordField pfield = (JPasswordField)textfield;
                    echo = pfield.getEchoChar();
                    pfield.setEchoChar('\0');
                }
                textfield.enableInputMethods(true);
                textfield.setText(ghostText);
                textfield.setForeground(ghostColor);*/
                textfield.setForeground(textfield.getBackground());
                textfield.repaint();
            } finally {
                registerListeners();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateState();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateState();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateState();
    }
    
    public void paint (Graphics g, JComponent c) {
        super.paint (g, c);
     
        JLayer jlayer = (JLayer)c;
        JTextField ftf = (JTextField)jlayer.getView();
        
        //if(textfield instanceof JFormattedTextField)
        //    System.out.println("" + isEmpty + ' ' + !hasFocus + ' ' + textfield.getText());
        if (isEmpty && !hasFocus){//!ftf.isEditValid()) {
            Graphics2D g2 = (Graphics2D)g.create();
            
            // Paint the red X.
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
            int w = c.getWidth();
            int h = c.getHeight();
            /* Draws a red box with a white x
            int s = 8;
            int pad = 4;
            int x = w - pad - s;
            int y = (h - s) / 2;
            g2.setPaint(Color.red);
            g2.fillRect(x, y, s + 1, s + 1);
            g2.setPaint(Color.white);
            g2.drawLine(x, y, x + s, y + s);
            g2.drawLine(x, y + s, x + s, y);
            */
            g2.setPaint(ghostColor);
            Font font = textfield.getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(ghostText, font, frc);
            Rectangle rect = layout.getPixelBounds(frc,0,0);
            layout.draw(g2, (float)w/2 - rect.width/2, (float)h/2 + rect.height/2 - 4);
            
            g2.dispose();
        }
    }

}
