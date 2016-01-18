package guru.furu.malbum_android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.MalbumUser;
import guru.furu.malbum_android.model.UserAlbum;

/**
 * Created by richard on 1/18/16.
 *
 * Displays photos for a single user album.
 */
public class SingleUserAlbumFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<AlbumPhoto> photosForUser;
    private MalbumUser malbumUser;
    private ThumbnailDownloader<AlbumPhotoHolder> thumbnailDownloader;

    private String userOfAlbumToDisplay;

    private static final String DEBUG = "SingleUserAlbumFragment";



    public static SingleUserAlbumFragment newInstance() {
        return new SingleUserAlbumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        this.malbumUser = ((SingleUserAlbumActivity)getActivity()).getMalbumUser();

        this.userOfAlbumToDisplay = getActivity().getIntent().getStringExtra("uname");


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
        photosForUser = new ArrayList<>();
        updateItems();
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();
        Log.i(DEBUG, "Background thread destroyed.");
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

   /* @Override
    public void onStop() {
        super.onStop();
        thumbnailDownloader.clearQueue();
        thumbnailDownloader.quit();
        Log.i(DEBUG, "Background thread destroyed.");
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.single_user_album_fragment, container, false);

        recyclerView = (RecyclerView)
                v.findViewById(R.id.fragment_single_user_album_recycler_view);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL));


        setupAdapter();

        return v;
    }


    private void setupAdapter() {

        // isAdded() checks to see if the fragment is hosted inside an activity
        if(isAdded()) {
            recyclerView.setAdapter(new AlbumAdapter(photosForUser));
        }
    }

    private class FetchAlbumsTask extends AsyncTask<Void, Void,
            List<AlbumPhoto>> {

        public FetchAlbumsTask() {}

        @Override
        protected List<AlbumPhoto> doInBackground(Void... params) {

            try {
                return new ServerConnect(malbumUser.getHostname(), malbumUser.getApi_key())
                        .getPhotosForUser(userOfAlbumToDisplay);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<AlbumPhoto> items) {
            photosForUser = items;
            setupAdapter();
        }
    }

    // holder and adapter are for the RecyclerView internals
    private class AlbumPhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {


        private ImageView itemImageView;
        private TextView itemTextView;
        private AlbumPhoto userPhoto;

        public void bindUserPhoto(AlbumPhoto albumPhoto) {
            userPhoto = albumPhoto;
        }

        @Override
        public void onClick(View v) {
            /*Intent i = PhotoPageActivity.newIntent(getActivity(), userAlbum.getPhotoPageUri());
            startActivity(i);*/
        }



        public AlbumPhotoHolder(View itemView) {
            super(itemView);

            itemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_album_image_view);
            itemTextView = (TextView) itemView.findViewById(R.id.album_name);

            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            itemImageView.setImageDrawable(drawable);
        }

        public void setText(String newText) {
            itemTextView.setText(newText);
        }
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
            View view = inflater.inflate(R.layout.album_item, viewGroup, false);

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

            AlbumPhoto.setText(customName);

            // make background thread download image thumbnail
            // the reference to the current item is passed on to the downloader
            thumbnailDownloader.queueThumbnail(AlbumPhoto, userPhoto.getThumbImageURL());

        }

        @Override
        public int getItemCount() {
            return userPhotos.size();
        }
    }


    private void updateItems() {
        new FetchAlbumsTask().execute();
    }

}
