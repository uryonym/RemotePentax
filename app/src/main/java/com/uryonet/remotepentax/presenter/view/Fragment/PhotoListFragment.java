package com.uryonet.remotepentax.presenter.view.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.uryonet.remotepentax.R;
import com.uryonet.remotepentax.model.entity.PhotoDir;
import com.uryonet.remotepentax.presenter.contract.PhotoListContract;
import com.uryonet.remotepentax.presenter.presenter.PhotoListPresenter;
import com.uryonet.remotepentax.presenter.view.adapter.FilterableSection;
import com.uryonet.remotepentax.presenter.view.adapter.PhotoSection;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class PhotoListFragment extends Fragment implements PhotoListContract.View {

    RecyclerView rvPhotoList;
    View currentView;

    public static final String TAG = "PhotoListFragment";

    SectionedRecyclerViewAdapter sectionAdapter;
    PhotoListPresenter photoListPresenter;

    private ActionMode actionMode;
    private int currentId = R.id.rbAll;
    private boolean isMultSelect = false;
    private List<String> selectedUrls = new ArrayList<>();

    public PhotoListFragment(){};

    public static PhotoListFragment newInstance() {
        return new PhotoListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "process onCreateView");
        return inflater.inflate(R.layout.fragment_photo_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "process onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        rvPhotoList = (RecyclerView) view.findViewById(R.id.rvPhotoList);
        currentView = view;

        setupMVP();
        setupViews();
        getPhotoList();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "process onStart");
        super.onStart();
        EventBus.getDefault().register(photoListPresenter);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "process onStop");
        EventBus.getDefault().unregister(photoListPresenter);
        super.onStop();
    }

    private void setupMVP() {
        photoListPresenter = new PhotoListPresenter(this);
    }

    private void setupViews() {
        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
            switch (sectionAdapter.getSectionItemViewType(position)) {
                case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                    return 3;
                default:
                    return 1;
            }
            }
        });
        rvPhotoList.setLayoutManager(glm);
    }

    private void getPhotoList() {
        photoListPresenter.getPhotoList();
    }

    private void setPhotoFilter(View view) {
        final View localView = view;
        RadioGroup rg = (RadioGroup) localView.findViewById(R.id.rgChangeFile);
        rg.check(currentId);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                currentId = i;
                RadioButton currentRb = (RadioButton) localView.findViewById(i);
                String filterTxt = currentRb.getText().toString();
                if (filterTxt.equals("RAW")) {
                    filterTxt = "PEF";
                }
                for (Section section : sectionAdapter.getCopyOfSectionsMap().values()) {
                    if (section instanceof FilterableSection) {
                        ((FilterableSection) section).filter(filterTxt);
                    }
                }
                sectionAdapter.notifyDataSetChanged();
            }
        });
        Log.d(TAG, String.valueOf(currentId));
        if (currentId != R.id.rbAll) {
            RadioButton currentRb = (RadioButton) localView.findViewById(currentId);
            String filterTxt = currentRb.getText().toString();
            Log.d(TAG, filterTxt);
            if (filterTxt.equals("RAW")) {
                filterTxt = "PEF";
            }
            for (Section section : sectionAdapter.getCopyOfSectionsMap().values()) {
                if (section instanceof FilterableSection) {
                    ((FilterableSection) section).filter(filterTxt);
                }
            }
            sectionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayPhotoList(List<PhotoDir> photoDirList, List<String> photoUrlList) {
        if(photoDirList != null) {
            sectionAdapter = new SectionedRecyclerViewAdapter();

            for(PhotoDir dir : photoDirList) {
                sectionAdapter.addSection(new PhotoSection(dir.getName(), dir.getFiles(), photoUrlList, getContext()) {

                    // アイテムタップ時イベント
                    @Override
                    protected void onPhotoClicked(View view, @NonNull String photoFile, @NonNull List<String> photoUrlList, int position) {
                        super.onPhotoClicked(view, photoFile, photoUrlList, position);
                        if (isMultSelect) {
//                            multiSelect(photoFile, position);
                        } else {
                            PhotoPagerFragment ppFragment = PhotoPagerFragment.newInstance(photoFile, (ArrayList<String>) photoUrlList);
                            getFragmentManager().beginTransaction().addToBackStack(PhotoPagerFragment.TAG).replace(R.id.fragment_container, ppFragment).commit();
                        }
                    }

                    // アイテムロングタップ時イベント
                    @Override
                    protected void onPhotoLongClicked(View view, @NonNull String photoFile, @NonNull List<String> photoUrlList, int position) {
                        super.onPhotoClicked(view, photoFile, photoUrlList, position);
                        Log.d(TAG, "Long Tap Event");
                        if (!isMultSelect) {
                            selectedUrls = new ArrayList<>();
                            isMultSelect = true;
                        }

                        PhotoPagerFragment ppFragment = PhotoPagerFragment.newInstance(photoFile, (ArrayList<String>) photoUrlList);

                        getFragmentManager().beginTransaction().addToBackStack(PhotoPagerFragment.TAG).replace(R.id.fragment_container, ppFragment).commit();
                    }

                });
            }
            rvPhotoList.setAdapter(sectionAdapter);
            setPhotoFilter(currentView);
        } else {
            Log.d(TAG, "PhotoDirs response null");
        }
    }

    @Override
    public void displayError(String s) {
        Log.e(TAG, s);
    }

}
