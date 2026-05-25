package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.GameRepository
import com.example.database.PlayerProfile
import com.example.database.WeaponItem
import com.example.network.GeminiApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Representation of message in Chat
data class ChatMessage(
    val sender: String, // "Player", "EslamAI" or "System"
    val text: String,
    val timeMillis: Long = System.currentTimeMillis()
)

// Boss characteristics
data class BossData(
    val level: Int,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val maxHealth: Float,
    val attackPower: Float,
    val primaryColor: String // Hex
)

sealed class MatchmakingState {
    object Idle : MatchmakingState()
    object Searching : MatchmakingState()
    data class Found(val opponent: String, val latencyMs: Int, val serverIp: String) : MatchmakingState()
    object Connected : MatchmakingState()
}

// Transaction representation
data class SecurityTransaction(
    val transactionId: String,
    val itemName: String,
    val price: Int,
    val status: String, // "SECURELY_AUTHORIZED", "COMPLETED"
    val hash: String,
    val timestamp: Long
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    val allWeapons: StateFlow<List<WeaponItem>>
    val playerProfile: StateFlow<PlayerProfile?>

    // Viewport and combat simulation state
    private val _gameLevel = MutableStateFlow(1)
    val gameLevel = _gameLevel.asStateFlow()

    private val _bossHealth = MutableStateFlow(200f)
    val bossHealth = _bossHealth.asStateFlow()

    private val _playerHealth = MutableStateFlow(100f)
    val playerHealth = _playerHealth.asStateFlow()

    private val _isVRModeOn = MutableStateFlow(false)
    val isVRModeOn = _isVRModeOn.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("ar")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    // 3D rotation angles for custom drawing
    private val _rotationX = MutableStateFlow(0f)
    val rotationX = _rotationX.asStateFlow()

    private val _rotationY = MutableStateFlow(0f)
    val rotationY = _rotationY.asStateFlow()

    // Matchmaking state
    private val _matchmaking = MutableStateFlow<MatchmakingState>(MatchmakingState.Idle)
    val matchmaking = _matchmaking.asStateFlow()

    // Support chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // Secure payments logs
    private val _transactions = MutableStateFlow<List<SecurityTransaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    // Screen Notification message
    private val _combatLogs = MutableStateFlow<List<String>>(emptyList())
    val combatLogs = _combatLogs.asStateFlow()

    // Configured bosses
    val bosses = listOf(
        BossData(
            level = 1,
            nameAr = "Cyber-Ogre (العملاق السيبراني)",
            nameEn = "Cyber-Ogre (Level 1)",
            descriptionAr = "قائم على رادارات متقدمة يتواجد في مستنقعات الخراب الرقمي. يطلق شعاعاً عشوائياً.",
            descriptionEn = "Ranging deep in the cyber scrap wastes. Shoots toxic laser orbs.",
            maxHealth = 200f,
            attackPower = 8f,
            primaryColor = "#00FF66"
        ),
        BossData(
            level = 2,
            nameAr = "Shadow-Reaper (وحش البعد المظلم)",
            nameEn = "Shadow-Reaper (Level 2)",
            descriptionAr = "يتخفى باستخدام تشفير عسكري متقدم للتخفي والظهور المفاجئ خلفك.",
            descriptionEn = "Clad in military-grade active cloaking. Strikes from complete stealth.",
            maxHealth = 350f,
            attackPower = 15f,
            primaryColor = "#BB00FF"
        ),
        BossData(
            level = 3,
            nameAr = "Void Leviathan (سيد الأعماق الكونية)",
            nameEn = "Void Leviathan (Level 3)",
            descriptionAr = "وحش هيدرا مجري يمتص طاقة سفينتك ويفجر الثقوب السوداء الدوارة.",
            descriptionEn = "Cosmic multi-headed monstrosity which warps spacetime and drains energy.",
            maxHealth = 500f,
            attackPower = 24f,
            primaryColor = "#FFAA00"
        ),
        BossData(
            level = 4,
            nameAr = "ESLAM SOBHY OVERLORD (المطور الأقصى وصاحب السيادة)",
            nameEn = "ESLAM SOBHY OVERLORD (Final Boss)",
            descriptionAr = "رئيس خوادم الحماية والمهندس الأقصى لعالم الألعاب الإلكترونية. ضرباته لا تخطئ!",
            descriptionEn = "The Supreme Architect of Vortex VR. Strikes using untamed compilation arrays!",
            maxHealth = 800f,
            attackPower = 38f,
            primaryColor = "#FF0033"
        )
    )

    init {
        val gameDao = AppDatabase.getDatabase(application).gameDao()
        repository = GameRepository(gameDao)

        allWeapons = repository.allWeapons.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        playerProfile = repository.playerProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            repository.ensureDefaultsPreloaded()
            // Pull settings from saved DB profile
            repository.playerProfile.collect { profile ->
                if (profile != null) {
                    _selectedLanguage.value = profile.selectedLanguage
                    _isVRModeOn.value = profile.systemVRModeOn
                    _gameLevel.value = profile.currentLevel
                }
            }
        }

        // Setup starting system support chat greeting
        val initialGreeting = if (_selectedLanguage.value == "ar") {
            "مرحباً بك في الدعم الفني الذكي للعبة Vortex VR Combat! أنا بطل الكود والاستشارات القتالية الذكية Eslam Sobhy AI. كيف يمكنني تزويدك بالتكتيكات أو حل مشاكلك التقنية والألعاب اليوم؟"
        } else {
            "Hello and welcome to Vortex VR Combat Intelligent Support! I am your lead developer & tactical advisor Eslam Sobhy AI. How can I assist you with game strategies, weapons configuration, or platform setups today?"
        }
        _chatMessages.value = listOf(
            ChatMessage("EslamAI", initialGreeting)
        )
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            _selectedLanguage.value = lang
            val current = gameDaoDirect() ?: return@launch
            repository.updateProfile(current.copy(selectedLanguage = lang))
            
            // Post notification in system log
            addCombatLog(if (lang == "ar") "تم تغيير اللغة إلى العربية" else "Language changed to English")
        }
    }

    private suspend fun gameDaoDirect(): PlayerProfile? {
        return repository.playerProfile.stateIn(viewModelScope).value
    }

    fun toggleVRMode() {
        viewModelScope.launch {
            val nextState = !_isVRModeOn.value
            _isVRModeOn.value = nextState
            val current = gameDaoDirect()
            if (current != null) {
                repository.updateProfile(current.copy(systemVRModeOn = nextState))
            }
            if (nextState) {
                addCombatLog(if (_selectedLanguage.value == "ar") "تم تشغيل ثنائي المنظور ثلاثي الأبعاد لنظارات الواقع الافتراضي VR!" else "Stereoscopic VR display split enabled! Mount into headset.")
            } else {
                addCombatLog(if (_selectedLanguage.value == "ar") "تم العودة إلى العرض الفردي القياسي" else "Standard screen view restored.")
            }
        }
    }

    fun updateRotation(dX: Float, dY: Float) {
        _rotationX.value = (_rotationX.value + dX) % 360f
        _rotationY.value = (_rotationY.value + dY) % 360f
    }

    fun addCombatLog(log: String) {
        val current = _combatLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 8) current.removeAt(current.size - 1)
        _combatLogs.value = current
    }

    // Fire weapon damage loop
    fun fireActiveWeapon() {
        val activeWeapon = getActiveWeapon()
        val dmg = activeWeapon?.damage ?: 25f
        val name = activeWeapon?.name ?: "Vortex Blaster"
        val laserColor = activeWeapon?.crystalColor ?: "#00DDFF"

        if (_bossHealth.value <= 0f) {
            respawnLevelBoss()
            return
        }

        // Damage calculation
        val currentDmg = (dmg * (0.8f + (Math.random().toFloat() * 0.4f))) // Random hit variance
        val critical = Math.random() > 0.8
        val actualDmg = if (critical) currentDmg * 1.5f else currentDmg
        
        val bossInstance = bosses.firstOrNull { it.level == _gameLevel.value } ?: bosses[0]
        val nextBossHp = (_bossHealth.value - actualDmg).coerceAtLeast(0f)
        _bossHealth.value = nextBossHp

        // Log combat Action
        val hitMsg = if (_selectedLanguage.value == "ar") {
            if (critical) "💥 ضربة حرجة! ألحقت سحقاً بسلاح $name بقوة ${actualDmg.toInt()} بالزعيم!" 
            else "📡 أطلقت صلية من $name على الزعيم بقوة ${actualDmg.toInt()}"
        } else {
            if (critical) "💥 CRITICAL IMPACT! Obliterated boss with $name doing ${actualDmg.toInt()} damage!" 
            else "📡 Discharged $name onto boss doing ${actualDmg.toInt()} damage."
        }
        addCombatLog(hitMsg)

        if (nextBossHp <= 0f) {
            handleBossDefeat(bossInstance)
        } else {
            // Boss retorts back 30% of the times
            if (Math.random() > 0.65) {
                bossAttackBack(bossInstance)
            }
        }
    }

    private fun bossAttackBack(boss: BossData) {
        val bossDmg = (boss.attackPower * (0.7f + Math.random().toFloat() * 0.6f)).toInt()
        val nextPlayerHp = (_playerHealth.value - bossDmg).coerceAtLeast(0f)
        _playerHealth.value = nextPlayerHp

        val bossStrikeText = if (_selectedLanguage.value == "ar") {
            "⚠️ ضربة مضادة من الزعيم ${boss.nameAr} قذفتك مسببة ضرراً قدره $bossDmg!"
        } else {
            "⚠️ Retrograde burst from boss ${boss.nameEn} hit you for $bossDmg armor decay!"
        }
        addCombatLog(bossStrikeText)

        if (nextPlayerHp <= 0) {
            // Respawn / Armor self-repair
            _playerHealth.value = 100f
            val defeatMsg = if (_selectedLanguage.value == "ar") {
                "🔴 تدمر درعك القتالي! تم تشغيل الحماية التلقائية وإعادة توليد الدروع بنسبة 100%."
            } else {
                "🔴 Threat detected: Armor melted! Auto-emergency repair initiated to 100%."
            }
            addCombatLog(defeatMsg)
        }
    }

    private fun handleBossDefeat(boss: BossData) {
        val victoryCoins = boss.level * 800
        val nextLevel = (boss.level + 1).coerceAtMost(4)

        viewModelScope.launch {
            val current = gameDaoDirect() ?: return@launch
            
            // Advance stats in profile
            val updated = current.copy(
                coins = current.coins + victoryCoins,
                bossCompletedLevel = boss.level.coerceAtLeast(current.bossCompletedLevel),
                currentLevel = nextLevel
            )
            repository.updateProfile(updated)
            _gameLevel.value = nextLevel

            val victoryText = if (_selectedLanguage.value == "ar") {
                "🏆 انتصار ملحمي! تم دحر الزعيم ${boss.nameAr}! حصلت على مكافأة $victoryCoins كوينز. مستوى الصعوبة التالي مفتوح الآن."
            } else {
                "🏆 EPIC CONQUEST! Defeated ${boss.nameEn}! Awarded $victoryCoins credits. Next tier unlocked."
            }
            addCombatLog(victoryText)

            // Auto spawn next boss
            respawnLevelBossForLevel(nextLevel)
        }
    }

    fun respawnLevelBoss() {
        respawnLevelBossForLevel(_gameLevel.value)
    }

    fun changeActiveLevel(lvl: Int) {
        viewModelScope.launch {
            val profile = gameDaoDirect() ?: return@launch
            if (lvl > profile.bossCompletedLevel + 1 && lvl != 1) {
                addCombatLog(if (_selectedLanguage.value == "ar") "🔒 المرحلة مغلقة! اهزم الزعماء السابقين لفتحها." else "🔒 Node Locked! Defeat previous level boss first.")
                return@launch
            }
            
            _gameLevel.value = lvl
            repository.updateProfile(profile.copy(currentLevel = lvl))
            respawnLevelBossForLevel(lvl)
            addCombatLog(if (_selectedLanguage.value == "ar") "🪐 تم الانتقال البعدي إلى الحلبة $lvl!" else "🪐 Transposition successful to Sector $lvl!")
        }
    }

    private fun respawnLevelBossForLevel(lvl: Int) {
        val boss = bosses.firstOrNull { it.level == lvl } ?: bosses[0]
        _bossHealth.value = boss.maxHealth
        _playerHealth.value = 100f
    }

    // Weapons store/customize operations
    fun selectActiveWeapon(weapon: WeaponItem) {
        if (!weapon.isUnlocked) {
            addCombatLog(if (_selectedLanguage.value == "ar") "❌ يجب شراء سلاح ${weapon.name} أولاً!" else "❌ You must acquire ${weapon.name} first!")
            return
        }
        viewModelScope.launch {
            val profile = gameDaoDirect() ?: return@launch
            repository.updateProfile(profile.copy(chosenWeaponId = weapon.id))
            addCombatLog(if (_selectedLanguage.value == "ar") "🔋 تم تجهيز سلاح ${weapon.name}!" else "🔋 Armed and tuned to ${weapon.name}!")
        }
    }

    fun purchaseWeapon(weapon: WeaponItem) {
        viewModelScope.launch {
            val profile = gameDaoDirect() ?: return@launch
            if (profile.coins < weapon.cost) {
                addCombatLog(if (_selectedLanguage.value == "ar") "💳 رصيد غير كافٍ من الكوينز لشراء ${weapon.name}!" else "💳 Insufficient credits to purchase ${weapon.name}!")
                return@launch
            }

            // Secure financial receipt logs
            val txId = "TX-" + UUID.randomUUID().toString().take(8).uppercase()
            val secureHash = "SHA256-" + UUID.randomUUID().toString().replace("-", "").take(16)
            val newTx = SecurityTransaction(
                transactionId = txId,
                itemName = weapon.name,
                price = weapon.cost,
                status = "SECURELY_AUTHORIZED",
                hash = secureHash,
                timestamp = System.currentTimeMillis()
            )

            // Deduct coins and unlock
            repository.updateProfile(profile.copy(coins = profile.coins - weapon.cost))
            repository.updateWeapon(weapon.copy(isUnlocked = true))

            val list = _transactions.value.toMutableList()
            list.add(0, newTx)
            _transactions.value = list

            val successMsg = if (_selectedLanguage.value == "ar") {
                "🛡️ تم تأكيد الشراء الآمن بنجاح! السلاح ${weapon.name} أصبح متاحاً الآن في ترسانتك."
            } else {
                "🛡️ Secure payment authenticated! Weapon ${weapon.name} added to your arsenal."
            }
            addCombatLog(successMsg)
        }
    }

    fun upgradeWeaponDamage(weapon: WeaponItem) {
        viewModelScope.launch {
            val profile = gameDaoDirect() ?: return@launch
            val upgradeCost = 450
            if (profile.coins < upgradeCost) {
                addCombatLog(if (_selectedLanguage.value == "ar") "❌ تحتاج إلى 450 كوينز لترقية الضرر!" else "❌ Requires 450 credits to recalibrate damage!")
                return@launch
            }

            // Perform upgrade
            val upgradedWeapon = weapon.copy(
                damage = (weapon.damage + 8.5f).coerceAtMost(120f)
            )
            repository.updateProfile(profile.copy(coins = profile.coins - upgradeCost))
            repository.updateWeapon(upgradedWeapon)

            addCombatLog(if (_selectedLanguage.value == "ar") "⚡ تم زيادة طاقة سلاح ${weapon.name} مسبباً تدميراً أعلى!" else "⚡ Calibrated ${weapon.name} damage parameters to +8.5!")
        }
    }

    fun getActiveWeapon(): WeaponItem? {
        val activeId = playerProfile.value?.chosenWeaponId ?: 1
        return allWeapons.value.firstOrNull { it.id == activeId }
    }

    // Interactive matchmaking lobby simulation
    fun startFastMatchmaking() {
        viewModelScope.launch {
            _matchmaking.value = MatchmakingState.Searching
            addCombatLog(if (_selectedLanguage.value == "ar") "🛰️ يتم الاتصال بأقرب عقده خادم فائقة السرعة لخوض مباراة حيه..." else "🛰️ Relaying connection to proximity high-speed server portal...")
            
            kotlinx.coroutines.delay(2200)

            val lobbyOpponents = listOf("Vortex_Slayer_X", "Neo_Sabbah_VR", "Kodar_AI_Master", "Cyberspace_Vanguard")
            val opponent = lobbyOpponents.random()
            val latency = (5..15).random() // Extremely fast simulated latency
            val serverIp = "98.154.21." + (100..254).random()

            _matchmaking.value = MatchmakingState.Found(opponent, latency, serverIp)
            addCombatLog(if (_selectedLanguage.value == "ar") "🎮 تم الوصول لخصم كفء! ند لند في الحلبة الفاشية مع تأخير $latency مللي ثانية." else "🎮 Opponent linked! High-speed link stable ($latency ms) with peer $opponent.")
        }
    }

    fun cancelMatchmaking() {
        _matchmaking.value = MatchmakingState.Idle
        addCombatLog(if (_selectedLanguage.value == "ar") "❌ تم إلغاء البحث عن مباراة والعودة للتدريب الفردي" else "❌ Match search terminated. Retracted to training.")
    }

    // AI Support chat connection utilizing local model or Gemini 3.5 Flash REST API
    fun sendChatMessage(msg: String) {
        if (msg.trim().isEmpty()) return

        val originalHistory = _chatMessages.value.toMutableList()
        originalHistory.add(ChatMessage("Player", msg))
        _chatMessages.value = originalHistory
        _isChatLoading.value = true

        viewModelScope.launch {
            // Build full prompt for Gemini
            val currentBoss = bosses.firstOrNull { it.level == _gameLevel.value } ?: bosses[0]
            val bossDisplayName = if (_selectedLanguage.value == "ar") currentBoss.nameAr else currentBoss.nameEn
            val activeWeapon = getActiveWeapon()?.name ?: "Blaster"

            val prompt = """أهلاً القائد إسلام صبحي، أنا لاعب حالياً في المستوى ${_gameLevel.value} وأواجه الزعيم $bossDisplayName. 
سلاحي المجهز هو $activeWeapon.
سؤالي أو مشكلتي هي: $msg"""

            val reply = GeminiApiClient.getBossLoreAdvice(prompt, currentBoss.nameEn, _selectedLanguage.value)
            
            val updatedHistory = _chatMessages.value.toMutableList()
            updatedHistory.add(ChatMessage("EslamAI", reply))
            _chatMessages.value = updatedHistory
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        val initialGreeting = if (_selectedLanguage.value == "ar") {
            "مرحباً بك مجدداً القائد! هنا منارة قيادة العقل الفضائي إسلام صبحي. تفضل بطرح أسئلتك اللامتناهية حول مستويات اللعب الاستثنائية!"
        } else {
            "Welcome back Commander! This is the Eslam Sobhy command hub. Direct your inquiries here about advanced stages, weapon scaling, and real-time multiplayer grids!"
        }
        _chatMessages.value = listOf(
            ChatMessage("EslamAI", initialGreeting)
        )
    }

    // Reset profile totally
    fun scoreReset() {
        viewModelScope.launch {
            val default = PlayerProfile(
                id = 0,
                playerName = "Eslam Sobhy",
                coins = 3000,
                currentLevel = 1,
                totalWins = 12,
                bossCompletedLevel = 0,
                systemVRModeOn = false,
                selectedLanguage = _selectedLanguage.value,
                chosenWeaponId = 1
            )
            repository.updateProfile(default)
            _gameLevel.value = 1
            _bossHealth.value = 200f
            _playerHealth.value = 100f
            _matchmaking.value = MatchmakingState.Idle
            addCombatLog(if (_selectedLanguage.value == "ar") "🔄 تم إعادة تهيئة مستويات التقدم والملف الشخصي بنجاح!" else "🔄 Profile parameters successfully recalibrated to clean genesis.")
        }
    }
}
