package org.wakacaw.km.core

import com.topjohnwu.superuser.Shell

object RootShell {
    fun isRooted(): Boolean = try {
        Shell.getShell().isRoot
    } catch (e: Throwable) {
        false
    }

    fun run(cmd: String): Shell.Result = Shell.cmd(cmd).exec()

    fun read(path: String): String? {
        val r = Shell.cmd("cat \"$path\"").exec()
        return if (r.isSuccess && r.out.isNotEmpty()) r.out.joinToString("\n") else null
    }

    fun write(path: String, value: String): Boolean {
        val r = Shell.cmd("echo %s > \"%s\"", value, path).exec()
        return r.isSuccess
    }
}
