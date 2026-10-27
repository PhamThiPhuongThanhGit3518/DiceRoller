package com.phuongthanh.diceroller.core.ads

import com.cyber.ads.remote.InterHolder
import com.cyber.ads.remote.NativeHolder
import com.cyber.ads.remote.NativeMultiHolder

object RemoteConfig {
    const val KEY_SHOW_LANGUAGE_INTRO_EVERY_OPEN = "show_language_intro_every_open"

    var NATIVE_LANGUAGE = NativeMultiHolder("native_language")
    var NATIVE_LANGUAGE_RE: NativeMultiHolder? = NativeMultiHolder("native_language_re")
    var NATIVE_LANGUAGE_SMALL: NativeHolder? = NativeHolder("native_language_small")

    var NATIVE_INTRO_FULL: NativeMultiHolder? = NativeMultiHolder("native_intro_full")
    var NATIVE_INTRO_FULL_RE: NativeMultiHolder? = NativeMultiHolder("native_intro_full_re")
    var NATIVE_INTRO: NativeMultiHolder? = NativeMultiHolder("native_intro")
    var NATIVE_INTRO_RE: NativeMultiHolder? = NativeMultiHolder("native_intro_re")

    var INTER_LANGUAGE = InterHolder("inter_language")

    fun showLanguageIntroEveryOpen(): Boolean {
        return true
    }
}
