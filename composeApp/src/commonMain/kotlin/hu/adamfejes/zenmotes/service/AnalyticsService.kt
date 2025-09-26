package hu.adamfejes.zenmotes.service

interface AnalyticsService {
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    fun trackScreenView(screenName: String)
    fun trackGameStart()
    fun trackGameEnd(score: Long, duration: Long)
    fun trackGameOver(achievedScore: Long, highScore: Long, isNewHighScore: Boolean)
    fun trackGamePause()
    fun trackGameResume()
    fun trackSettingsChanged(settingName: String, newValue: Any)
    fun setUserProperty(propertyName: String, value: String)
}