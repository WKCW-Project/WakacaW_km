package org.wakacaw.km.profile

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.wakacaw.km.core.RootShell
import org.wakacaw.km.core.Sysfs

val Application.datastore by preferencesDataStore(name = "wakacaw")

object Profiles {
    private val KEY_ACTIVE = stringPreferencesKey("active_profile_json")

    fun applyCurrent(): Boolean = runBlocking {
        val app = getApp() ?: return@runBlocking false
        val json = app.datastore.data.first()[KEY_ACTIVE] ?: return@runBlocking false
        val map = parse(json)
        apply(map)
    }

    fun saveAndApply(map: Map<String,String>): Boolean = runBlocking {
        val app = getApp() ?: return@runBlocking false
        app.datastore.edit { it[KEY_ACTIVE] = serialize(map) }
        apply(map)
    }

    private fun apply(values: Map<String,String>): Boolean {
        var ok = true
        Sysfs.FAST_CHARGE?.let { path ->
            values["fast_charge"]?.let { v -> ok = ok && RootShell.write(path, if (v=="1") "1" else "0") }
        }
        values["tcp"]?.let { v -> ok = ok && RootShell.write(Sysfs.TCP_CONGESTION, v) }
        Sysfs.CPU_GOVERNOR?.let { path ->
            values["cpu_gov"]?.let { v -> ok = ok && RootShell.write(path, v) }
        }
        Sysfs.IO_SCHEDULER?.let { path ->
            values["io_sched"]?.let { v -> ok = ok && RootShell.write(path, v) }
        }
        return ok
    }

    private fun serialize(m: Map<String,String>) = m.entries.joinToString(";"){ "${it.key}=${it.value}" }
    private fun parse(s: String) = s.split(";").mapNotNull {
        val i = it.indexOf("="); if (i>0) it.substring(0,i) to it.substring(i+1) else null
    }.toMap()

    // quick way to get Application
    private fun getApp(): Application? = try {
        val activityThread = Class.forName("android.app.ActivityThread")
        val currentApplication = activityThread.getMethod("currentApplication")
        currentApplication.invoke(null) as? Application
    } catch (_: Throwable) { null }
}
