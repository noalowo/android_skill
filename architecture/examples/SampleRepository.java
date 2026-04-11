package com.example.app.data.repository;

import java.util.List;

import io.reactivex.Observable;

/**
 * MVP Model 層 — Repository Pattern 範本
 *
 * Repository 負責：
 *   1. 決定資料來源（API / 本地快取 / SharedPreferences）
 *   2. 將 API Response 轉換為 Domain Model（若有需要）
 *   3. 提供統一的資料存取介面給 Presenter
 *
 * Presenter 只依賴 Repository 介面，不直接操作 Retrofit / Room 等具體實作，
 * 使得 Presenter 可用 Mockito Mock Repository 來做純 JUnit 測試。
 *
 * 目錄結構建議：
 *   data/
 *   ├── repository/
 *   │   ├── SampleRepository.java          ← 介面
 *   │   └── SampleRepositoryImpl.java      ← 實作
 *   ├── remote/
 *   │   └── ApiService.java                ← Retrofit 介面
 *   └── model/
 *       ├── request/                        ← API Request 類別
 *       └── response/                       ← API Response 類別
 */

// ─── Repository 介面 ──────────────────────────────────────────────────

public interface SampleRepository {

    /**
     * 取得列表資料
     * @return Observable 串流，由實作決定是從 API 或本地快取取得
     */
    Observable<List<SampleItem>> fetchItems();

    /**
     * 依 ID 取得單筆資料
     * @param id 資料 ID
     */
    Observable<SampleItem> fetchItemById(String id);
}

// ─── Repository 實作 ──────────────────────────────────────────────────
//（實際專案中應為獨立檔案 SampleRepositoryImpl.java）

/*
public class SampleRepositoryImpl implements SampleRepository {

    private final ApiService apiService;

    public SampleRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Observable<List<SampleItem>> fetchItems() {
        return apiService.getItems()
                .map(response -> response.getData().getItemList());
    }

    @Override
    public Observable<SampleItem> fetchItemById(String id) {
        return apiService.getItemById(id)
                .map(response -> response.getData());
    }
}
*/

// ─── 擴充：帶本地快取的 Repository ──────────────────────────────────────
//（先嘗試從快取讀取，失敗則呼叫 API 並更新快取）

/*
public class SampleRepositoryImpl implements SampleRepository {

    private final ApiService apiService;
    private final SampleDao sampleDao;       // Room DAO

    public SampleRepositoryImpl(ApiService apiService, SampleDao sampleDao) {
        this.apiService = apiService;
        this.sampleDao = sampleDao;
    }

    @Override
    public Observable<List<SampleItem>> fetchItems() {
        // 先讀本地快取，再從 API 更新
        return Observable.concat(
                sampleDao.getAll().toObservable(),
                apiService.getItems()
                        .map(response -> response.getData().getItemList())
                        .doOnNext(items -> sampleDao.insertAll(items))
        ).firstElement().toObservable();
    }
}
*/
