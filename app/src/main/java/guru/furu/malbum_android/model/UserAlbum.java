package guru.furu.malbum_android.model;

/**
 * Created by richard on 1/12/16.
 *
 * This class represents entries in AlbumFragment's RecyclerView.
 */
public class UserAlbum {

    private String userName;
    private String albumImageUrl;

    public UserAlbum(String userName, String albumImageUrl) {
        this.userName = userName;
        this.albumImageUrl = albumImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public void setAlbumImageUrl(String albumImageUrl) {
        this.albumImageUrl = albumImageUrl;
    }
}
