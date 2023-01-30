package dte.mgmprojects.march;

import android.telecom.CallScreeningService;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface Retrofit {
   Call<ResponseBody> sendMessage(@Body RequestBody message);
}

