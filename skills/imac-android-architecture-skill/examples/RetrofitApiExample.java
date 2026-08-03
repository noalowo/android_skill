package com.example.app.utils.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit ApiService 範本
 * 展示各種 HTTP 方法的正確寫法
 */
public interface ApiServiceExample {

    // 標準 POST（帶 body）
    @POST("/api/resource")
    Observable<Object> createResource(@Body Object request);

    // 標準 GET（帶 query 參數）
    @GET("/api/resource")
    Call<Object> getResource(@Query("id") String id);

    // GET 帶 body（須用 @HTTP）
    @HTTP(method = "GET", path = "/api/resource", hasBody = true)
    Call<Object> getResourceWithBody(@Body Object request);

    // DELETE 帶 body（須用 @HTTP）
    @HTTP(method = "DELETE", path = "/api/resource", hasBody = true)
    Call<Object> deleteResource(@Body Object request);

    // Path 參數
    @GET("/api/resource/{id}")
    Call<Object> getById(@Path("id") int id);
}
