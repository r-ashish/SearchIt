package com.cfd.asmi;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class SearchResultActivity extends AppCompatActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    GifImageView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        gifView = (GifImageView) findViewById(R.id.gif_view);
        uploadImage();
    }

    private void uploadImage(){
        Uri imageUri = getIntent().getData();
        try{
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapFactory.decodeStream(imageStream).compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] b = baos.toByteArray();
            String encImage = Base64.encodeToString(b, Base64.DEFAULT);
            Log.d("b64 image", encImage);
            postImage(encImage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void postImage(String image) throws IOException {
        JSONObject data = new JSONObject();
        try{
            data.put("b64File", image);
        }catch (Exception e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, data.toString());
        Request request = new Request.Builder()
                .url("http://cb83a86f.ngrok.io/api/imgurupload")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    Log.d("upload image", response.toString());
                    getImageInfo(response);
                }
            }
        });
    }

    private void getImageInfo(Response response){
        ((LinearLayout.LayoutParams)gifView.getLayoutParams()).height = dp2Px(200);
        ((LinearLayout.LayoutParams)gifView.getLayoutParams()).setMargins(0,0,0,0);

    }

    private int dp2Px(float dp){
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int)(dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
