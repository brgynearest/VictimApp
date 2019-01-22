package com.thesis.sad.victimapp.Model;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thesis.sad.victimapp.R;

public class BottomSheetVictim extends BottomSheetDialogFragment {
    String mTag;
    public static BottomSheetVictim newInstance(String tag)
    {
        BottomSheetVictim f= new BottomSheetVictim();
        Bundle args = new Bundle();
        args.putString("Tag",tag);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTag = getArguments().getString("TAG");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.bottom_sheet_victim,container,false);
        //TextView
        return view;
    }
}
