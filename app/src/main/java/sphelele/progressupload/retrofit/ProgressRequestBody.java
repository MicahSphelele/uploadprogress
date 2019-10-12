package sphelele.progressupload.retrofit;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {


    private File file;
    private UploadCallBack listener;
    private static final int DEFALUT_BUFFER_SIZE = 4096;


    public ProgressRequestBody(File file, UploadCallBack listener) {
        this.file = file;
        this.listener = listener;
    }


    @Override
    public MediaType contentType() {
        return MediaType.parse("image/*");
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength=file.length();

        byte [] buffer = new byte[DEFALUT_BUFFER_SIZE];

        FileInputStream in = new FileInputStream(file);
        long uploaded = 0;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1){

                handler.post(new ProgressUpdater(uploaded,fileLength));
                uploaded+=read;
                sink.write(buffer,0,read);
            }
        }catch (Exception e){
            e.printStackTrace();
            listener.onUploadError(e);
        }finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {

        private long uploaded;
        private long fileLength;



        ProgressUpdater(long uploaded, long fileLength) {
            this.uploaded=uploaded;
            this.fileLength=fileLength;
        }


        @Override
        public void run() {
            listener.onProgressUpdate((int)(100*uploaded/fileLength) );
        }
    }

    public interface UploadCallBack {

        void  onProgressUpdate(int percent);
        void  onUploadError(Throwable t);
    }
}
