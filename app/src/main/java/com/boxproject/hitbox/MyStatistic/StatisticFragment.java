package com.boxproject.hitbox.MyStatistic;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.boxproject.hitbox.MainActivity;
import com.boxproject.hitbox.R;
import com.boxproject.hitbox.data.TrainingDbHelper;
import com.boxproject.hitbox.data.TrainingsDbContract;

import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class StatisticFragment extends Fragment {

    private final String TAG = "statisticTest";

    private RecyclerView recyclerView;
    private StatisticDataAdapter statisticDataAdapter;
    private TextView noStatisticText;

    float radius = 20f;
    ViewGroup rootView;
    Drawable windowBackground;
    private BlurView blurView1;

    TrainingDbHelper trainingDbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingDbHelper = new TrainingDbHelper(getActivity());
        updateEntriesNumber();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.statistic_fragment, container, false);

        recyclerView = root.findViewById(R.id.recycler_view);
        noStatisticText = root.findViewById(R.id.no_statistic_text);
//        Button addButton = root.findViewById(R.id.add_statistic);
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                trainingDbHelper.insertTrainingItem(new TrainingDbItem("jjjjj", "01.01.1899", "1-2-3-4-5-6-7-8-9-10",20,10,420,4, 2,"20:55"));
//                trainingDbHelper.insertTrainingItem(new TrainingDbItem("jjjjj", "01.01.1899", "1-2-3-4-5-6-7-8-9-10",10,5,630,4, 2,"20:55"));
////                Log.i(TAG, "Db length " + trainingDbHelper.dbLength());
//            }
//        });
//        Button averageButton = root.findViewById(R.id.get_average);
//        averageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "average number of hits: " + trainingDbHelper.getColumnAverage(new String[] {TrainingsDbContract.COLUMN_NUMBER_OF_HITS})[0]);
//            }
//        });
        View decorView = getActivity().getWindow().getDecorView();
        rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        windowBackground = decorView.getBackground();

        beginStatisticList(trainingDbHelper.getTrainingDbList());
        blurView1 = root.findViewById(R.id.blur_view_1);
        BlurView blurView2 = root.findViewById(R.id.blur_view_2);

        blurView2.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        Log.i(TAG, "Last database ID: " + trainingDbHelper.getLastId());

        return root;
    }

    private void updateEntriesNumber(){
        if(trainingDbHelper != null) {
            int entries = trainingDbHelper.dbLength();
            if (entries == 1)
                ((MainActivity) getActivity()).setActionBarSubTitle(getString(R.string.number_of_statistic_entry, entries));
            else
                ((MainActivity) getActivity()).setActionBarSubTitle(getString(R.string.number_of_statistic_entries, entries));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_statistic_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                updateStatisticList(trainingDbHelper.searchInDb(s));
                //Log.i(TAG, "onQueryTextSubmit: " + s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                updateStatisticList(trainingDbHelper.searchInDb(s));
                //Log.i(TAG, "onQueryTextChange: " + s);
                return false;
            }
        });
    }

    private void beginStatisticList(List<StatisticListItem> statisticListItems){
        if(recyclerView != null){
            statisticDataAdapter = new StatisticDataAdapter(getActivity(), statisticListItems);
            recyclerView.setAdapter(statisticDataAdapter);
        }
        if(statisticListItems.size() == 0){
            noStatisticText.setVisibility(View.VISIBLE);
            blurView1.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                    .setBlurRadius(radius)
                    .setHasFixedTransformationMatrix(true);
        }
        else noStatisticText.setVisibility(View.INVISIBLE);
        statisticDataAdapter.setOnStatisticItemClickListener(new StatisticDataAdapter.StatisticItemClickListener() {
            @Override
            public void onClick(int position, View view, String title, int id) {
            float[] average = trainingDbHelper.getColumnAverage(new String[] {TrainingsDbContract.COLUMN_NUMBER_OF_HITS, TrainingsDbContract.COLUMN_AVERAGE_IMPACT_FORCE, TrainingsDbContract.COLUMN_STRONGEST_HIT,
                        TrainingsDbContract.COLUMN_NUMBER_OF_SERIES, TrainingsDbContract.COLUMN_HITS_PER_SERIES});
            int dbLength = trainingDbHelper.dbLength();
            ShowTrainingItem showTrainingItem = new ShowTrainingItem(trainingDbHelper.getTrainingDbItemById(id), average, dbLength);
                    showTrainingItem.show(getChildFragmentManager(), "showTrainingItem");
            }

            @Override
            public void onLongClick(final int position, View view, final String title, final int id) {
                Log.i(TAG, "onLongClick: ");
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.inflate(R.menu.statistic_item_popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.rename_item:
                                RenameStatisticItem renameStatisticItem = new RenameStatisticItem(id, title);
                                if (getFragmentManager() != null) {
                                    renameStatisticItem.show(getFragmentManager(), "renameStatisticItem");
                                    renameStatisticItem.setOnApplyButtonClickListener(new RenameStatisticItem.ApplyButtonClickListener() {
                                        @Override
                                        public void onApplyButtonClicked() {
                                            updateStatisticList(trainingDbHelper.getTrainingDbList());
                                        }
                                    });
                                }
                                return true;
                            case R.id.delete_item:
                                    StatisticItemDeleteConfirmation statisticItemDeleteConfirmation = new StatisticItemDeleteConfirmation(id, title);
                                if (getFragmentManager() != null) {
                                    statisticItemDeleteConfirmation.show(getFragmentManager(), "statisticDeleteDialog");
                                    statisticItemDeleteConfirmation.setOnPositiveButtonClickedListener(new StatisticItemDeleteConfirmation.PositiveButtonClickListener() {
                                        @Override
                                        public void onPositiveButtonClicked() {
                                            updateStatisticList(trainingDbHelper.getTrainingDbList());
                                            updateEntriesNumber();
                                        }
                                    });
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {

                    }
                });
                popupMenu.show();
            }
        });
    }
    private void updateStatisticList(List<StatisticListItem> statisticListItems){
        if(recyclerView != null && statisticDataAdapter != null)
        {
            statisticDataAdapter.changeStatisticList(statisticListItems);
            statisticDataAdapter.notifyDataSetChanged();
        }
        if(statisticListItems.size() == 0)
            noStatisticText.setVisibility(View.VISIBLE);
        else noStatisticText.setVisibility(View.INVISIBLE);

    }
}
