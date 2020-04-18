package com.github.kr328.clash

import android.content.pm.IPackageManager
import android.net.Uri
import android.os.ServiceManager
import android.util.Log
import com.github.kr328.clash.model.Clash
import com.github.kr328.clash.model.Initial
import java.io.File

class ExecAttribute(
        val coreDir: File,
        val dataDir: File,
        val initial: Initial,
        val clash: Clash
) {
    companion object {
        const val ENV_HTTP_PORT = "CLASH_HTTP_PORT"
        const val ENV_SOCKS_PORT = "CLASH_SOCKS_PORT"
        const val ENV_REDIRECT_PORT = "CLASH_REDIR_PORT"
        const val ENV_TUN_DEVICE = "CLASH_TUN_DEVICE"
        const val ENV_DNS_PORT = "CLASH_DNS_PORT"
        const val ENV_UID = "CLASH_UID"
        const val ENV_GID = "CLASH_GID"
        const val ENV_BLACKLIST = "PROXY_BLACKLIST_UID"

        private val packageManager by lazy {
            IPackageManager.Stub.asInterface(ServiceManager.getService("package"))
                    ?: throw IllegalArgumentException("PackageManager not found")
        }
    }

    val user = UserGroupIds.USER_RADIO
    val group = UserGroupIds.GROUP_RADIO
    val groups = listOf(UserGroupIds.GROUP_INET, UserGroupIds.GROUP_RADIO, UserGroupIds.GROUP_SDCARD_RW)
    val environment: Map<String, String> by lazy { buildEnvironment() }

    fun start(): ExecProcess {
        return ExecProcess(this)
    }

    private val blacklistUid: List<Int> by lazy { parseBlacklistUid() }

    private fun buildEnvironment(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        result[ENV_UID] = user.toString()
        result[ENV_GID] = group.toString()
        result[ENV_BLACKLIST] = blacklistUid.joinToString(separator = " ")

        if (clash.httpPort > 0)
            result[ENV_HTTP_PORT] = clash.httpPort.toString()
        if (clash.socksPort > 0)
            result[ENV_SOCKS_PORT] = clash.socksPort.toString()
        if (clash.redirectPort > 0)
            result[ENV_REDIRECT_PORT] = clash.redirectPort.toString()

        if (clash.dns.enable) {
            Utils.splitHostPort(clash.dns.listen)?.apply {
                if (second > 0)
                    result[ENV_DNS_PORT] = second.toString()
            }
        }

        if (clash.tun.enable) {
            Uri.parse(clash.tun.deviceUrl).takeIf { it != Uri.EMPTY && it.scheme == "dev" }?.apply {
                result[ENV_TUN_DEVICE] = host ?: return@apply
            }
        }

        return result
    }

    private fun parseBlacklistUid(): List<Int> {
        return initial.blacklist
                .map {
                    it.split(":")
                }.filter {
                    it.size >= 2
                }
                .mapNotNull {
                    when (it[0]) {
                        "package" ->
                            loadPackageUid(it[1], it.getOrNull(2)?.toIntOrNull())
                        "uid" ->
                            it[1].toIntOrNull()
                        "user" ->
                            android.os.Process.getUidForName(it[1]).takeIf { uid -> uid >= 0 }
                        else -> null
                    }
                }
                .distinct()
    }

    private fun loadPackageUid(packageName: String, userId: Int?): Int? {
        return try {
            packageManager.getApplicationInfo(packageName, 0, userId ?: 0)?.uid
        } catch (e: Exception) {
            Log.w(Constants.TAG, "Package $packageName not found", e)
            null
        }
    }
}