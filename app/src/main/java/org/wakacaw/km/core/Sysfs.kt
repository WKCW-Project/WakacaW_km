package org.wakacaw.km.core

import com.topjohnwu.superuser.Shell

object Sysfs {
    private fun firstExisting(vararg patterns: String): String? {
        val joined = patterns.joinToString(" ")
        val res = Shell.cmd("ls $joined 2>/dev/null | head -n 1").exec()
        return res.out.firstOrNull()
    }

    val CPU_GOVERNOR by lazy {
        firstExisting(
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
            "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor"
        )
    }

    val TCP_CONGESTION: String = "/proc/sys/net/ipv4/tcp_congestion_control"

    val IO_SCHEDULER by lazy {
        firstExisting(
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/sda/queue/scheduler",
            "/sys/block/dm-0/queue/scheduler"
        )
    }

    val FAST_CHARGE by lazy {
        firstExisting(
            "/sys/kernel/fast_charge/force_fast_charge",
            "/sys/class/power_supply/battery/charging_enabled"
        )
    }
}
