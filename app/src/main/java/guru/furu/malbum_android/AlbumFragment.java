package guru.furu.malbum_android;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richard on 1/10/16.
 *
 * The "Latest Images" from every user in the system.
 */
public class AlbumFragment extends Fragment {

    private RecyclerView recyclerView;
    
    private List<UserAlbum> albums = new ArrayList<>();

    private MalbumUser malbumUser;

    // TODO: add handler stuff

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.album_fragment, container, false);

        recyclerView = (RecyclerView)
                v.findViewById(R.id.fragment_photo_album_recycler_view);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL));


        this.malbumUser = ((TabbedGalleryActivity)getActivity()).getMalbumUser();

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

            return new ServerConnect(malbumUser.getHostname(), malbumUser.getApi_key())
                    .fetchAlbums();
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


        private ImageView mItemImageView;
        private UserAlbum userAlbum;

        public void bindUserAlbum(UserAlbum UserAlbum) {
            userAlbum = UserAlbum;
        }


        @Override
        public void onClick(View v) {
          /*  Intent i = PhotoPageActivity.newIntent(getActivity(), userAlbum.getPhotoPageUri());
            startActivity(i);*/
        }



        public AlbumHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_album_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumHolder> {

        private List<UserAlbum> userAlbums;

        public AlbumAdapter(List<UserAlbum> UserAlbums) {
            userAlbums = UserAlbums;
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

            // make background thread download image thumbnail
            // the reference to the current item is passed on to the downloader
            mThumbailDownloader.queueThumbnail(AlbumHolder, UserAlbum.getAlbumImageUrl());

        }

        @Override
        public int getItemCount() {
            return userAlbums.size();
        }
    }

}
