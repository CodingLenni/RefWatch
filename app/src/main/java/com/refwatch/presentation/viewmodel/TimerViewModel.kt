import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refwatch.data.GameSettingsRepository
import com.refwatch.data.GameSettingsRepositoryImpl
import com.refwatch.presentation.model.GameSettings
import com.refwatch.presentation.model.Halftime
import com.refwatch.presentation.model.Period
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for a soccer halftime timer. It counts up to the standard half duration (Primary Timer),
 * stops, and then automatically starts an indefinite Additional Time timer.
 */
class TimerViewModel(
    gameSettingsRepository: GameSettingsRepository = GameSettingsRepositoryImpl,
) : ViewModel() {

    // --- Primary (Match) Timer Exposed State ---
    private val _period = MutableStateFlow(Period.FIRST)
    val halftime: StateFlow<Halftime> =
        combine(
            _period,
            gameSettingsRepository.selectedGame
        ) { period, game ->
            when (period) {
                Period.FIRST -> game.firstHalf
                Period.SECOND -> game.secondHalf
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GameSettings.defaultSetting.firstHalf
        )

    /** The time elapsed during the standard match period. */
    private val _elapsedTimeDuration =
        MutableStateFlow(Duration.ZERO + GameSettings.defaultSetting.firstHalf.startTimeMinutes)
    val playClockTimer: StateFlow<Duration> = combine(
        _elapsedTimeDuration,
        halftime
    ) { elapsed, halftime -> elapsed + halftime.startTimeMinutes }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Duration.ZERO + GameSettings.defaultSetting.firstHalf.startTimeMinutes
        )


    /** The time remaining until the standard match target duration is reached. */
    private val _timeLeftDuration = MutableStateFlow(Duration.ZERO)
    val timeLeftDuration: StateFlow<Duration> = _timeLeftDuration.asStateFlow()

    /** Tracks the running state of the primary timer. */
    private val _isPrimaryTimerRunning = MutableStateFlow(false)
    val isPrimaryTimerRunning: StateFlow<Boolean> = _isPrimaryTimerRunning.asStateFlow()

    /** Signals when the standard match time is completed (the whistle for full time). */
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    // --- Additional Time Timer Exposed State ---

    /** Tracks the time elapsed during the Additional Time period. */
    private val _additionalTimeDuration = MutableStateFlow(Duration.ZERO)
    val additionalTimeDuration: StateFlow<Duration> = _additionalTimeDuration.asStateFlow()

    /** Signals when the Additional Time timer is actively running. */
    private val _isAdditionalTimeRunning = MutableStateFlow(false)
    val isAdditionalTimeRunning: StateFlow<Boolean> = _isAdditionalTimeRunning.asStateFlow()

    val isRunning = combine(
        isPrimaryTimerRunning,
        isAdditionalTimeRunning
    ) { primary, additional ->
        primary || additional
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val timerInStartPosition = combine(
        halftime,
        _elapsedTimeDuration
    ) { halftime, elapsedTime -> elapsedTime == Duration.ZERO }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    // --- Internal Logic ---

    private var primaryTimerJob: Job? = null
    private var additionalTimerJob: Job? = null
    private var startTimeMs: Long = 0L

    private var accumulatedDuration: Duration = Duration.ZERO // For primary timer
    private var accumulatedAdditionalDuration: Duration = Duration.ZERO // For additional time

    private val TIMER_UPDATE_INTERVAL = 100L

    init {
        _timeLeftDuration.value = halftime.value.length
        viewModelScope.launch {
            halftime.collect { half ->
                _timeLeftDuration.value = half.length
            }
        }
    }

    // --- Configuration ---

    fun switchPeriod() {
        _period.value = if (_period.value == Period.FIRST) Period.SECOND else Period.FIRST
    }

    // --- Primary Timer Control ---

    fun startTimer() {
        if (_isPrimaryTimerRunning.value || _isFinished.value || _isAdditionalTimeRunning.value) return

        _isPrimaryTimerRunning.value = true
        startTimeMs = System.currentTimeMillis()
        val currentTarget = halftime.value.length

        primaryTimerJob = viewModelScope.launch {
            while (_isPrimaryTimerRunning.value) {
                val timeSinceStart = (System.currentTimeMillis() - startTimeMs).milliseconds
                val currentTotalDuration = accumulatedDuration + timeSinceStart
                var timeLeft = currentTarget - currentTotalDuration

                // Check for target completion
                if (currentTotalDuration >= currentTarget) {
                    // TARGET REACHED: Stop primary, start additional time
                    _elapsedTimeDuration.value = currentTarget
                    _timeLeftDuration.value = Duration.ZERO
                    accumulatedDuration = currentTarget
                    pauseTimer(isPrimaryOnly = true)
                    _isFinished.value = true
                    startAdditionalTimer() // Start the new timer
                    break
                }

                if (timeLeft < Duration.ZERO) {
                    timeLeft = Duration.ZERO
                }

                _elapsedTimeDuration.value = currentTotalDuration
                _timeLeftDuration.value = timeLeft

                delay(TIMER_UPDATE_INTERVAL)
            }
        }
    }

    /**
     * Pauses the currently running timer(s).
     * @param isPrimaryOnly If true, only pauses the primary timer (used internally when switching).
     */
    fun pauseTimer(isPrimaryOnly: Boolean = false) {
        if (_isPrimaryTimerRunning.value) {
            _isPrimaryTimerRunning.value = false
            primaryTimerJob?.cancel()
            primaryTimerJob = null
            accumulatedDuration = _elapsedTimeDuration.value // Save primary time
        }

        if (!isPrimaryOnly && _isAdditionalTimeRunning.value) {
            _isAdditionalTimeRunning.value = false
            additionalTimerJob?.cancel()
            additionalTimerJob = null
            accumulatedAdditionalDuration = _additionalTimeDuration.value // Save additional time
        }
    }

    // --- Additional Time Control ---

    /**
     * Starts the Additional Time timer. This runs indefinitely after the main time completes.
     */
    private fun startAdditionalTimer() {
        if (_isAdditionalTimeRunning.value) return

        _isAdditionalTimeRunning.value = true
        // Use current system time as the reference start point for the new phase
        startTimeMs = System.currentTimeMillis()

        additionalTimerJob = viewModelScope.launch {
            while (_isAdditionalTimeRunning.value) {
                val timeSinceStart = (System.currentTimeMillis() - startTimeMs).milliseconds
                val currentTotalAdditionalDuration = accumulatedAdditionalDuration + timeSinceStart

                _additionalTimeDuration.value = currentTotalAdditionalDuration

                delay(TIMER_UPDATE_INTERVAL)
            }
        }
    }

    // --- Reset Control ---

    /**
     * Stops all timers and resets all elapsed and accumulated times to zero.
     */
    fun resetTimer() {
        pauseTimer() // Stops both timers

        // Reset primary timer state
        accumulatedDuration = Duration.ZERO
        _elapsedTimeDuration.value = Duration.ZERO
        _timeLeftDuration.value = halftime.value.length
        _isPrimaryTimerRunning.value = false

        _isFinished.value = false

        // Reset additional time state
        accumulatedAdditionalDuration = Duration.ZERO
        _additionalTimeDuration.value = Duration.ZERO
    }

    /**
     * Ensures the coroutines are cancelled when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        primaryTimerJob?.cancel()
        additionalTimerJob?.cancel()
    }
}