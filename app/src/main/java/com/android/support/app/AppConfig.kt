package com.android.support.app

import com.android.support.navigation.DevRouter
import com.android.support.navigation.ProRouter
import com.android.support.navigation.Router


interface AppConfig {
    fun createRoute(): Router = DevRouter()

    val hotline: String get() = "0962221259"
    val endpoint: String get() = "https://quangpv.ddns.net/api"
    val webLink: String get() = "https://quangpv.ddns.net/"
    val shouldLog: Boolean get() = true

    companion object : AppConfig by BuildModeDelegate.mode
}

private object BuildModeDelegate {
    val mode = when ("dev") {
        "pro" -> switchMode(ProDebug(), ProRelease())
        "dev" -> switchMode(DevDebug(), DevRelease())
        "staging" -> switchMode(StagingDebug(), StagingRelease())
        else -> error("Not support build config")
    }

    private fun switchMode(debug: AppConfig, release: AppConfig): AppConfig {
//        return if (BuildConfig.DEBUG) debug else release
        return if (true) debug else release
    }
}

private open class DevDebug : AppConfig

private open class StagingDebug : AppConfig

private open class ProDebug : AppConfig {
    override val endpoint: String get() = "https://quangpv.ddns.net/api/"

    override fun createRoute(): Router {
        return ProRouter()
    }
}

private class ProRelease : ProDebug() {
    override val shouldLog: Boolean get() = false
}

private class DevRelease : DevDebug() {
    override val shouldLog: Boolean get() = true
}

private class StagingRelease : StagingDebug() {
    override val shouldLog: Boolean get() = false
}