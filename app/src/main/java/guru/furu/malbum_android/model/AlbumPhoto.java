package guru.furu.malbum_android.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.crypto.Cipher;

import guru.furu.malbum_android.ServerConnect;

/**
 * Created by richard on 1/18/16.
 *
 * Represents a photo.
 */
public class AlbumPhoto {

    private int photo_id;
    private int user_id;
    private String photo_path;
    private String upload_date;
    private String modified_date;
    private String uname;
    private String name;
    private String thumb_name;
    private String description;
    private String custom_name;
    private String hostname;

    private Bitmap photo;

    private List<Comment> comments;

    public AlbumPhoto(String hostname, JSONObject json) {
        this.hostname = hostname;
        try {
            photo_id = json.getInt("photo_id");
            user_id = json.getInt("user_id");
            photo_path = json.getString("photo_path");
            upload_date = json.getString("upload_date");
            modified_date = json.getString("modified_date");
            name = json.getString("name");
            thumb_name = json.getString("thumb_name");
            description = json.getString("description");
            custom_name = json.getString("custom_name");
            uname = json.getString("uname");
            comments = null;


        } catch (JSONException joe) {
            joe.printStackTrace();
        }

    }

    public AlbumPhoto(String hostname, JSONObject json, List<Comment> comments) {
        this.hostname = hostname;
        try {
            photo_id = json.getInt("photo_id");
            user_id = json.getInt("user_id");
            photo_path = json.getString("photo_path");
            upload_date = json.getString("upload_date");
            modified_date = json.getString("modified_date");
            name = json.getString("name");
            thumb_name = json.getString("thumb_name");
            description = json.getString("description");
            custom_name = json.getString("custom_name");
            uname = json.getString("uname");
            this.comments = comments;


        } catch (JSONException joe) {
            joe.printStackTrace();
        }

    }


    public String getFullImageURL() {
        return "http://" + hostname + ServerConnect.DEFAULT_PORT + "/img/" + uname + "/" + name;
    }

    public String getThumbImageURL() {
        return "http://" + hostname + ServerConnect.DEFAULT_PORT + "/img/" + uname + "/" + thumb_name;
    }

    public int getPhoto_id() {
        return photo_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public String getModified_date() {
        return modified_date;
    }

    public String getName() {
        return name;
    }

    public String getThumb_name() {
        return thumb_name;
    }

    public String getDescription() {
        return description;
    }

    public String getCustom_name() {
        return custom_name;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getUname() {
        return uname;
    }

    public String getHostname() {
        return hostname;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
}
