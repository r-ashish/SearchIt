package com.cfd.searchit;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS).build();
    GifImageView gifView;
    ImageView displayImage;
    RecyclerView relatedLinksRecyclerView;
    TextView title, description, loadingMsg;
    LinearLayout loadingLayout, dataLayout;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        initLayout();
        uploadImage();
    }

    private void initLayout(){
        relatedLinksRecyclerView = (RecyclerView) findViewById(R.id.related_links_recycler_view);
        gifView = (GifImageView) findViewById(R.id.gif_view);
        loadingMsg = (TextView) findViewById(R.id.loading_msg);
        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.description);
        dataLayout = (LinearLayout) findViewById(R.id.data_layout);
        loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        displayImage = (ImageView) findViewById(R.id.display_image);
    }

    private void uploadImage(){
        Uri imageUri = getIntent().getData();
        try{
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image = scaleDown(BitmapFactory.decodeStream(imageStream), 800);
            image.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] b = baos.toByteArray();
            String encImage = Base64.encodeToString(b, Base64.DEFAULT);
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
                .url("http://192.168.0.106:7070/api/imgurupload")
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
                    String result = response.body().string();
                    Log.d("upload image", result);
                    processImage(result);
                }
            }
        });
    }

    private void getImageInfo(String json) throws IOException {
        JSONObject data = new JSONObject();
        try{
            data = new JSONObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url("http://192.168.0.106:7070/api/imageinfo?url="+data.optString("link"))
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
                    String result = response.body().string();
                    Log.d("upload image", result);
                    updateUI(result);
                }
            }
        });
    }

    private void processImage(String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingMsg.setText(R.string.searching);
                gifView.setImageResource(R.drawable.search);
                ((LinearLayout.LayoutParams)gifView.getLayoutParams()).height = dp2Px(200);
                ((LinearLayout.LayoutParams)gifView.getLayoutParams()).setMargins(0,0,0,0);
            }
        });
        try{
            getImageInfo(response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateUI(String response){
        JSONObject data;
        try{
            data = new JSONObject(response);
            JSONObject basicInfo = data.getJSONObject("basic_info");
            final JSONArray purchaseInfo = data.getJSONArray("purchase_info");
            final String bestGuess = basicInfo.getString("best_guess");
            final String des = basicInfo.getJSONArray("descriptions").getString(0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingLayout.setVisibility(View.GONE);
                    title.setText(capitalizeString(bestGuess));
                    description.setText(des);
                    description.setMovementMethod(new ScrollingMovementMethod());
                    //            if(des.isEmpty())
//                description.setText("Description not available");
                    displayImage.setImageBitmap(image);
                    gifView.setImageResource(0);
                    dataLayout.setVisibility(View.VISIBLE);
                    setRecyclerViewData(purchaseInfo);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setRecyclerViewData(JSONArray purchaseInfo){
        List<SearchResult> searchResults = new ArrayList<>();
        Gson gson = new Gson();
        try{
            for(int i = 0; i < purchaseInfo.length(); i++){
                searchResults.add(gson.fromJson(purchaseInfo.getJSONObject(i).toString(), SearchResult.class));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        SearchResultsAdapter mAdapter = new SearchResultsAdapter(this, searchResults);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        relatedLinksRecyclerView.setLayoutManager(mLayoutManager);
        relatedLinksRecyclerView.setAdapter(mAdapter);
    }

    public static String capitalizeString(String string) {
        StringBuilder result = new StringBuilder(string.length());
        String words[] = string.split("\\ ");
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return result.toString();
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        if (ratio >= 1.0) return realImage;
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, true);
    }


    private int dp2Px(float dp){
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int)(dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
