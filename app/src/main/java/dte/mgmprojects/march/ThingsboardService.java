package dte.mgmprojects.march;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ThingsboardService {
    @Headers ({"Accept application/json", "Content-Type: application/json"})
    @POST ("auth/login")
    Call<JSONObject> getUserToken (@Body JSONObject user);

    @Headers({"Accept application/json"})
    @GET("auth/login")
    Call<JSONObject> getLastestTel (@Header("X-Authorization")String token, @Path("device_id") String device_id);

    @Headers ({"Accept text/plain", "Content-Type: application/json"})
    @POST("v1/{device_access_token}/telemetry")
    Call<Void> sendTel (@Body JSONObject tele, @Path ("device_access_token") String device_access_token);

    @Headers ({"Accept application/json"})
    @GET ("v1/{device_access_token}/attributes?sharedKeys=mode")
    Call<JSONObject> getMode ( @Path ("device_access_token") String device_access_token);

}
