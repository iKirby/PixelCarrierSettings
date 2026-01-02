package me.ikirby.pixelutils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyFrameworkInitializer
import android.telephony.ims.ProvisioningManager
import android.view.View
import android.widget.Toast
import com.android.internal.telephony.ISub
import com.android.internal.telephony.ITelephony
import me.ikirby.pixelutils.databinding.ActivityMainBinding
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import rikka.shizuku.ShizukuBinderWrapper

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var telephony: ITelephony
    private lateinit var sub: ISub
    private var subIdPhone0 = 0
    private var subIdPhone1 = 0

    private val shizukuPermissionListener =
        OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                showToast(R.string.shizuku_permission_not_granted)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HiddenApiBypass.setHiddenApiExemptions("")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnableIMS0.setOnClickListener { enableIMSProvisioning(subIdPhone0) }
        binding.btnResetIMS0.setOnClickListener { resetIMS(subIdPhone0) }
        binding.btnConfigOverrides0.setOnClickListener {
            val intent = Intent(this, ConfigOverridesActivity::class.java).apply {
                putExtra("subId", subIdPhone0)
                putExtra("displayName", binding.textNameSub0.text.toString())
            }
            startActivity(intent)
        }
        binding.btnDisableIMS0.setOnClickListener { disableIMSProvisioning(subIdPhone0) }

        binding.btnEnableIMS1.setOnClickListener { enableIMSProvisioning(subIdPhone1) }
        binding.btnResetIMS1.setOnClickListener { resetIMS(subIdPhone1) }
        binding.btnConfigOverrides1.setOnClickListener {
            val intent = Intent(this, ConfigOverridesActivity::class.java).apply {
                putExtra("subId", subIdPhone1)
                putExtra("displayName", binding.textNameSub1.text.toString())
            }
            startActivity(intent)
        }
        binding.btnDisableIMS1.setOnClickListener { disableIMSProvisioning(subIdPhone1) }

        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
    }

    override fun onStart() {
        super.onStart()
        checkShizukuPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog(msg: String) {
        AlertDialog.Builder(this).apply {
            setMessage(msg)
            setPositiveButton(android.R.string.ok) { _, _ -> finish() }
        }.show()
    }

    private fun checkShizukuPermission() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                requestShizukuPermission()
                return
            }
            init()
        } catch (_: Exception) {
            showDialog(getString(R.string.shizuku_not_running))
        }
    }

    private fun requestShizukuPermission() {
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            showToast(R.string.shizuku_permission_not_granted)
            return
        }

        Shizuku.requestPermission(0)
    }

    private fun init() {
        TelephonyFrameworkInitializer
            .getTelephonyServiceManager()
            .telephonyServiceRegisterer
            .get()?.let {
                telephony = ITelephony.Stub.asInterface(ShizukuBinderWrapper(it))
            }
        TelephonyFrameworkInitializer
            .getTelephonyServiceManager()
            .subscriptionServiceRegisterer
            .get()?.let {
                sub = ISub.Stub.asInterface(ShizukuBinderWrapper(it))
            }
        if (!::telephony.isInitialized || !::sub.isInitialized) {
            showDialog(getString(R.string.init_failed))
            return
        }
        loadSubscriptionStatus()
    }

    private fun enableIMSProvisioning(subId: Int) {
        if (telephony.getImsProvisioningInt(
                subId,
                ProvisioningManager.KEY_VOIMS_OPT_IN_STATUS
            ) == ProvisioningManager.PROVISIONING_VALUE_ENABLED
        ) {
            showToast(R.string.voims_already_enabled)
            return
        }
        telephony.setImsProvisioningInt(
            subId,
            ProvisioningManager.KEY_VOIMS_OPT_IN_STATUS,
            ProvisioningManager.PROVISIONING_VALUE_ENABLED
        )
        showToast(R.string.voims_enabled)
    }

    private fun disableIMSProvisioning(subId: Int) {
        telephony.setImsProvisioningInt(
            subId,
            ProvisioningManager.KEY_VOIMS_OPT_IN_STATUS,
            ProvisioningManager.PROVISIONING_VALUE_DISABLED
        )
        showToast(R.string.voims_disabled)
    }

    private fun resetIMS(subId: Int) {
        telephony.resetIms(sub.getSlotIndex(subId))
        showToast(R.string.ims_reset)
    }

    private fun loadSubscriptionStatus() {
        val subscriptions = sub.getActiveSubscriptionInfoList(null, null, false)
        if (subscriptions.isEmpty()) {
            binding.layoutSub0.visibility = View.GONE
            binding.layoutSub1.visibility = View.GONE
            showToast(R.string.no_active_sim)
            return
        }

        val first = subscriptions[0]
        subIdPhone0 = first.subscriptionId
        binding.textNameSub0.text = first.displayName
        binding.layoutSub0.visibility = View.VISIBLE

        val second = subscriptions.getOrNull(1)
        if (second == null) {
            binding.layoutSub1.visibility = View.GONE
            return
        }
        subIdPhone1 = second.subscriptionId
        binding.textNameSub1.text = second.displayName
        binding.layoutSub1.visibility = View.VISIBLE
    }

}
