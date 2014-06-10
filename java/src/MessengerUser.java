import java.util.Hashtable;

//representation of User
class MessengerUser {
    public static MessengerUser current;
    public static Hashtable<String,MessengerUser> users = new Hashtable<String,MessengerUser>();
    public MessengerUser[] contacts = new MessengerUser[0];
    public MessengerUser[] blocked = new MessengerUser[0];
    public int uid = -1;
    public String name = "";
    public String status = "";
    
    public static MessengerUser getUser(String name) {
        if(!users.containsKey(name)) {
            MessengerUser user = new MessengerUser();
            users.put(name, user);
            user.name = name;
        }
        
        return users.get(name);
    }
}
