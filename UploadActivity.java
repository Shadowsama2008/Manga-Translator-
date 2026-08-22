package com.example.manga_translator;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadActivity extends AppCompatActivity {
    private static final int PICK_IMAGES = 1;
    private List<Uri> selectedImages = new ArrayList<>();
    private LinearLayout imagesContainer;
    private DatabaseHelper db;
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        db = new DatabaseHelper(this);
        imagesContainer = findViewById(R.id.imagesContainer);
        Button pickBtn = findViewById(R.id.pickImagesBtn);
        Button uploadBtn = findViewById(R.id.uploadBtn);
        EditText nameInput = findViewById(R.id.projectNameInput);
        Spinner langSpinner = findViewById(R.id.langSpinner);

        pickBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICK_IMAGES);
        });

        uploadBtn.setOnClickListener(v -> {
            projectName = nameInput.getText().toString().trim();
            if (projectName.isEmpty()) {
                Toast.makeText(this, "أدخل اسم المشروع", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImages.isEmpty()) {
                Toast.makeText(this, "اختر صوراً أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            String lang = langSpinner.getSelectedItem().toString();
            uploadProject(lang);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            selectedImages.clear();
            imagesContainer.removeAllViews();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(uri);
                    addImagePreview(uri);
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                selectedImages.add(uri);
                addImagePreview(uri);
            }
        }
    }

    private void addImagePreview(Uri uri) {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(uri).into(iv);
        imagesContainer.addView(iv);
    }

    private void uploadProject(String lang) {
        // حفظ الصور في مجلد التطبيق
        String projectFolder = getFilesDir() + "/projects/" + System.currentTimeMillis() + "/";
        new File(projectFolder).mkdirs();

        long projectId = db.insertProject(projectName, lang, selectedImages.size());

        for (int i = 0; i < selectedImages.size(); i++) {
            Uri uri = selectedImages.get(i);
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                String imagePath = projectFolder + "page_" + (i + 1) + ".jpg";
                FileOutputStream fos = new FileOutputStream(imagePath);
                fos.write(buffer);
                fos.close();

                // استخراج النص
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                String rawText = OCRProcessor.extractText(bitmap);

                // ترجمة النص (سيتم عبر الخدمة)
                String translated = rawText; // سيتم ترجمته لاحقاً

                db.insertPage(projectId, i + 1, imagePath, rawText, translated);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // بدء الترجمة في الخلفية
        Intent serviceIntent = new Intent(this, TranslatorService.class);
        startService(serviceIntent);

        Toast.makeText(this, "تم رفع المشروع! جاري الترجمة...", Toast.LENGTH_LONG).show();
        finish();
    }
}