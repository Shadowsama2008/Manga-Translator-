package com.example.manga_translator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProjectActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private int projectId;
    private TextView titleText;
    private RecyclerView pagesRecyclerView;
    private PageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectId = getIntent().getIntExtra("project_id", -1);
        db = new DatabaseHelper(this);

        titleText = findViewById(R.id.projectTitle);
        pagesRecyclerView = findViewById(R.id.pagesRecyclerView);
        pagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button pdfBtn = findViewById(R.id.generatePdfBtn);
        pdfBtn.setOnClickListener(v -> {
            generatePDF();
        });

        loadProject();
    }

    private void loadProject() {
        List<Project> projects = db.getAllProjects();
        for (Project p : projects) {
            if (p.id == projectId) {
                titleText.setText(p.name);
                break;
            }
        }

        List<Page> pages = db.getPagesByProject(projectId);
        adapter = new PageAdapter(pages);
        pagesRecyclerView.setAdapter(adapter);
    }

    private void generatePDF() {
        // سيتم تنفيذ توليد PDF عبر خدمة خلفية
        Intent intent = new Intent(this, PDFGeneratorService.class);
        intent.putExtra("project_id", projectId);
        startService(intent);

        // بعد التوليد، سيتم فتح الـ PDF
        Intent viewerIntent = new Intent(this, PDFViewerActivity.class);
        viewerIntent.putExtra("project_id", projectId);
        startActivity(viewerIntent);
    }

    class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {
        private List<Page> pages;

        PageAdapter(List<Page> pages) { this.pages = pages; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_page, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Page p = pages.get(position);
            holder.pageNumberText.setText("صفحة " + p.pageNumber);
            Glide.with(ProjectActivity.this).load(new File(p.imagePath)).into(holder.pageImage);
            holder.rawText.setText("النص الأصلي: " + p.rawText);
            holder.correctedText.setText(p.correctedText);

            holder.fixBtn.setOnClickListener(v -> {
                String newText = holder.correctedText.getText().toString();
                db.updatePageCorrection(p.id, newText);
                // إعادة تحميل الصفحات
                loadProject();
            });
        }

        @Override
        public int getItemCount() { return pages.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView pageNumberText, rawText;
            EditText correctedText;
            ImageView pageImage;
            Button fixBtn;

            ViewHolder(View itemView) {
                super(itemView);
                pageNumberText = itemView.findViewById(R.id.pageNumber);
                rawText = itemView.findViewById(R.id.rawText);
                correctedText = itemView.findViewById(R.id.correctedText);
                pageImage = itemView.findViewById(R.id.pageImage);
                fixBtn = itemView.findViewById(R.id.fixBtn);
            }
        }
    }
}