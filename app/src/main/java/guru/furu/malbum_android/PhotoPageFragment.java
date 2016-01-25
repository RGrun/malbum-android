package guru.furu.malbum_android;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.Comment;
import guru.furu.malbum_android.model.MalbumUser;

/**
 * Created by richard on 1/21/16.
 *
 * Holds a single photo.
 */
public class PhotoPageFragment extends Fragment {


    private ImageView photoImage;
    private TextView photoUser;
    private TextView photoDate;
    private LinearLayout commentHolder;
    private EditText newComment;
    private Button postNewComment;

    private ProgressDialog mProgress;

    private AlbumPhoto photo;

    private MalbumUser malbumUser;

    private String photoId;

    private static final String DEBUG = "PhotoPageFragment";

    public static PhotoPageFragment newInstance() {
        return new PhotoPageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.photo_fragment, container, false);

        photoImage = (ImageView) v.findViewById(R.id.image_holder);
        photoUser = (TextView) v.findViewById(R.id.photo_user);
        photoDate = (TextView) v.findViewById(R.id.photo_date);

        commentHolder = (LinearLayout) v.findViewById(R.id.comment_holder);

        newComment = (EditText) v.findViewById(R.id.new_comment);

        postNewComment = (Button) v.findViewById(R.id.submit_comment);

        postNewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new PostCommentTask().execute(newComment.getText().toString());
            }
        });

        malbumUser = ((PhotoPageActivity) getActivity()).getMalbumUser();
        photoId = getActivity().getIntent().getStringExtra("photo_id");

        if(photo == null) {
            fetchPhoto();
        } else {
            bindPhotoData();
        }

        return v;
    }


    private void fetchPhoto() {
        // attempt contact of server here
        mProgress = ProgressDialog.show(getActivity(), "Contacting Server",
                "Fetching photo information...");

        new FetchPhotoTask().execute();
    }

    private class FetchPhotoTask extends AsyncTask<Void, Void, AlbumPhoto> {

        public FetchPhotoTask() {}

        @Override
        protected AlbumPhoto doInBackground(Void... params) {

            try {
                return ServerConnect.getPhotoInformation(malbumUser.getHostname(),
                        malbumUser.getApi_key(), photoId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // (hopefully) never reach here
            return null;
        }

        @Override
        protected void onPostExecute(AlbumPhoto item) {
            photo = item;

            if(mProgress != null) {
                mProgress.dismiss();
            }

            bindPhotoData();

        }
    }

    private class PostCommentTask extends AsyncTask<String, Void, Boolean> {

        String commentToPost;

        public PostCommentTask() {}

        @Override
        protected Boolean doInBackground(String... params) {

            commentToPost = params[0];

            try {
                return ServerConnect.postComment(commentToPost, malbumUser, photoId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // (hopefully) never reach here
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // success
            if(result) {
                // append new comment to list of comments
                View v = getLayoutInflater(null).inflate(R.layout.comment, null);

                TextView tv = (TextView) v.findViewById(R.id.comment_text);
                TextView uv = (TextView) v.findViewById(R.id.comment_name);

                tv.setText(newComment.getText());
                uv.setText("-" + malbumUser.getUname());

                commentHolder.addView(v);
                newComment.setText(""); // clear edittext

                Toast.makeText(getActivity(), R.string.comment_posted, Toast.LENGTH_SHORT).show();
            } else {
                // failure
                Toast.makeText(getActivity(), R.string.comment_failure, Toast.LENGTH_LONG).show();
            }


        }
    }

    // bind model to view
    private void bindPhotoData() {

        if(!photo.getCustom_name().equals("null")) {
            ((PhotoPageActivity) getActivity()).setToolbarText(photo.getCustom_name());
        } else {
            ((PhotoPageActivity) getActivity()).setToolbarText(photo.getName());
        }

        photoUser.setText(photo.getUname());
        photoDate.setText(photo.getUpload_date());

        photoImage.setImageBitmap(photo.getPhoto());


        for(Comment cmt : photo.getComments()) {

            View v = getLayoutInflater(null).inflate(R.layout.comment, null);

            TextView tv = (TextView) v.findViewById(R.id.comment_text);
            TextView uv = (TextView) v.findViewById(R.id.comment_name);

            tv.setText(cmt.getComment());
            uv.setText("-" + cmt.getUname());

            commentHolder.addView(v);
        }

    }

}
