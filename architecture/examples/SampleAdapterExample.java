package com.example.app.ui.sample;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.databinding.ItemSampleBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView Adapter 範本
 *
 * 重點：
 *   1. ViewHolder 使用 ViewBinding（ItemSampleBinding），不使用 findViewById
 *   2. 更新資料使用 DiffUtil（避免 notifyDataSetChanged）
 *   3. 點擊事件透過 OnItemClickListener 介面回傳，由 Activity/Fragment 轉交 Presenter
 *   4. Adapter 不持有 Context，透過 parent.getContext() 取得
 *
 * Layout 對應：item_sample.xml
 *   需包含 tv_title、tv_description 等 View ID
 */
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {

    // ─── 點擊事件介面 ─────────────────────────────────────────────────────

    public interface OnItemClickListener {
        void onItemClick(SampleItem item, int position);
    }

    // ─── 欄位 ─────────────────────────────────────────────────────────────

    private final List<SampleItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    // ─── 公開方法 ─────────────────────────────────────────────────────────

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 使用 DiffUtil 計算差異後更新列表，避免 notifyDataSetChanged() 造成全部重繪。
     * DiffUtil 會自動判斷新增、移除、移動的項目並觸發對應的動畫。
     */
    public void updateItems(List<SampleItem> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                // 以唯一 ID 判斷是否為同一筆資料
                return Objects.equals(
                        items.get(oldPos).getId(),
                        newItems.get(newPos).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                // 以 equals 判斷內容是否相同
                return Objects.equals(items.get(oldPos), newItems.get(newPos));
            }
        });

        items.clear();
        items.addAll(newItems);
        result.dispatchUpdatesTo(this);
    }

    // ─── RecyclerView.Adapter ─────────────────────────────────────────────

    @NonNull
    @Override
    public SampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSampleBinding binding = ItemSampleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SampleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────

    class SampleViewHolder extends RecyclerView.ViewHolder {

        private final ItemSampleBinding binding;

        SampleViewHolder(ItemSampleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SampleItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvDescription.setText(item.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(items.get(pos), pos);
                    }
                }
            });
        }
    }
}
