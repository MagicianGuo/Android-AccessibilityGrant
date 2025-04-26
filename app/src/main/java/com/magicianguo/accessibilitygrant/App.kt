package com.magicianguo.accessibilitygrant

import android.app.Application
import android.os.Build
import com.magicianguo.libshizuku.ShizukuUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        addHiddenApiExemptions()
        ShizukuUtils.init(this)
    }

    private fun addHiddenApiExemptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
    }
}