package com.hotguy.workshopmanagement.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

// Extensión de Context que crea el DataStore como singleton ligado al ciclo
// de vida de la aplicación. El nombre "workshop_tokens" es el nombre del
// fichero en disco (dentro del directorio privado de la app, no accesible
// por otras apps en dispositivos no rooteados).
private val Context.tokenDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "workshop_tokens")

/**
 * Repositorio de almacenamiento seguro para los tokens JWT.
 *
 * Combina dos mecanismos de seguridad de la plataforma Android:
 *
 * 1. **Android Keystore**: genera y almacena una clave AES-GCM cuyo material
 *    criptográfico nunca sale del entorno seguro del sistema (en dispositivos
 *    con TEE o SE, la clave está respaldada por hardware). Disponible desde
 *    API 23, sin restricciones a partir de API 26 (nuestro minSdk).
 *
 * 2. **DataStore Preferences**: almacén asíncrono basado en coroutinas que
 *    reemplaza a SharedPreferences. Los datos se persisten en un fichero
 *    en el directorio privado de la app.
 *
 * El flujo de escritura es:
 *   token en texto plano → cifrar con AES-GCM usando la clave del Keystore
 *   → codificar IV + ciphertext en Base64 → guardar en DataStore
 *
 * El flujo de lectura es el inverso:
 *   leer Base64 de DataStore → decodificar → extraer IV y ciphertext
 *   → descifrar con la clave del Keystore → token en texto plano
 *
 * Por qué no EncryptedSharedPreferences:
 *   La librería security-crypto que lo provee está deprecated desde la
 *   versión 1.1.0-alpha. Además, tiene problemas conocidos de compatibilidad
 *   en ciertos fabricantes. Este enfoque es más explícito, más estable y
 *   más fácil de auditar.
 */
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Constantes ────────────────────────────────────────────────────────────

    private companion object {
        // Alias con el que se identifica la clave en el Android Keystore
        const val KEYSTORE_ALIAS      = "workshop_jwt_key"
        // Proveedor de Keystore del sistema Android
        const val ANDROID_KEYSTORE    = "AndroidKeyStore"
        // AES en modo GCM sin padding: cifrado autenticado (detecta manipulaciones)
        const val AES_GCM_NO_PADDING  = "AES/GCM/NoPadding"
        // Tamaño del tag de autenticación GCM en bits (128 es el máximo y recomendado)
        const val GCM_TAG_LENGTH      = 128
        // Tamaño del IV en bytes: 12 bytes = 96 bits, estándar para GCM
        const val GCM_IV_SIZE         = 12

        // Claves que identifican cada preferencia dentro del DataStore
        val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Guarda el par de tokens en DataStore, cifrados con la clave del Keystore.
     *
     * Ambos tokens se cifran antes de llamar a edit{}, de forma que si el
     * cifrado falla, el DataStore no se toca y no queda un estado inconsistente.
     *
     * @param accessToken  Token JWT de corta duración (1 hora).
     * @param refreshToken Token de refresco de larga duración (7 días).
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val encryptedAccess  = encrypt(accessToken)
        val encryptedRefresh = encrypt(refreshToken)
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = encryptedAccess
            prefs[KEY_REFRESH_TOKEN] = encryptedRefresh
        }
    }

    /**
     * Elimina ambos tokens del DataStore.
     * Se llama al hacer logout o cuando el refresh token ha expirado/sido revocado.
     */
    suspend fun clearTokens() {
        context.tokenDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    /**
     * Flow del access token descifrado.
     *
     * Emite el token actual inmediatamente al suscribirse, y vuelve a emitir
     * cada vez que el valor cambia en DataStore (por ejemplo, tras un refresco).
     * Emite null si no hay token almacenado o si el descifrado falla.
     */
    val accessTokenFlow: Flow<String?> = context.tokenDataStore.data
        .map { prefs ->
            prefs[KEY_ACCESS_TOKEN]?.let { runCatching { decrypt(it) }.getOrNull() }
        }

    /**
     * Flow del refresh token descifrado.
     * Emite null si no hay token almacenado.
     */
    val refreshTokenFlow: Flow<String?> = context.tokenDataStore.data
        .map { prefs ->
            prefs[KEY_REFRESH_TOKEN]?.let { runCatching { decrypt(it) }.getOrNull() }
        }

    /**
     * Lee el access token actual de forma síncrona.
     *
     * Solo debe usarse desde [AuthInterceptor], que opera en un hilo de IO
     * de OkHttp (no en el hilo principal) y necesita acceso síncrono al token.
     * En el resto de la app, usa [accessTokenFlow].
     *
     * @return El access token descifrado, o null si no hay sesión activa.
     */
    fun getAccessTokenSync(): String? = runCatching {
        // runBlocking es aceptable aquí: OkHttp ya corre en un hilo de IO,
        // por lo que no bloqueamos el hilo principal.
        runBlocking {
            accessTokenFlow.first()
        }
    }.getOrNull()

    /**
     * Lee el refresh token actual de forma síncrona.
     *
     * Solo debe usarse desde [TokenAuthenticator].
     *
     * @return El refresh token descifrado, o null si no hay sesión activa.
     */
    fun getRefreshTokenSync(): String? = runCatching {
        runBlocking {
            refreshTokenFlow.first()
        }
    }.getOrNull()

    // ── Cifrado / Descifrado ─────────────────────────────────────────────────

    /**
     * Cifra un texto plano con AES-GCM usando la clave del Keystore.
     *
     * AES-GCM (Galois/Counter Mode) es un modo de cifrado **autenticado**:
     * además de cifrar los datos, produce un tag de autenticación que detecta
     * cualquier modificación del ciphertext. Si alguien altera los bytes
     * cifrados en disco, el descifrado lanzará una excepción.
     *
     * El IV (Initialization Vector) es generado aleatoriamente por la API
     * de Cipher en cada llamada. Debe guardarse junto al ciphertext para poder
     * descifrar después. No es un secreto (puede estar en claro), pero debe
     * ser único para cada operación de cifrado con la misma clave.
     *
     * Formato del resultado (todo en Base64 para poder guardarlo como String):
     *   Base64( IV[12 bytes] | ciphertext[N bytes] | GCM_tag[16 bytes] )
     *   Nota: el tag GCM lo añade doFinal() al final del ciphertext automáticamente.
     *
     * @param plainText Texto a cifrar (el token JWT en texto plano).
     * @return Cadena Base64 con IV y ciphertext+tag concatenados.
     */
    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val iv         = cipher.iv  // 12 bytes, generados aleatoriamente por el Cipher
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Concatenar IV delante del ciphertext para poder separarlos al descifrar
        val combined = iv + cipherText
        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Descifra un valor producido por [encrypt].
     *
     * @param encryptedBase64 Cadena Base64 producida por [encrypt].
     * @return El token JWT en texto plano.
     * @throws javax.crypto.AEADBadTagException Si el ciphertext fue manipulado.
     * @throws Exception Si la clave no existe o los datos están corruptos.
     */
    private fun decrypt(encryptedBase64: String): String {
        val combined   = Base64.getDecoder().decode(encryptedBase64)

        // Separar el IV (primeros 12 bytes) del resto (ciphertext + tag GCM)
        val iv         = combined.sliceArray(0 until GCM_IV_SIZE)
        val cipherText = combined.sliceArray(GCM_IV_SIZE until combined.size)

        val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )

        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    /**
     * Obtiene la clave AES-GCM del Android Keystore, o la crea si no existe.
     *
     * La clave se genera una única vez y el sistema la guarda de forma segura.
     * En dispositivos con TEE (Trusted Execution Environment) o Secure Element,
     * las operaciones criptográficas se realizan dentro del enclave seguro y
     * el material de la clave nunca se expone en la memoria de la app.
     *
     * Parámetros de la clave:
     * - Propósito: cifrar Y descifrar (ambos necesarios para nuestro uso)
     * - Modo de bloque: GCM
     * - Padding: ninguno (GCM no usa padding)
     * - Tamaño: 256 bits (máxima seguridad para AES)
     * - RANDOMIZED_ENCRYPTION_REQUIRED: true → el IV debe ser aleatorio,
     *   impidiendo que el mismo texto produzca siempre el mismo cifrado
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }

        // Devolver la clave existente si ya fue creada en una ejecución anterior
        keyStore.getKey(KEYSTORE_ALIAS, null)?.let { return it as SecretKey }

        // Primera vez: generar la clave con los parámetros de seguridad
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .also { generator ->
                generator.init(
                    KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
            }
            .generateKey()
    }
}
