package ru.arvalon.gallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.arvalon.gallery.R;
import ru.arvalon.gallery.model.ListItem;

/** RecyclerView адаптер для списка файлов */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesHolder> {

    private Context ctx;
    private List<ListItem> listItems;

    public FilesAdapter(Context context, List<ListItem> listItems) {
        ctx=context;
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public FilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file,parent,false);

        return new FilesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesHolder holder, int position) {

        ListItem listItem = listItems.get(position);

        holder.name.setText(listItem.getName());

        String type = listItem.isDir() ? ctx.getString(R.string.dir_label) : listItem.getMediaType();

        holder.type.setText(type);

        holder.itemView.setTag(listItem);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    /** холдер для разметки */
    static class FilesHolder extends RecyclerView.ViewHolder {

        TextView name, type;

        public FilesHolder(View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.tv_filename);
            type =itemView.findViewById(R.id.tv_filetype);
        }
    }

    public void setData(List<ListItem> newListItems){
        //listItems=newListItems;
        listItems.addAll(newListItems);
        notifyDataSetChanged();
    }
}
