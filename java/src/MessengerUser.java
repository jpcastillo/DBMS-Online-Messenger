//representation of User
class MessengerUser {
    public static MessengerUser current;
    public MessengerUser[] contacts = null;
    public MessengerUser[] blocked = null;
    public int uid = -1;
    public String name = "";
    public String status = "";
}
