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

import guru.furu.malbum_android.model.MalbumUser;
import guru.furu.malbum_android.model.UserAlbum;

/**
 * Created by richard on 1/10/16.
 *
 * The "Latest Images" from every user in the system.
 */
public class AlbumFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<UserAlbum> albums;
    private MalbumUser malbumUser;
    private ThumbnailDownloader<AlbumHolder> thumbnailDownloader;
    private static final String DEBUG = "AlbumFragment";

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
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

                new ThumbnailDownloader.ThumbnailDownloadListener<AlbumHolder>() {
                    @Override
                    public void onThumbnailDownloaded(AlbumHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                });

        thumbnailDownloader.start();
        thumbnailDownloader.getLooper();
        Log.i(DEBUG, "Background thread started.");
        albums = new ArrayList<>();
        //updateItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thumbnailDownloader.quit();
        Log.i(DEBUG, "Background thread destroyed.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailDownloader.clearQueue();
    }

    /*@Override
    public void onStop() {
        super.onStop();
        thumbnailDownloader.clearQueue();
        thumbnailDownloader.quit();
        Log.i(DEBUG, "Background thread destroyed.");
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.album_fragment, container, false);

        recyclerView = (RecyclerView)
                v.findViewById(R.id.fragment_photo_album_recycler_view);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL));


        setupAdapter();

        return v;
    }


    private void setupAdapter() {

        // isAdded() checks to see if the fragment is hosted inside an activity
        if(isAdded()) {
            recyclerView.setAdapter(new AlbumAdapter(albums));
        }
    }

    private class FetchAlbumsTask extends AsyncTask<Void, Void, List<UserAlbum>> {

        public FetchAlbumsTask() {}

        @Override
        protected List<UserAlbum> doInBackground(Void... params) {

            try {
                return ServerConnect.fetchAlbums(malbumUser.getHostname(), malbumUser.getApi_key());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<UserAlbum> items) {
            albums = items;
            setupAdapter();
        }
    }

    // holder and adapter are for the RecyclerView internals
    private class AlbumHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {


        private ImageView itemImageView;
        private TextView itemTextView;
        private UserAlbum userAlbum;

        public void bindUserAlbum(UserAlbum UserAlbum) {
            userAlbum = UserAlbum;
        }

        @Override
        public void onClick(View v) {
            Intent i = SingleUserAlbumActivity.newIntent(getActivity(), userAlbum.getUserName());
            startActivity(i);
        }



        public AlbumHolder(View itemView) {
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

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumHolder> {

        private List<UserAlbum> userAlbums;

        public AlbumAdapter(List<UserAlbum> userAlbums) {
            this.userAlbums = userAlbums;
        }

        @Override
        public AlbumHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());


            // inflate proper layout and pass to holder's constructor
            View view = inflater.inflate(R.layout.album_item, viewGroup, false);

            return new AlbumHolder(view);
        }


        // bind model to view
        @Override
        public void onBindViewHolder(AlbumHolder AlbumHolder, int position) {
            UserAlbum UserAlbum = userAlbums.get(position);

            // TODO: get better placeholder image
            Drawable placeholder = getResources().getDrawable(R.drawable.placeholder);
            AlbumHolder.bindUserAlbum(UserAlbum);

            AlbumHolder.bindDrawable(placeholder);
            AlbumHolder.setText(UserAlbum.getUserName());

            // make background thread download image thumbnail
            // the reference to the current item is passed on to the downloader
            thumbnailDownloader.queueThumbnail(AlbumHolder, UserAlbum.getAlbumImageUrl());

        }

        @Override
        public int getItemCount() {
            return userAlbums.size();
        }
    }

    private void updateItems() {
        new FetchAlbumsTask().execute();
    }


}
