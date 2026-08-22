package com.example.manga_translator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

public class OCRProcessor {
    private static final TextRecognizer recognizer = TextRecognition.getClient(
            new JapaneseTextRecognizerOptions.Builder().build()
    );

    public static String extractText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        String[] result = {""};
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    result[0] = visionText.getText();
                })
                .addOnFailureListener(e -> {
                    Log.e("OCR", "فشل استخراج النص", e);
                });
        return result[0];
    }
}