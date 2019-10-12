package sphelele.progressupload.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    //private static final String BASE_URL = "";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    public static final String BASE_URL="http://www.something.co.za/";
    private RetrofitClient(String baseURL){
        retrofit=new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(String baseURL){
        if(instance==null){
            instance= new RetrofitClient(baseURL);
        }

        return instance;
    }

    public Api getApi(){

        return retrofit.create(Api.class);
    }
}
