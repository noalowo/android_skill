# Android View ID 前綴命名規範

## XML ID (snake_case) 與 Java 變數 (camelCase) 對照

| XML ID | Java 變數 | 元件類型 |
|--------|-----------|----------|
| `iv_photo` | `ivPhoto` | ImageView |
| `tv_title` | `tvTitle` | TextView |
| `btn_submit` | `btnSubmit` | Button |
| `cv_card` | `cvCard` | CardView |
| `rv_list` | `rvList` | RecyclerView |
| `ll_container` | `llContainer` | LinearLayout |
| `cl_root` | `clRoot` | ConstraintLayout |
| `sv_scroll` | `svScroll` | ScrollView |
| `et_input` | `etInput` | EditText |
| `pv_preview` | `pvPreview` | PreviewView |
| `gl_grid` | `glGrid` | GridLayout |
| `fl_frame` | `flFrame` | FrameLayout |
| `sw_toggle` | `swToggle` | Switch |
| `cb_check` | `cbCheck` | CheckBox |
| `rb_option` | `rbOption` | RadioButton |
| `sb_seek` | `sbSeek` | SeekBar |
| `pb_progress` | `pbProgress` | ProgressBar |
| `sp_spinner` | `spSpinner` | Spinner |
| `wv_web` | `wvWeb` | WebView |
| `vp_pager` | `vpPager` | ViewPager |
| `tl_tabs` | `tlTabs` | TabLayout |
| `fab_action` | `fabAction` | FloatingActionButton |

## 規則

1. XML ID 一律 `snake_case`，以元件縮寫為前綴
2. Java 變數一律 `camelCase`，保留前綴
3. 陣列型態的 View 後綴加數字：`iv_photo_1`、`iv_photo_2` -> `ivPhoto1`、`ivPhoto2`
4. 共用按鈕後綴加 `_base`：`btn_home_base`、`btn_app_base`
