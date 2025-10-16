package me.ikirby.pixelutils

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.Instrumentation
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.os.ServiceManager
import android.system.Os
import android.telephony.CarrierConfigManager
import rikka.shizuku.ShizukuBinderWrapper

class InstrumentationHelper : Instrumentation() {

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        if (arguments == null) {
            finish(0, null)
            return
        }
        val subId = arguments.getInt("subId", 0)
        val overrides = arguments.getParcelable("overrides", PersistableBundle::class.java)
        val persistent = arguments.getBoolean("persistent", false)
        overrideConfig(subId, overrides, persistent)
    }

    @SuppressLint("MissingPermission")
    private fun overrideConfig(subId: Int, overrides: PersistableBundle?, persistent: Boolean) {
        val ams = IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(ServiceManager.getService(Context.ACTIVITY_SERVICE))
        )
        ams.startDelegateShellPermissionIdentity(Os.getuid(), null)
        try {
            val ccm = context.getSystemService(CarrierConfigManager::class.java)
            ccm.overrideConfig(subId, overrides, persistent)
        } finally {
            ams.stopDelegateShellPermissionIdentity()
        }
        finish(0, null)
    }
}
