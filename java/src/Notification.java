import java.util.*;

class Notification {
    private static Hashtable<Integer, Notification> notifications = new Hashtable<Integer,Notification>();
    
    public int mid;
    public int cid;
    public String user;
    public String timestamp;
    
    public static Notification getNotification(int mid) {
        if(!notifications.containsKey(mid)) {
            Notification notification = new Notification();
            notification.mid = mid;
            notifications.put(mid,notification);
        }
        
        return notifications.get(mid);
    }
    
    public static Collection<Notification> getNotifications(){
        return notifications.values();
    }
    
    public static void removeNotification(int mid) {
        notifications.remove(mid);
    }
    
    public static void clear() {
        notifications.clear();
    }
}
