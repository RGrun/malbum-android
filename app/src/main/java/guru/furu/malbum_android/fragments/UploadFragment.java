package guru.furu.malbum_android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import guru.furu.malbum_android.R;
import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.util.PictureUtils;

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

    private AlbumPhoto imageToUpload;

    private File photoFile;

    private static final int REQUEST_PHOTO = 0;

    private static final String DEBUG = "UploadFragment";

    public static UploadFragment newInstance() {
        return new UploadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);



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


        if(photoFile != null) {
            updatePhoto();
        }

        return v;
    }

    private void updatePhoto() {
        if(photoFile == null || !photoFile.exists()) {
            // do something here?
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            uploadImage.setImageBitmap(bitmap);


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

}
