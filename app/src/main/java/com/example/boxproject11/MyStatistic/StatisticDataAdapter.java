package com.example.boxproject11.MyStatistic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boxproject11.R;

import java.util.List;

public class StatisticDataAdapter extends RecyclerView.Adapter<StatisticDataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<StatisticListItem> statisticListItems;

    public StatisticDataAdapter(Context context, List<StatisticListItem> statisticListItems) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.statisticListItems = statisticListItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.statistic_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatisticListItem statisticListItem = statisticListItems.get(position);
        holder.title.setText(statisticListItem.getTitle());
        holder.date.setText(statisticListItem.getDate());
        holder.id = statisticListItem.getId();
    }

    @Override
    public int getItemCount() {
        return statisticListItems.size();
    }

    public void changeStatisticList(List<StatisticListItem> statisticListItems){
        this.statisticListItems = statisticListItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        private final String TAG = "statisticTest";
        private int id;
        private final TextView title, date;
        private final CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            title = itemView.findViewById(R.id.title_text);
            date = itemView.findViewById(R.id.date_text);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(statisticItemClickListener != null)
                        statisticItemClickListener.onClick(getAdapterPosition(), view, title.getText().toString(), id);
                }
            });
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(statisticItemClickListener != null)
                        statisticItemClickListener.onLongClick(getAdapterPosition(), view, title.getText().toString(), id);
                    return true;
                }
            });
        }
    }
    private static StatisticItemClickListener statisticItemClickListener;
    public interface StatisticItemClickListener{
        void onClick(int position, View view, String title, int id);
        void onLongClick(int position, View view, String title, int id);
    }
    public void setOnStatisticItemClickListener(StatisticItemClickListener listener)
    {
        statisticItemClickListener = listener;
    }
}
