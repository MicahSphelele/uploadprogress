package sphelele.progressupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sphelele.progressupload.retrofit.ProgressRequestBody;
import sphelele.progressupload.retrofit.RetrofitClient;

/**By : Sphelele Micah Ngubane 2019/10/12 11:10 am**/
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ProgressRequestBody.UploadCallBack {
    
    private static final String TAG = "@MainActivity";
    private ImageView imageView;
    private Button btn_upload;
    private Uri selectedPath;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btn_upload=findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(this);
        btn_upload.setVisibility(View.INVISIBLE);
        (findViewById(R.id.btn_select)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_select){
            ImagePicker.create(this) // Activity or Fragment
                    .theme(R.style.AppTheme)
                    .folderMode(true)
                    .toolbarFolderTitle("App Name")
                    .limit(1)
                    .imageDirectory("ImagePicker")
                    .start();
            return;
        }

        if(v==btn_upload){
            performImageUpload();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Saving system memory by destroying the unused objects
        destroyViewObject(imageView);
        destroyViewObject(btn_upload);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(ImagePicker.shouldHandle(requestCode, resultCode, data)){
            if(resultCode==RESULT_OK){
                Log.d(TAG, "RESULT_OK");

                //if you want to accept multiple images
                //List<Image> images = ImagePicker.getImages(data);

                //If you want to accept a single image
                Image image = ImagePicker.getFirstImageOrNull(data);

                //Load selected image in image view
                Glide.with(MainActivity.this)
                        .load(new File(image.getPath()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .apply(new RequestOptions()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)).into(imageView);


                btn_upload.setVisibility(View.VISIBLE);
                selectedPath = Uri.parse(image.getPath());

                Log.d(TAG, "IMAGE PATH : " + image.getPath());

            }else{
                Toast.makeText(MainActivity.this,"Image selection cancelled" , Toast.LENGTH_SHORT).show();
            }

    }

    }

    @Override
    public void onProgressUpdate(int percent) {
        progressDialog.setProgress(percent);
    }

    @Override
    public void onUploadError(Throwable t) {
        Toast.makeText(this,"Encountered an error on upload",Toast.LENGTH_LONG).show();
    }

    private void destroyViewObject(View view){
        if(view!=null){
            //noinspection UnusedAssignment
            view=null;

        }
    }

    private void performImageUpload(){
        if(selectedPath!=null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Uploading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();

            File file = new File(String.valueOf(selectedPath));
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(file, this);

            MultipartBody.Part file_body = MultipartBody.Part.createFormData("uploaded_file",file.getName(),progressRequestBody);

            //Performing image on the background thread
            new Thread(() ->
                    RetrofitClient.getInstance(RetrofitClient.BASE_URL).getApi().uploadFile(file_body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    progressDialog.dismiss();
                                    Log.d(TAG, "onResponse Retrofit : " + response.body());
                                    if(response.body()!=null){
                                        switch (response.body()) {
                                            case "0":
                                                Toast.makeText(MainActivity.this, "Missing parameters.", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "2":
                                                Toast.makeText(MainActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                                                break;
                                            case "3":
                                                Toast.makeText(MainActivity.this, "File upload failed.", Toast.LENGTH_SHORT).show();
                                                break;
                                            default:


                                                Toast.makeText(MainActivity.this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    progressDialog.dismiss();
                                    Log.d(TAG,"onFailure Retrofit : " + t.getMessage());
                                    Toast.makeText(MainActivity.this, "Unable to Uploaded file", Toast.LENGTH_SHORT).show();
                                }
                            })).start();

        }
    }
}
