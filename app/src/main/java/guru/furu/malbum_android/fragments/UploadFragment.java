package guru.furu.malbum_android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import guru.furu.malbum_android.R;
import guru.furu.malbum_android.activities.TabbedGalleryActivity;
import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.MalbumUser;
import guru.furu.malbum_android.util.PictureUtils;
import guru.furu.malbum_android.util.ServerConnect;

/**
 * Created by richard on 1/10/16.
 *
 * This fragment is responsible for communicating with the camera app
 * and uploading the new image to the server.
 */
public class UploadFragment extends Fragment {

    private TextView uploadInstructions;
    private ImageButton uploadImage;
    private Button uploadButton;
    private EditText uploadCustomName;
    private EditText uploadDescription;

    private AlbumPhoto imageToUpload;

    private ProgressDialog mProgress;

    private File photoFile;

    private MalbumUser user;

    private static final int REQUEST_PHOTO = 0;

    private static final String DEBUG = "UploadFragment";

    public static UploadFragment newInstance() {
        return new UploadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        user = ((TabbedGalleryActivity)getActivity()).getMalbumUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.upload_fragment, container, false);

        if(imageToUpload == null) {
            imageToUpload = new AlbumPhoto();
            imageToUpload.setPhotoFilename("IMG_" + new Date() + ".jpg");
        }

        if(photoFile == null) {
            photoFile = AlbumPhoto.getPhotoFile(getActivity(), imageToUpload);
        }

        uploadInstructions = (TextView) v.findViewById(R.id.upload_instructions);

        uploadImage = (ImageButton) v.findViewById(R.id.upload_image);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = captureImage.
                resolveActivity(getActivity().getPackageManager()) != null;

        uploadImage.setEnabled(canTakePhoto);

        if(canTakePhoto) {
            Uri uri = Uri.fromFile(photoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch camera app
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });


        uploadButton = (Button) v.findViewById(R.id.upload_button);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postPhoto();
            }
        });


        if(photoFile != null) {
            updatePhoto();
        }

        uploadCustomName = (EditText) v.findViewById(R.id.upload_custom_name);


        uploadDescription = (EditText) v.findViewById(R.id.upload_comment);

        return v;
    }

    private void updatePhoto() {
        if(photoFile == null || !photoFile.exists()) {
            // do something here?
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            uploadImage.setImageBitmap(bitmap);

            uploadButton.setEnabled(true);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            Log.e(DEBUG, "Problem with camera activity.");
            return;
        }

        if(requestCode == REQUEST_PHOTO) {
            updatePhoto();
        }
    }

    private void postPhoto() {

        // attempt contact of server here
        mProgress = ProgressDialog.show(getActivity(), "Contacting Server",
                "Uploading photo to server...");


        new PostPhotoTask().execute(uploadCustomName.getText().toString(),
                uploadDescription.getText().toString());
    }


    private class PostPhotoTask extends AsyncTask<String, Void, Boolean> {


        public PostPhotoTask() {}

        @Override
        protected Boolean doInBackground(String... params) {

            String customName = params[0];
            String customDescription = params[1];


            try {
                return ServerConnect.postPhoto(user, photoFile, customName, customDescription);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // (hopefully) never reach here
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            mProgress.dismiss();

            // success
            if(result) {
                Toast.makeText(getActivity(), R.string.upload_success, Toast.LENGTH_LONG).show();

                photoFile.delete();

                uploadButton.setEnabled(false);
                uploadCustomName.getText().clear();
                uploadDescription.getText().clear();

                ((TabbedGalleryActivity) getActivity())
                        .setCurrentTab(TabbedGalleryActivity.LATEST_TAB);


            } else {

                Toast.makeText(getActivity(), R.string.upload_failure, Toast.LENGTH_LONG).show();

            }


        }
    }

}
