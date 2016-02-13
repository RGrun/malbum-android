package guru.furu.malbum_android.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.List;

import guru.furu.malbum_android.util.ServerConnect;

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

    // filename on Android device
    private String fileName;

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

            setPhotoFilename("IMG_" + name + ".jpg");


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

            setPhotoFilename("IMG_" + name + ".jpg");

        } catch (JSONException joe) {
            joe.printStackTrace();
        }

    }

    // this constructor is needed for the image upload progress
    public AlbumPhoto() {
        // stub
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

    public void setPhotoFilename(String newname) {
        this.fileName = newname;
    }

    public String getPhotoFilename() {
        return fileName;
    }

    public static File getPhotoFile(Context context, AlbumPhoto photo) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (externalFilesDir == null) {
            return null;
        }

        return new File(externalFilesDir, photo.getPhotoFilename());
    }
}
