package com.cfd.asmi;

import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rashish on 23/01/18.
 */

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder> {

    private List<SearchResult> searchResults;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title, link, description;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            link = (TextView) view.findViewById(R.id.link);
            description = (TextView) view.findViewById(R.id.description);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(searchResults.get(getAdapterPosition()).link));
        }
    }

    Context context;

    public SearchResultsAdapter(Context context, List<SearchResult> teacherInfoList) {
        this.searchResults= teacherInfoList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.related_links_template, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SearchResult searchResult = searchResults.get(position);
        holder.title.setText(searchResult.title);
        holder.description.setText(searchResult.description);
        holder.link.setText(searchResult.link);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
}