package com.capstone.galaxyknot.fragment;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.CollectorFragmentBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectorFragment extends Fragment {

    private CollectorFragmentBinding binding;

    public CollectorFragment() {
        // Required empty public constructor
    }

    public static CollectorFragment newInstance(String param1, String param2) {
        CollectorFragment fragment = new CollectorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collector, container, false);
        binding.setFrag(this);
        binding.setIsClassifier(StateManager.isNowClassifierState);

        return binding.getRoot();
    }
}