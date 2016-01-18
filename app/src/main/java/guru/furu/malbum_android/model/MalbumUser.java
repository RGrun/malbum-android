package guru.furu.malbum_android.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.jar.JarException;

/**
 * Created by richard on 1/10/16.
 *
 * Represents a logged-in user to the system.
 */
public class MalbumUser {

    private final String uname;
    private final String user_id;
    private final String uname_lower;
    private final String fname;
    private final String lname;
    private final String api_key;
    private final String hostname;

    public MalbumUser(String uname,
                      String user_id,
                      String uname_lower,
                      String fname,
                      String lname,
                      String api_key,
                      String hostname) {

        this.uname = uname;
        this.user_id = user_id;
        this.uname_lower = uname_lower;
        this.fname = fname;
        this.lname = lname;
        this.api_key = api_key;
        this.hostname = hostname;
    }

    public MalbumUser(JSONObject data, String hostname) throws JSONException {
        this.uname = data.getString("uname");
        this.user_id = data.getString("user_id");
        this.uname_lower = data.getString("uname_lower");
        this.fname = data.getString("fname");
        this.lname = data.getString("lname");
        this.api_key = data.getString("api_key");
        this.hostname = hostname;
    }

    public String getUname() {
        return uname;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUname_lower() {
        return uname_lower;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getApi_key() {
        return api_key;
    }

    public String getHostname() {
        return hostname;
    }
}
