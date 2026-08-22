package com.example.manga_translator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecyclerView projectsRecyclerView;
    private ProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        projectsRecyclerView = findViewById(R.id.projectsRecyclerView);
        projectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button uploadBtn = findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UploadActivity.class));
        });

        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        List<Project> projects = db.getAllProjects();
        adapter = new ProjectAdapter(projects);
        projectsRecyclerView.setAdapter(adapter);
    }

    class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
        private List<Project> projects;

        ProjectAdapter(List<Project> projects) { this.projects = projects; }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_project, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Project p = projects.get(position);
            holder.nameText.setText(p.name);
            holder.infoText.setText(p.lang + " - " + p.pagesCount + " صفحات - " + p.status);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
                intent.putExtra("project_id", p.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return projects.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, infoText;
            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.projectName);
                infoText = itemView.findViewById(R.id.projectInfo);
            }
        }
    }
}