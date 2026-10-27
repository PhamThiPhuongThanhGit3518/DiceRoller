package com.phuongthanh.diceroller.features.language

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.cyber.ads.onboading.BaseLanguageActivity
import com.cyber.ads.onboading.LanguageAdapter
import com.cyber.ads.onboading.LanguageModel
import com.cyber.ads.remote.NativeHolder
import com.cyber.ads.remote.NativeMultiHolder
import com.phuongthanh.diceroller.MainActivity
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.ads.AdsManager
import com.phuongthanh.diceroller.core.ads.RemoteConfig

class LanguageActivity : BaseLanguageActivity() {

    // 1. Khai báo các holder quảng cáo chân trang
    override var nativeLanguage: NativeMultiHolder = RemoteConfig.NATIVE_LANGUAGE
    override var nativeLanguageRe: NativeMultiHolder? = RemoteConfig.NATIVE_LANGUAGE_RE
    override var nativeSmall: NativeHolder? = RemoteConfig.NATIVE_LANGUAGE_SMALL

    // 2. Preload trước các Native Ads cho màn Intro kế tiếp
    override var nativeIntroFullMulti: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_FULL
    override var nativeIntroFullMultiRe: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_FULL_RE
    override var nativeIntro: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO
    override var nativeIntroRe: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_RE

    override fun onCreate(savedInstanceState: Bundle?) {
        reorderAndEnrichLanguages()
        super.onCreate(savedInstanceState)
        setupCustomUI()
    }

    private fun reorderAndEnrichLanguages() {
        val current = BaseLanguageActivity.languages.toMutableList()

        // Thêm tiếng Trung và tiếng Thái nếu chưa có trong danh sách gốc của thư viện
        if (current.none { it.langCode == "zh" }) {
            current.add(
                LanguageModel(
                    com.cyber.ads.R.drawable.ds_ic_china,
                    "Chinese",
                    "zh",
                    false,
                    "中文",
                    null,
                    false,
                    false,
                    false
                )
            )
        }
        if (current.none { it.langCode == "th" }) {
            current.add(
                LanguageModel(
                    R.drawable.ic_flag_thailand,
                    "Thai",
                    "th",
                    false,
                    "ไทย",
                    null,
                    false,
                    false,
                    false
                )
            )
        }

        // Ưu tiên hiển thị đúng thứ tự theo bản mẫu thiết kế Figma
        val preferredOrder = listOf("en", "vi", "zh", "ja", "ko", "th", "in", "es", "fr", "de", "ru")
        val sorted = current.sortedBy { model ->
            val code = model.langCode.removePrefix("group_")
            val index = preferredOrder.indexOf(code)
            if (index >= 0) index else 999
        }
        BaseLanguageActivity.languages = sorted
    }

    private fun setupCustomUI() {
        val rvLanguage = findViewById<RecyclerView>(R.id.rvLanguage)

        // 1. Tìm kiếm ngôn ngữ (Search language)
        val etSearch = findViewById<EditText>(R.id.etSearchLanguage)
        etSearch?.addTextChangedListener { editable ->
            val query = editable?.toString()?.trim().orEmpty()
            val adapter = rvLanguage?.adapter as? LanguageAdapter ?: return@addTextChangedListener
            val allLanguages = BaseLanguageActivity.languages
            val filtered = if (query.isEmpty()) {
                allLanguages
            } else {
                allLanguages.filter { model: LanguageModel ->
                    model.name.contains(query, ignoreCase = true) ||
                        (model.nativeName?.contains(query, ignoreCase = true) == true)
                }
            }
            adapter.setLanguages(filtered)
        }

        // 2. Lắng nghe thay đổi danh sách để cập nhật Thẻ "You Selected"
        val adapter = rvLanguage?.adapter as? LanguageAdapter
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateYouSelected()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                updateYouSelected()
            }
        })

        // Lắng nghe thao tác chọn trên RecyclerView
        rvLanguage?.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_UP) {
                    rv.postDelayed({ updateYouSelected() }, 100)
                }
                return false
            }
        })

        // 3. Khởi tạo lựa chọn mặc định
        if (fromSplash) {
            rvLanguage?.post {
                val currentAdapter = rvLanguage.adapter as? LanguageAdapter
                currentAdapter?.selectLanguage("en")
                updateYouSelected()
            }
        } else {
            updateYouSelected()
        }

        // 4. Click thẻ "You Selected" để scroll về đầu
        findViewById<View>(R.id.layoutYouSelected)?.setOnClickListener {
            rvLanguage?.smoothScrollToPosition(0)
        }

        // 5. Nút Continue (tvNext)
        findViewById<TextView>(R.id.tvNext)?.setOnClickListener {
            val selected = getSelectedLanguage()
            if (selected == null) {
                Toast.makeText(this, com.cyber.ads.R.string.please_select_a_language, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val langCode = selected.langCode.removePrefix("group_")
            setCurrentLang(langCode)
            nextActivity()
        }
    }

    private fun updateYouSelected() {
        val selected = getSelectedLanguage() ?: return
        findViewById<ImageView>(R.id.ivSelectedFlag)?.setImageResource(selected.flag)
        findViewById<TextView>(R.id.tvSelectedName)?.text = selected.name
    }

    private fun getSelectedLanguage(): LanguageModel? {
        val rv = findViewById<RecyclerView>(R.id.rvLanguage) ?: return null
        val adapter = rv.adapter as? LanguageAdapter ?: return null
        return adapter.currentList.firstOrNull { it.selected }
            ?: BaseLanguageActivity.languages.firstOrNull { it.selected }
            ?: BaseLanguageActivity.languages.firstOrNull { it.langCode == "en" }
    }

    // 3. Callback điều hướng khi bấm nút Next / Tick hoàn tất chọn ngôn ngữ
    override fun nextActivity() {
        AdsManager.loadAndShowInter(this, RemoteConfig.INTER_LANGUAGE) {
            if (fromSplash) {
                // Đi tiếp tới luồng Onboarding (Lần đầu mở app)
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("open_onboarding", true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            } else {
                // Đổi ngôn ngữ từ màn Settings -> Reset task và quay lại MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("open_home", true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
