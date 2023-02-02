package dte.mgmprojects.march;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {


        private  static final String BASE_URI = "https://srv_iot.diatel.upm.es/api/";
        private static retrofit2.Retrofit.Builder builder = new retrofit2.Retrofit.Builder()
                .baseUrl("")
                .client(new OkHttpClient.Builder().addInterceptor((
                        new HttpLoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY)).build())
                .addConverterFactory(GsonConverterFactory.create());

        public static <S> S createService(Class <S> serviceClass) {
            Retrofit adapter = builder.build();
            return adapter.create(serviceClass);
        }


}
