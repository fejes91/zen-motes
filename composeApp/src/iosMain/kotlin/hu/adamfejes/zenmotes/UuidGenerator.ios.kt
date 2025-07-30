import platform.Foundation.NSUUID

actual object UuidGenerator {
    actual fun randomUUID(): String = NSUUID().UUIDString()
}