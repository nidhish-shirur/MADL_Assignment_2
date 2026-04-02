package com.nid.madl02_49;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        // Setup Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // We moved the initial loading into refreshNotes() to keep things clean!
        refreshNotes();
    }

    // This is the magic method! It runs automatically every time this screen becomes visible again
    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes(); // Reload the data so any edited notes show up instantly
    }

    // Method to reload the database into the RecyclerView
    public void refreshNotes() {
        Cursor newCursor = dbHelper.getAllNotes();
        NoteAdapter newAdapter = new NoteAdapter(this, newCursor);
        recyclerView.setAdapter(newAdapter);
    }
}