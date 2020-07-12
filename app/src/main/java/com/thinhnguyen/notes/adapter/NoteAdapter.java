package com.thinhnguyen.notes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinhnguyen.notes.R;
import com.thinhnguyen.notes.model.Note;

import java.util.ArrayList;

public class NoteAdapter extends BaseAdapter implements Filterable {
    Context mContext;

    ArrayList<Note> mOriginalNotes;
    ArrayList<Note> mFilteredNotes;

    FilteredItem mFilteredItem = new FilteredItem();

    public NoteAdapter(Context mContext, ArrayList<Note> mNotes) {
        this.mContext = mContext;
        this.mOriginalNotes = mNotes;
        this.mFilteredNotes = mNotes;
    }

    @Override
    public int getCount() {
        return mFilteredNotes.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilteredNotes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.note_item, null);

            viewHolder = new ViewHolder();
            viewHolder.mTitle = convertView.findViewById(R.id.tv_title_note);
            viewHolder.mContent = convertView.findViewById(R.id.tv_content_note);
            viewHolder.mDate = convertView.findViewById(R.id.tv_date_note);
            viewHolder.mAlarm = convertView.findViewById(R.id.img_alarm_black);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Note note = mFilteredNotes.get(position);

        viewHolder.mTitle.setText(note.getTitle());
        viewHolder.mContent.setText(note.getContent());
        viewHolder.mDate.setText(note.getDate());

        if (note.isHasAlarm() == 1)
        {
            viewHolder.mAlarm.setVisibility(View.VISIBLE);
        }
        else
        {
            viewHolder.mAlarm.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder
    {
        TextView mTitle;
        TextView mContent;
        TextView mDate;
        ImageView mAlarm;
    }

    @Override
    public Filter getFilter() {
        return mFilteredItem;
    }

    private class FilteredItem extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterStr = constraint.toString().toLowerCase();
            int count = mOriginalNotes.size();

            FilterResults filterResults = new FilterResults();
            ArrayList<Note> filteredNotes = new ArrayList<>(count);

            final ArrayList<Note> notes = mOriginalNotes;

            Note currentNote;
            for (int i = 0; i < count; i++)
            {
                currentNote = notes.get(i);
                if (currentNote.getTitle().toLowerCase().startsWith(filterStr) ||
                        currentNote.getContent().toLowerCase().startsWith(filterStr))
                {
                    filteredNotes.add(currentNote);
                }
            }

            filterResults.values = filteredNotes;
            filterResults.count = count;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredNotes = (ArrayList<Note>) results.values;
            notifyDataSetChanged();
        }
    }
}
