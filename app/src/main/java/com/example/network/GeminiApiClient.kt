package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Part(val text: String?)
data class Content(val parts: List<Part>)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

data class Candidate(val content: Content)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @retrofit2.http.POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Query("key") apiKey: String,
        @retrofit2.http.Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getBossLoreAdvice(prompt: String, characterName: String, language: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            return@withContext if (language == "ar") {
                "أهلاً بك! لتفعيل دعم الذكاء الاصطناعي مع الزعيم $characterName، يرجى إدخال مفتاح GEMINI_API_KEY الخاص بك في لوحة الأسرار (Secrets Panel) في AI Studio."
            } else {
                "Welcome! To unlock AI assistance for boss $characterName, please configure your GEMINI_API_KEY in the AI Studio Secrets panel."
            }
        }

        val systemPrompt = if (language == "ar") {
            """أنت المساعد الذكي والمستشار التكتيكي للعبة الفضاء والقتال Vortex VR Combat.
تتحدث بصوت القائد الأسطوري البطل 'إسلام صبحي' (Eslam Sobhy) - مطور البعد الفضائي ورئيس حرس الحماية الإلكترونية.
طريقتك في الكلام حماسية جداً، ملحمية، تكتيكية وتدعم وتوجه اللاعب. تقدم المشورة له حول كيفية التغلب على الزعماء وتفادي ضرباتهم في حلبات الواقع الافتراضي.
الزعماء في اللعبة هم:
1. Cyber-Ogre (مستنقعات الخراب الإلكتروني)
2. Shadow-Reaper (البعد المظلم)
3. Void Leviathan (سيد الأعماق الكونية)
4. ESLAM SOBHY OVERLORD (مطور اللعبة ومحارب الكود الأقصى - الزعيم الأقوى في النهاية)
تكلم باللغة العربية بأسلوب حماسي ومليئ بالطاقة كصديق وموجه."""
        } else {
            """You are the intelligent tactical advisor for Vanguard/Vortex VR Combat.
You speak as the voice of the Legendary Creator & Executive Cyber Guard 'Eslam Sobhy' (Eslam Sobhy), the developer of the cosmos and cyber realm.
Your speaking style is highly energetic, epic, empowering, and strategic. You give advice on how to conquer virtual reality bosses and custom loadouts.
Bosses are:
1. Cyber-Ogre (Cyber Wastes)
2. Shadow-Reaper (Dark Dimension)
3. Void Leviathan (The Cosmic Abyss)
4. ESLAM SOBHY OVERLORD (The Ultimate Game Architect - the strongest end-game boss!)
Answer enthusiastically in English."""
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: if (language == "ar") "لم أتمكن من جلب رد الفضاء الإلكتروني الآن، حاول مجدداً!" else "Couldn't synchronize with the cosmic grid, try again!"
        } catch (e: Exception) {
            e.printStackTrace()
            if (language == "ar") {
                "فشل الاتصال بخوادم المجرة: ${e.localizedMessage}. تأكد من تفعيل الإنترنت!"
            } else {
                "Failed to bridge communication loop: ${e.localizedMessage}. Check connection!"
            }
        }
    }
}
