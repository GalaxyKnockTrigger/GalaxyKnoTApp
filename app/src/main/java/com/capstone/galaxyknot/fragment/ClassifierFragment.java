package com.capstone.galaxyknot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.databinding.ClassifierFragmentBinding;

public class ClassifierFragment extends Fragment {
    private ClassifierFragmentBinding binding;

    public ClassifierFragment() {
        // Required empty public constructor
    }

    public static ClassifierFragment newInstance() {
        ClassifierFragment fragment = new ClassifierFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_classifier, container, false);

        binding.classifierButton.setOnClickListener((view -> {
            if(view.isSelected()){
                view.setSelected(false);
            }else{
                view.setSelected(true);
            }
        }));

        return binding.getRoot();
    }
}
