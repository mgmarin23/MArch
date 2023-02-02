package dte.mgmprojects.march;

import com.google.gson.JsonObject;

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
    Call<JsonObject> getUserToken (@Body JsonObject user);

    @Headers({"Accept application/json"})
    @GET("plugins/telemetry/DEVICE/bf185470-9da9-11ed-8d2b-073de4e14907/values/timeseries?keys=db,tank")
    Call<JsonObject> getLastestTel (@Header("X-Authorization")String token, @Path("device_id") String device_id);

    @Headers ({"Accept text/plain", "Content-Type: application/json"})
    @POST("v1/6xSlEailzRAtTYRD4eEh/telemetry")
    Call<Void> sendTel (@Body JsonObject tele, @Path ("device_access_token") String device_access_token);

    @Headers ({"Accept application/json"})
    @GET ("v1/6xSlEailzRAtTYRD4eEh/attributes?sharedKeys=mode")
    Call<JsonObject> getMode ( @Path ("device_access_token") String device_access_token);

}
