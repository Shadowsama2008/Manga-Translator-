package com.example.manga_translator;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFGeneratorService extends IntentService {
    public PDFGeneratorService() {
        super("PDFGeneratorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int projectId = intent.getIntExtra("project_id", -1);
        DatabaseHelper db = new DatabaseHelper(this);
        List<Page> pages = db.getPagesByProject(projectId);

        try {
            String outputPath = getFilesDir() + "/output/manga_" + projectId + ".pdf";
            new File(getFilesDir() + "/output").mkdirs();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            for (Page p : pages) {
                Bitmap bitmap = BitmapFactory.decodeFile(p.imagePath);
                if (bitmap != null) {
                    Image img = Image.getInstance(p.imagePath);
                    img.scaleToFit(document.getPageSize().getWidth() - 40, document.getPageSize().getHeight() - 40);
                    document.add(img);
                }
            }

            document.close();
            db.updateProjectStatus(projectId, "completed");
            Log.d("PDF", "تم إنشاء PDF بنجاح: " + outputPath);

        } catch (Exception e) {
            Log.e("PDF", "خطأ في توليد PDF", e);
        }
    }
}