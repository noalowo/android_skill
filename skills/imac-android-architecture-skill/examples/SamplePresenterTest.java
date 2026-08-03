package com.example.app.ui.sample;

import com.example.app.data.repository.SampleRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * MVP Presenter 單元測試範本
 *
 * 測試原則：
 *   1. Presenter 不依賴 Android SDK → 可用純 JUnit + Mockito 測試
 *   2. Mock View 和 Repository，驗證 Presenter 在各種情境下的行為
 *   3. 使用 TrampolineSchedulerRule 讓 RxJava 同步執行
 *
 * 測試目錄：app/src/test/java/（非 androidTest）
 *
 * build.gradle 依賴：
 *   testImplementation 'junit:junit:4.13.2'
 *   testImplementation 'org.mockito:mockito-core:5.11.0'
 *   testImplementation 'io.reactivex.rxjava2:rxjava:2.2.21'
 *   testImplementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
 */
@RunWith(MockitoJUnitRunner.class)
public class SamplePresenterTest {

    /**
     * 讓 RxJava 的 io() / mainThread() 等 Scheduler 在測試中同步執行。
     * 若不設定，subscribeOn(Schedulers.io()) 會在背景執行緒跑，
     * 導致 verify() 時 callback 尚未觸發而測試失敗。
     */
    @Rule
    public TrampolineSchedulerRule schedulerRule = new TrampolineSchedulerRule();

    @Mock
    SampleContract.View mockView;

    @Mock
    SampleRepository mockRepository;

    private SamplePresenter presenter;

    @Before
    public void setup() {
        // Presenter 只接收 Repository，不接收 Context
        presenter = new SamplePresenter(mockRepository);
        presenter.onViewAttached(mockView);
    }

    // ─── 成功情境 ──────────────────────────────────────────────────────────

    @Test
    public void loadData_success_showsDataAndHidesLoading() {
        // Given
        List<SampleItem> items = Arrays.asList(
                new SampleItem("1", "Item 1"),
                new SampleItem("2", "Item 2")
        );
        when(mockRepository.fetchItems()).thenReturn(Observable.just(items));

        // When
        presenter.loadData();

        // Then
        verify(mockView).showLoading(true);
        verify(mockView).showLoading(false);
        verify(mockView).onDataLoaded(items);
        verify(mockView, never()).showError(anyString());
    }

    // ─── 失敗情境 ──────────────────────────────────────────────────────────

    @Test
    public void loadData_networkError_showsError() {
        // Given
        when(mockRepository.fetchItems())
                .thenReturn(Observable.error(new IOException("Network error")));

        // When
        presenter.loadData();

        // Then
        verify(mockView).showLoading(true);
        verify(mockView).showLoading(false);
        verify(mockView).showError(anyString());
    }

    // ─── View 已銷毀情境 ──────────────────────────────────────────────────

    @Test
    public void loadData_afterViewDetached_doesNotCrash() {
        // Given
        presenter.onViewDetached();

        // Then — View 已銷毀，不應有任何互動也不應 crash
        verifyNoMoreInteractions(mockView);
    }

    @After
    public void tearDown() {
        presenter.onDestroy();
    }

    // ─── RxJava 測試輔助 Rule ─────────────────────────────────────────────
    //（實際專案中建議獨立為 TrampolineSchedulerRule.java 放在 test 目錄）

    public static class TrampolineSchedulerRule implements TestRule {
        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    // 將所有 Scheduler 替換為同步的 trampoline
                    RxJavaPlugins.setIoSchedulerHandler(s -> Schedulers.trampoline());
                    RxJavaPlugins.setComputationSchedulerHandler(s -> Schedulers.trampoline());
                    RxJavaPlugins.setNewThreadSchedulerHandler(s -> Schedulers.trampoline());
                    RxAndroidPlugins.setInitMainThreadSchedulerHandler(s -> Schedulers.trampoline());
                    try {
                        base.evaluate();
                    } finally {
                        RxJavaPlugins.reset();
                        RxAndroidPlugins.reset();
                    }
                }
            };
        }
    }
}
