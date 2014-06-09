import java.util.*;

class Message {
    private static Hashtable<Integer, Message> messages = new Hashtable<Integer,Message>();
    private static int smid = -10;
    
    public int mid = -1;
    public String user = "";
    public String timestamp = "";
    public String text = "";
    public String media_type = "";
    public String media_url = "";
    
    public static Message getMessage(int mid) {
        if(!messages.containsKey(mid)) {
            Message message = new Message();
            message.mid = mid;
            messages.put(mid,message);
        }
        
        return messages.get(mid);
    }
    
    public static Message systemMessage(String s) {
        Message message = new Message();
        message.mid = smid;
        message.text = s;
        
        messages.put(smid--,message);
        
        return message;
    }
    
    public static String[] getIds() {
        Enumeration<Integer> e = messages.keys();
        ArrayList<String> list = new ArrayList<String>();
        while(e.hasMoreElements()) {
            Integer i = e.nextElement();
            if(i.intValue() >= 0);
            list.add(i.toString());
        }
        
        Object[] array1 = list.toArray();
        
        String[] retArray = new String[array1.length];
        
        for(int i = 0; i < array1.length; ++i)
            retArray[i] = (String)array1[i];
        
        return retArray;
    }

}
