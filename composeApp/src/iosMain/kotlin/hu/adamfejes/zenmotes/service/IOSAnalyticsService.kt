package hu.adamfejes.zenmotes.service

import hu.adamfejes.zenmotes.utils.Logger

class IOSAnalyticsService : AnalyticsService {
    override fun trackEvent(eventName: String, parameters: Map<String, Any>) {
        Logger.d("Analytics", "Event: $eventName, Parameters: $parameters")
        // TODO: Implement iOS-specific analytics (Firebase, App Store Connect, etc.)
    }

    override fun trackScreenView(screenName: String) {
        trackEvent("screen_view", mapOf("screen_name" to screenName))
    }

    override fun trackGameStart() {
        trackEvent("game_start")
    }

    override fun trackGameEnd(score: Long, duration: Long) {
        trackEvent("game_end", mapOf(
            "score" to score,
            "duration_seconds" to duration
        ))
    }

    override fun trackGameOver(achievedScore: Long, highScore: Long, isNewHighScore: Boolean) {
        trackEvent("game_over", mapOf(
            "achieved_score" to achievedScore,
            "high_score" to highScore,
            "is_new_high_score" to isNewHighScore
        ))
    }

    override fun trackGamePause() {
        trackEvent("game_pause")
    }

    override fun trackGameResume() {
        trackEvent("game_resume")
    }

    override fun trackSettingsChanged(settingName: String, newValue: Any) {
        trackEvent("settings_changed", mapOf(
            "setting_name" to settingName,
            "new_value" to newValue
        ))
    }

    override fun setUserProperty(propertyName: String, value: String) {
        Logger.d("Analytics", "User Property Set - $propertyName: $value")
        // TODO: Implement iOS-specific user properties
    }
}