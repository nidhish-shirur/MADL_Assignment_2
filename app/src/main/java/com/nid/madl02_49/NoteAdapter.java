package com.nid.madl02_49;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private Cursor cursor;

    public NoteAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // 1. Grab data from the database row (including the ID for Edit/Delete)
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

        // 2. Put data into the UI fields
        holder.tvTitle.setText(title);
        holder.tvDesc.setText(desc);
        holder.tvDate.setText(date);
        holder.tvPriority.setText(priority);

        // 3. Load the Image safely
        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("camera_thumbnail_bitmap")) {
            holder.ivNote.setImageURI(android.net.Uri.parse(imagePath));
        } else {
            // Fallback placeholder if no image was selected
            holder.ivNote.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 4. Edit Button Logic
        holder.btnEdit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, MainActivity.class);
            // Pass the data to MainActivity so it can populate the text boxes
            intent.putExtra("NOTE_ID", id);
            intent.putExtra("TITLE", title);
            intent.putExtra("DESC", desc);
            intent.putExtra("IMAGE", imagePath);
            intent.putExtra("PRIORITY", priority);
            context.startActivity(intent);
        });

        // 5. Delete Button Logic
        holder.btnDelete.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(context);
            db.deleteNote(id);

            android.widget.Toast.makeText(context, "Note Deleted", android.widget.Toast.LENGTH_SHORT).show();

            // Tell the ViewNotesActivity to refresh the list
            if (context instanceof ViewNotesActivity) {
                ((ViewNotesActivity) context).refreshNotes();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    // This nested class connects the UI IDs to the adapter
    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDate, tvPriority;
        ImageView ivNote;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvDesc = itemView.findViewById(R.id.tvNoteDesc);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            tvPriority = itemView.findViewById(R.id.tvNotePriority);
            ivNote = itemView.findViewById(R.id.imgNote);

            // Link the buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}