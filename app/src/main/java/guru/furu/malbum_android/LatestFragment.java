package guru.furu.malbum_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import guru.furu.malbum_android.model.AlbumPhoto;
import guru.furu.malbum_android.model.MalbumUser;

/**
 * Created by richard on 1/12/16.
 *
 * "Latest images" view.
 */
public class LatestFragment extends Fragment {

    //TODO: use linearlayoutmanager in recyclerview and hook up latest_image.xml

    RecyclerView recyclerView;
    private List<AlbumPhoto> photosForUser;
    private MalbumUser malbumUser;
    private ThumbnailDownloader<AlbumPhotoHolder> thumbnailDownloader;

    public static LatestFragment newInstance() {
        return new LatestFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.latest_fragment, container, false);

        return v;
    }
}
