package com.example.pw_8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button loadImageButton;
    private Button button1;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        loadImageButton = findViewById(R.id.loadImageButton);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        button1.setOnClickListener(v -> startSequentialWork());
        button2.setOnClickListener(v -> startParallelWork());

        loadImageButton.setOnClickListener(v -> new DownloadImageTask().execute("https://random.dog/woof.json"));
    }

    private void startSequentialWork() {
        // Создаем рабочий запрос для первой задачи
        OneTimeWorkRequest workRequest1 = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        // Создаем рабочий запрос для второй задачи
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        // Создаем рабочий запрос для третьей задачи
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest.Builder(TaskWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        // Устанавливаем зависимости между задачами, чтобы они выполнялись последовательно
        WorkManager.getInstance(this)
                .beginWith(workRequest1)
                .then(workRequest2)
                .then(workRequest3)
                .enqueue();

        Toast.makeText(this, "Задачи поставлены в очередь последовательно", Toast.LENGTH_SHORT).show();
    }

    private void startParallelWork() {
        // Создаем рабочий запрос для первой параллельной задачи
        OneTimeWorkRequest parallelWorkRequest1 = new OneTimeWorkRequest.Builder(ParallelTaskWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        // Создаем рабочий запрос для второй параллельной задачи
        OneTimeWorkRequest parallelWorkRequest2 = new OneTimeWorkRequest.Builder(ParallelTaskWorker.class)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .build();

        // Ставим обе параллельные задачи в очередь
        WorkManager.getInstance(this)
                .beginWith(Arrays.asList(parallelWorkRequest1, parallelWorkRequest2))
                .enqueue();

        Toast.makeText(this, "Параллельные задачи поставлены в очередь", Toast.LENGTH_SHORT).show();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    String jsonResponse = convertInputStreamToString(inputStream);
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    String imageUrl = jsonObject.getString("url");
                    bitmap = downloadBitmap(imageUrl);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, bytesRead));
            }
            return stringBuilder.toString();
        }

        private Bitmap downloadBitmap(String imageUrl) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
            return null;
        }
    }
}