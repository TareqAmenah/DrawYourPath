package com.tradinos.drawyourpath.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tradinos.drawyourpath.MainActivity;
import com.tradinos.drawyourpath.MyPath;
import com.tradinos.drawyourpath.PathViewModel;
import com.tradinos.drawyourpath.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class HomeFragment extends Fragment {

    private EditText edtFrom, edtTo, edtDistance, edtDuration;
    private Button bSave;

    private PathViewModel mPathViewModel;

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).bottom_sheet.setVisibility(View.VISIBLE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        edtFrom = root.findViewById(R.id.from_edt);
        edtTo = root.findViewById(R.id.to_edt);
        edtDistance = root.findViewById(R.id.distance_edt);
        edtDuration = root.findViewById(R.id.duration_edt);
        bSave = root.findViewById(R.id.save_button);

        mPathViewModel = new ViewModelProvider(requireActivity()).get(PathViewModel.class);

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sFrom = edtFrom.getText().toString();
                String sTo = edtTo.getText().toString();
                String sDistance = edtDistance.getText().toString();
                String sDuration = edtDuration.getText().toString();

                if(sFrom.equals("") || sTo.equals("") || sDistance.equals("") || sDuration.equals(""))
                    return;

                mPathViewModel.insertPath(new MyPath(sFrom, sTo, sDistance, sDuration));

            }
        });


        return root;
    }

}