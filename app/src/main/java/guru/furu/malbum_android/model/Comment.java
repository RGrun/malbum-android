package guru.furu.malbum_android.model;

/**
 * Created by richard on 1/23/16.
 *
 * Represents a comment on a photo.
 */
public class Comment {
    private String uname;
    private String date;
    private String comment;
    private int userId;
    private int photoId;
    private int commentId;

    public Comment(String uname, String date, String comment, int userId,
                   int photoId, int commentId) {
        this.uname = uname;
        this.date = date;
        this.comment = comment;
        this.userId = userId;
        this.photoId = photoId;
        this.commentId = commentId;
    }

    public String getUname() {
        return uname;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public int getUserId() {
        return userId;
    }

    public int getPhotoId() {
        return photoId;
    }

    public int getCommentId() {
        return commentId;
    }
}
