package com.tradinos.drawyourpath.ui.paths;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tradinos.drawyourpath.MainActivity;
import com.tradinos.drawyourpath.MyPath;
import com.tradinos.drawyourpath.PathViewModel;
import com.tradinos.drawyourpath.PathsAdapter;
import com.tradinos.drawyourpath.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PathsFragment extends Fragment {


    private PathViewModel mPathViewModel;
    private PathsAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPathViewModel = new ViewModelProvider(this).get(PathViewModel.class);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_paths, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.paths_recyclerView);
        adapter = new PathsAdapter(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));

        Toast.makeText(getActivity(),"paths fragment creat view", Toast.LENGTH_LONG);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mPathViewModel.getAllPaths()!=null)
        mPathViewModel.getAllPaths().observe(getViewLifecycleOwner(), new Observer<List<MyPath>>() {
            @Override
            public void onChanged(List<MyPath> myPaths) {
                adapter.setPaths(myPaths);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).bottom_sheet.setVisibility(View.INVISIBLE);
    }


}