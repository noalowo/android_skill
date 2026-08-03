package com.example.app.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * MVP Base Fragment
 * 封裝 ViewBinding、Presenter 生命週期綁定等通用邏輯。
 *
 * 與 BaseActivity 的關鍵差異：
 *   - binding 在 onDestroyView() 設為 null（非 onDestroy），
 *     因為 Fragment 可能在 View 銷毀後仍存活（如 back stack）。
 *   - inflateBinding 需要額外的 container 參數。
 *
 * 生命週期對應：
 *   onCreateView   → inflate ViewBinding
 *   onViewCreated  → createPresenter() + initViews()
 *   onResume       → presenter.onViewAttached(this)
 *   onPause        → presenter.onViewDetached()
 *   onDestroyView  → binding = null
 *   onDestroy      → presenter.onDestroy()
 *
 * 用法：
 *   public class SampleFragment
 *           extends BaseFragment<FragmentSampleBinding, SamplePresenter>
 *           implements SampleContract.View { ... }
 *
 * @param <VB> ViewBinding 類型
 * @param <P>  Presenter 類型
 */
public abstract class BaseFragment<VB extends ViewBinding, P extends BaseContract.Presenter>
        extends Fragment implements BaseContract.View {

    protected VB binding;
    protected P presenter;

    // ─── 子類必須實作 ─────────────────────────────────────────────────────

    /** inflate ViewBinding，例如 FragmentSampleBinding.inflate(inflater, container, false) */
    protected abstract VB inflateBinding(LayoutInflater inflater, ViewGroup container);

    /** 建立 Presenter 實例 */
    protected abstract P createPresenter();

    /** 初始化 View 元件與事件綁定，此時 binding 和 presenter 皆已就緒 */
    protected abstract void initViews();

    // ─── 生命週期 ─────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = createPresenter();
        initViews();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) presenter.onViewAttached(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPause() {
        super.onPause();
        if (presenter != null) presenter.onViewDetached();
    }

    /**
     * Fragment 的 View 被銷毀時呼叫。
     * 必須在此將 binding 設為 null，而非 onDestroy()，
     * 因為 Fragment 實例可能仍在 back stack 中存活。
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        if (presenter != null) presenter.onDestroy();
        super.onDestroy();
    }

    // ─── BaseContract.View 預設實作 ────────────────────────────────────────

    @Override
    public void showError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void showError(ErrorType type, String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                switch (type) {
                    case NETWORK:
                        Toast.makeText(getActivity(), "網路連線異常：" + message,
                                Toast.LENGTH_LONG).show();
                        break;
                    case TIMEOUT:
                        Toast.makeText(getActivity(), "連線逾時，請稍後再試",
                                Toast.LENGTH_LONG).show();
                        break;
                    case SERVER:
                        Toast.makeText(getActivity(), "伺服器錯誤：" + message,
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), message,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        // 子類覆寫以顯示自訂的 Loading UI
    }

    @Override
    public void finishActivity() {
        if (getActivity() != null) getActivity().finish();
    }
}
