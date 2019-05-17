package com.example.leeyoubin.knockknock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    Button face;
    Button recog;
    ImageView iv;

    private Uri photoUri;
    private String filePath;
    String imgFile;

    String currentPhotoPath;
    String fileName;//이미지 이름
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    Intent data;
    private String imageFilePath;
    Intent intent;
    private int REQUEST_TEST = 1;
    TextView emotion;

    final String clientId = "03fyTLwrHV5l0ABjQaCW";//애플리케이션 클라이언트 아이디값";
    final String clientSecret = "W134QS_Tw9";//애플리케이션 클라이언트 시크릿값";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button face = findViewById(R.id.face);
        Button recog = findViewById(R.id.recog);
        ImageView iv = findViewById(R.id.iv);


        StringBuffer reqStr = new StringBuffer();
        final TextView emotion = findViewById(R.id.emotion);


        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTakePhotoIntent();
            }

        });
        recog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    String paramName = "image"; // 파라미터명은 image로 지정
                    String imgFile = currentPhotoPath;
                    File uploadFile = new File(imgFile);
                    String apiURL = "https://openapi.naver.com/v1/vision/face"; // 얼굴 감지
                    URL url = new URL(apiURL);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    // multipart request
                    String boundary = "---" + System.currentTimeMillis() + "---";
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    con.setRequestProperty("X-Naver-Client-Id", clientId);
                    con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                    OutputStream outputStream = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                    String LINE_FEED = "\r\n";
                    // file 추가
                    String fileName = uploadFile.getName();
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
                    writer.append("Content-Type: "  + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();
                    FileInputStream inputStream = new FileInputStream(uploadFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    inputStream.close();
                    writer.append(LINE_FEED).flush();
                    writer.append("--" + boundary + "--").append(LINE_FEED);
                    writer.close();
                    BufferedReader br = null;
                    int responseCode = con.getResponseCode();
                    if(responseCode==200) { // 정상 호출
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {  // 에러 발생
                        System.out.println("error!!!!!!! responseCode= " + responseCode);
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    }
                    String inputLine;
                    if(br != null) {
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = br.readLine()) != null) {
                            response.append(inputLine);
                        }
                        br.close();
                        System.out.println(response.toString());
                        emotion.setText(response.toString());
                    } else {
                        System.out.println("error !!!");
                        emotion.setText("error !!!");
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    emotion.setText(e.toString());
                }
            }
        });
    }





    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ((ImageView) findViewById(R.id.iv)).setImageURI(photoUri);
        }
    }

    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp;
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}


