package com.example.qr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.mlkit.vision.barcode.common.Barcode;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    TextView txtResults;
    ImageView mImageView;
    Bitmap mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);

        txtResults = findViewById(R.id.txtresults);
        mImageView = findViewById(R.id.image_view);
    }

    public void abrirGaleria(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }

    public void abrirCamara(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == REQUEST_CAMERA)
                    mSelectedImage = (Bitmap) data.getExtras().get("data");
                else
                    mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                mImageView.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void OCRfx(View v) {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    StringBuilder resultados = new StringBuilder();
                    for (Text.TextBlock block : text.getTextBlocks()) {
                        resultados.append(block.getText()).append("\n");
                    }
                    txtResults.setText(resultados.length() > 0 ? resultados.toString() : "No hay Texto");
                })
                .addOnFailureListener(e -> txtResults.setText("Error al procesar imagen"));
    }

    public void detectarCodigos(View v) {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    StringBuilder resultados = new StringBuilder();
                    for (Barcode barcode : barcodes) {
                        resultados.append("Código detectado: ").append(barcode.getRawValue()).append("\n");
                    }
                    txtResults.setText(resultados.length() > 0 ? resultados.toString() : "No se detectaron códigos");
                })
                .addOnFailureListener(e -> txtResults.setText("Error al procesar imagen"));
    }

    public void Rostrosfx(View v) {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    Bitmap bitmap = mSelectedImage.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(5);
                    paint.setStyle(Paint.Style.STROKE);
                    for (Face rostro : faces) {
                        canvas.drawRect(rostro.getBoundingBox(), paint);
                    }
                    mImageView.setImageBitmap(bitmap);
                    txtResults.setText(faces.size() > 0 ? "Hay " + faces.size() + " rostro(s)" : "No Hay rostros");
                })
                .addOnFailureListener(e -> txtResults.setText("Error al procesar imagen"));
    }
}