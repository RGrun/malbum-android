package guru.furu.malbum_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by richard on 1/10/16.
 */
public class GalleryFragment extends Fragment {

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.gallery_fragment, container, false);

        return v;
    }

}
