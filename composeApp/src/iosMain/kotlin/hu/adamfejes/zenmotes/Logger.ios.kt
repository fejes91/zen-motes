import platform.Foundation.NSLog

actual object Logger {
    actual fun d(tag: String, message: String) {
        NSLog("[$tag] $message")
    }
    
    actual fun i(tag: String, message: String) {
        NSLog("[$tag] $message")
    }
    
    actual fun w(tag: String, message: String) {
        NSLog("[$tag] WARNING: $message")
    }
    
    actual fun e(tag: String, message: String) {
        NSLog("[$tag] ERROR: $message")
    }
}