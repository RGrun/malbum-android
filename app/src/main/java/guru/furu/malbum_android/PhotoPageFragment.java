package guru.furu.malbum_android;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import guru.furu.malbum_android.model.AlbumPhoto;

/**
 * Created by richard on 1/21/16.
 *
 * Holds a single photo.
 */
public class PhotoPageFragment extends Fragment {

    private TextView photoName;
    private ImageView photoImage;
    private TextView photoUser;
    private TextView photoDate;
    private LinearLayout commentHolder;
    private EditText newComment;
    private Button postNewComment;

    private ProgressDialog mProgress;

    private AlbumPhoto photo;

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

        photoName = (TextView) v.findViewById(R.id.photo_name);
        photoImage = (ImageView) v.findViewById(R.id.image_holder);
        photoUser = (TextView) v.findViewById(R.id.photo_user);
        photoDate = (TextView) v.findViewById(R.id.photo_date);

        commentHolder = (LinearLayout) v.findViewById(R.id.comment_holder);

        newComment = (EditText) v.findViewById(R.id.new_comment);

        postNewComment = (Button) v.findViewById(R.id.submit_comment);

        postNewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Hook up new comment posting
            }
        });

        fetchPhoto();

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
                //TODO: write serverconnect method for getting photo information
                return new ServerConnect(malbumUser.getHostname(), malbumUser.getApi_key())
                        .getPhotosForUser(userOfAlbumToDisplay);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(AlbumPhoto item) {
            photo = item;

            if(mProgress != null) {
                mProgress.dismiss();
            }
        }
    }

}
