package dwapensk.hpu.edu.cannongame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
/**
 * Created by kiev on 3/10/2018.
 */
public class TitleFragment extends Fragment {
    private Button mStartButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savdInstanceState) {
        View view = inflater.inflate(R.layout.fragment_title, container, false);

        mStartButton = (Button) view.findViewById(R.id.start_game);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MainActivityFragment(),"findThisFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}
