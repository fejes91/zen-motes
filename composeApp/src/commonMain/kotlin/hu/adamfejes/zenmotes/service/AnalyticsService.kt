package hu.adamfejes.zenmotes.service

interface AnalyticsService {
    fun trackScreenView(screenName: String)
    fun trackGameStart()
    fun trackScoreUpdate(amount: Int, isBonus: Boolean)
    fun trackGameOver(achievedScore: Long, highScore: Long, isNewHighScore: Boolean)
    fun trackGamePause(currentScore: Int, countdownTime: Long)
    fun trackGameResume(currentScore: Int, countdownTime: Long)
    fun trackSettingsChanged(settingName: String, newValue: Any)
    fun setUserProperty(propertyName: String, value: String)
    fun trackAverageFPS(average: Int)
}