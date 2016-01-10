package guru.furu.malbum_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by richard on 1/10/16.
 */
public class UploadFragment extends Fragment {

    public static UploadFragment newInstance() {
        return new UploadFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.upload_fragment, container, false);

        return v;
    }

}
