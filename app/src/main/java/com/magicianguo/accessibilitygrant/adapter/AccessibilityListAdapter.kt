package com.magicianguo.accessibilitygrant.adapter

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.magicianguo.accessibilitygrant.R
import com.magicianguo.accessibilitygrant.bean.AccessibilityItemBean

class AccessibilityListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mList = mutableListOf<AccessibilityItemBean>()

    fun updateList(list: List<AccessibilityItemBean>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AccessibilityItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_accessibility_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemBean = mList[position]
        val context = holder.itemView.context
        if (holder is AccessibilityItemHolder) {
            holder.ivIcon.setImageDrawable(itemBean.applicationIcon)
            holder.tvName.text = "应用名称：${itemBean.applicationLabel}"
            holder.tvPackage.text = "应用包名：${itemBean.packageName}"
            holder.tvServiceLabel.text = "辅助功能名称：${itemBean.serviceLabel}"
            holder.tvServiceName.text = "辅助功能服务：${itemBean.serviceName}"
            if (itemBean.appType == 0) {
                holder.tvAppType.text = "用户"
                holder.tvAppType.background = context.getDrawable(R.drawable.bg_icon_app_type_user)
            } else {
                holder.tvAppType.text = "系统"
                holder.tvAppType.background = context.getDrawable(R.drawable.bg_icon_app_type_system)
            }
            holder.swAccessibility.isChecked = itemBean.enabled
            // 这里不能使用 setOnCheckedChangeListener ，容易在滚动中触发回调
            holder.swAccessibility.setOnClickListener { v ->
                v.postDelayed({
                    val isChecked = holder.swAccessibility.isChecked
                    val fullServiceName = "${itemBean.packageName}/${itemBean.serviceName}"
                    val services = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    Log.d("AccessibilityListAdapter", "onBindViewHolder: services = $services")
                    val enabledNames = mutableListOf<String>()
                    if (!services.isNullOrEmpty()) {
                        enabledNames.addAll(services.split(":"))
                    }
                    enabledNames.remove(fullServiceName)
                    if (isChecked) {
                        enabledNames.add(fullServiceName)
                    }
                    val result = StringBuilder()
                    for (i in 0 until enabledNames.size) {
                        if (i != 0) {
                            result.append(":")
                        }
                        result.append(enabledNames[i])
                    }
                    Log.d("AccessibilityListAdapter", "onBindViewHolder: isChecked = $isChecked")
                    Log.d("AccessibilityListAdapter", "onBindViewHolder: result = $result")
                    Settings.Secure.putString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, result.toString())
                }, 100)
            }
        }
    }

    private class AccessibilityItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvPackage: TextView = itemView.findViewById(R.id.tv_package)
        val tvServiceLabel: TextView = itemView.findViewById(R.id.tv_service_label)
        val tvServiceName: TextView = itemView.findViewById(R.id.tv_service_name)
        val tvAppType: TextView = itemView.findViewById(R.id.tv_app_type)
        val swAccessibility: SwitchMaterial = itemView.findViewById(R.id.sw_accessibility)
    }

}