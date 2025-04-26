package com.magicianguo.accessibilitygrant.activity

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.magicianguo.accessibilitygrant.R
import com.magicianguo.accessibilitygrant.adapter.AccessibilityListAdapter
import com.magicianguo.accessibilitygrant.bean.AccessibilityItemBean
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.demo.util.ShizukuSystemServerApi

class MainActivity : AppCompatActivity() {
    private val mBtnReqShizuku: Button by lazy { findViewById(R.id.btn_req_shizuku) }
    private val mClList: View by lazy { findViewById(R.id.cl_list) }
    private val mRvList: RecyclerView by lazy { findViewById(R.id.rv_list) }
    private val mEtSearch: EditText by lazy { findViewById(R.id.et_search) }

    private var mSearchJob: Job? = null
    private val mAdapter = AccessibilityListAdapter()
    private var mShizukuHasBinder = false
    private var mShizukuGranted = false
    private val mPermissionResultListener = object : Shizuku.OnRequestPermissionResultListener {
        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
            if (requestCode == 0) {
                mShizukuGranted = grantResult == PackageManager.PERMISSION_GRANTED
                if (mShizukuGranted) {
                    grantPermissionByShizuku(Manifest.permission.WRITE_SECURE_SETTINGS)
                    loadList()
                }
            }
        }
    }

    private val mSearchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mSearchJob?.cancel()
            mSearchJob = lifecycleScope.launch {
                delay(1000)
                loadList()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private val mComparator =
        Comparator<AccessibilityItemBean> { o1, o2 -> o1.appType - o2.appType }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        mEtSearch.addTextChangedListener(mSearchTextWatcher)
        Shizuku.addRequestPermissionResultListener(mPermissionResultListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mEtSearch.removeTextChangedListener(mSearchTextWatcher)
        Shizuku.removeRequestPermissionResultListener(mPermissionResultListener)
    }

    private fun initView() {
        mRvList.layoutManager = LinearLayoutManager(this)
        mRvList.adapter = mAdapter
        mShizukuHasBinder = Shizuku.pingBinder()
        mShizukuGranted = mShizukuHasBinder && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        if (mShizukuGranted) {
            loadList()
        }
        mBtnReqShizuku.setOnClickListener {
            if (!mShizukuHasBinder) {
                Toast.makeText(this, "请激活Shizuku并重启应用！", Toast.LENGTH_SHORT).show()
            } else {
                Shizuku.requestPermission(0)
            }
        }
    }

    private fun loadList() {
        mBtnReqShizuku.isVisible = false
        mClList.isVisible = true
        val services = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val accessibilityList = arrayListOf<AccessibilityItemBean>()
        val enabledNames = if (!services.isNullOrEmpty()) {
            services.split(":")
        } else {
            arrayListOf()
        }
        Log.d("MainActivity", "loadList: enabledNames = $enabledNames")
        val resolveInfos = packageManager.queryIntentServices(
            Intent("android.accessibilityservice.AccessibilityService"),
            0
        )
        resolveInfos.forEach { resolveInfo ->

        }
        for (i in 0 until resolveInfos.size) {
            val resolveInfo = resolveInfos[i]
            val packageName = resolveInfo.serviceInfo.packageName
            Log.d("MainActivity", "loadList: resolveInfo.serviceInfo = ${resolveInfo.serviceInfo.name}")
            val applicationIcon = packageManager.getApplicationIcon(packageName)
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val applicationLabel = packageManager.getApplicationLabel(applicationInfo).toString()
            val enabled = "${enabledNames}/".contains("${packageName}/")
            val serviceLabel = resolveInfo.serviceInfo.loadLabel(packageManager)
            val serviceName = resolveInfo.serviceInfo.name
            // 应用类型，0为用户安装应用，1为系统应用
            val appType = if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) 0 else 1
            val inputText = mEtSearch.text.toString().uppercase()
            if (inputText.isNotEmpty() && !applicationLabel.uppercase().contains(inputText)) {
                continue
            }
            accessibilityList.add(AccessibilityItemBean(packageName, applicationIcon, applicationLabel, enabled, serviceLabel.toString(), serviceName, appType))
        }
        accessibilityList.sortWith(mComparator)
        mAdapter.updateList(accessibilityList)
    }

    private fun grantPermissionByShizuku(permission: String) {
        val pkg: String = packageName
        val userId = 0
        try {
            if (ShizukuSystemServerApi.PackageManager_checkPermission(permission, pkg, userId)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ShizukuSystemServerApi.PackageManager_grantRuntimePermission(
                    pkg,
                    permission,
                    userId
                )
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}