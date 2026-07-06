# Retrofit2 常用註解參考

## HTTP 方法註解

| 註解 | 說明 |
|------|------|
| `@GET("path")` | GET 請求（不支援 body） |
| `@POST("path")` | POST 請求 |
| `@PUT("path")` | PUT 請求 |
| `@DELETE("path")` | DELETE 請求（不支援 body） |
| `@PATCH("path")` | PATCH 請求 |
| `@HTTP(method, path, hasBody)` | 自訂方法，可強制帶 body |

## GET/DELETE 帶 Body 的寫法

Retrofit 的 `@GET` 和 `@DELETE` 預設不允許 request body。若 API 設計需要帶 body，須改用 `@HTTP`：

```java
// 錯誤：@GET 不支援 @Body
@GET("/api/resource")
Call<Response> getResource(@Body Request request); // 編譯錯誤

// 正確：改用 @HTTP
@HTTP(method = "GET", path = "/api/resource", hasBody = true)
Call<Response> getResource(@Body Request request);

@HTTP(method = "DELETE", path = "/api/resource", hasBody = true)
Call<Response> deleteResource(@Body Request request);
```

## 參數註解

| 註解 | 說明 | 範例 |
|------|------|------|
| `@Body` | 將物件序列化為 request body | `@Body Request req` |
| `@Query("key")` | URL query 參數 | `@Query("name") String name` |
| `@Path("key")` | URL path 參數 | `@Path("id") int id` |
| `@Header("key")` | 自訂 header | `@Header("Token") String token` |
| `@Field("key")` | form-urlencoded 欄位（需搭配 `@FormUrlEncoded`） | `@Field("username") String user` |
| `@Part` | multipart 欄位（需搭配 `@Multipart`） | `@Part MultipartBody.Part file` |

## 回傳型別

| 型別 | 說明 |
|------|------|
| `Call<T>` | 同步/非同步回呼 |
| `Observable<T>` | RxJava2 串流（需 `adapter-rxjava2`） |
| `Single<T>` | RxJava2 單次回傳 |
| `Completable` | RxJava2 無回傳值 |
