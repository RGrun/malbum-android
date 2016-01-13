package guru.furu.malbum_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by richard on 1/12/16.
 */
public class LatestFragment extends Fragment {

    RecyclerView recyclerView;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.album_fragment, container, false);

        return v;
    }
}
