package guru.furu.malbum_android.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import guru.furu.malbum_android.activities.PhotoPageActivity;
import guru.furu.malbum_android.R;
import guru.furu.malbum_android.util.ServerConnect;
import guru.furu.malbum_android.activities.TabbedGalleryActivity;
import guru.furu.malbum_android.util.ThumbnailDownloader;
import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.MalbumUser;

/**
 * Created by richard on 1/12/16.
 *
 * "Latest images" fragment.
 */
public class LatestFragment extends Fragment {

    RecyclerView recyclerView;
    private List<AlbumPhoto> latestPhotos;
    private MalbumUser malbumUser;
    private ThumbnailDownloader<AlbumPhotoHolder> thumbnailDownloader;

    private static final String DEBUG = "LatestFragment";

    public static LatestFragment newInstance() {
        return new LatestFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        this.malbumUser = ((TabbedGalleryActivity)getActivity()).getMalbumUser();

        // this Handler belongs to the UI Thread's Looper
        // because it's created in the UI Thread.
        Handler responseHandler = new Handler();
        thumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        thumbnailDownloader.setThumbnailDownloadListener(

                new ThumbnailDownloader.ThumbnailDownloadListener<AlbumPhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(AlbumPhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                });

        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(DEBUG, "Background thread started.");
        latestPhotos = new ArrayList<>();
        updateItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.latest_fragment, container, false);

        recyclerView = (RecyclerView)
                v.findViewById(R.id.fragment_latest_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));


        setupAdapter();

        return v;
    }

    private void setupAdapter() {

        // isAdded() checks to see if the fragment is hosted inside an activity
        if(isAdded()) {
            recyclerView.setAdapter(new AlbumAdapter(latestPhotos));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }



    // holder and adapter are for the RecyclerView internals
    private class AlbumPhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {


        private ImageView photoImage;
        private TextView photoTitle;
        private TextView photoUser;
        private TextView photoDate;

        private AlbumPhoto userPhoto;

        public void bindUserPhoto(AlbumPhoto albumPhoto) {
            userPhoto = albumPhoto;
        }

        @Override
        public void onClick(View v) {

            String photoId = "" + userPhoto.getPhoto_id();

            Intent i = PhotoPageActivity.newIntent(getActivity(), photoId);
            startActivity(i);
        }



        public AlbumPhotoHolder(View itemView) {
            super(itemView);

            photoImage = (ImageView) itemView.findViewById(R.id.latest_image);
            photoTitle = (TextView) itemView.findViewById(R.id.latest_title);
            photoUser = (TextView) itemView.findViewById(R.id.latest_uname);
            photoDate = (TextView) itemView.findViewById(R.id.latest_date);

            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            photoImage.setImageDrawable(drawable);
        }

        public void setPhotoTitle(String newText) {
            photoTitle.setText(newText);
        }

        public void setPhotoUser(String newText) { photoUser.setText(newText); }

        public void setPhotoDate(String newText) { photoDate.setText(newText); }

    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumPhotoHolder> {

        private List<AlbumPhoto> userPhotos;

        public AlbumAdapter(List<AlbumPhoto> userAlbums) {
            this.userPhotos = userAlbums;
        }

        @Override
        public AlbumPhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());


            // inflate proper layout and pass to holder's constructor
            View view = inflater.inflate(R.layout.latest_image, viewGroup, false);

            return new AlbumPhotoHolder(view);
        }


        // bind model to view
        @Override
        public void onBindViewHolder(AlbumPhotoHolder AlbumPhoto, int position) {
            AlbumPhoto userPhoto = userPhotos.get(position);

            // TODO: get better placeholder image
            Drawable placeholder = getResources().getDrawable(R.drawable.placeholder);
            AlbumPhoto.bindUserPhoto(userPhoto);

            AlbumPhoto.bindDrawable(placeholder);

            // use normal photo name if no custom name is given

            String customName = userPhoto.getCustom_name();
            if(customName.equals("null")) {
                customName = userPhoto.getName();
            }

            AlbumPhoto.setPhotoTitle(customName);

            AlbumPhoto.setPhotoUser(userPhoto.getUname());
            AlbumPhoto.setPhotoDate(userPhoto.getUpload_date());


            /*Drawable drawable = new BitmapDrawable(getResources(), userPhoto.getPhoto());

            AlbumPhoto.bindDrawable(drawable);*/

            // make background thread download image thumbnail
            // the reference to the current item is passed on to the downloader
            thumbnailDownloader.queueThumbnail(AlbumPhoto, userPhoto.getThumbImageURL());

        }

        @Override
        public int getItemCount() {
            return userPhotos.size();
        }
    }

    private class FetchLatestTask extends AsyncTask<Void, Void,
            List<AlbumPhoto>> {

        public FetchLatestTask() {}

        @Override
        protected List<AlbumPhoto> doInBackground(Void... params) {

            try {

                return ServerConnect.getLatestPhotos(malbumUser.getHostname(),
                        malbumUser.getApi_key());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<AlbumPhoto> items) {
            latestPhotos = items;
            setupAdapter();
        }
    }

    private void updateItems() {
        new FetchLatestTask().execute();
    }

}
