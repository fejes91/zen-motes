package hu.adamfejes.zenmotes.utils

expect object Logger {
    val isDebugBuild: Boolean
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
}