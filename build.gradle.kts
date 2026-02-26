plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // --- INICIO DEL CAMBIO ---
    // 1. Añadir el plugin de Google Services
    // (Asegúrate de que la versión esté en tu libs.versions.toml o usa una explícita)
    // Si no usas TOML para plugins, puedes poner:
    id("com.google.gms.google-services") version "4.4.2" apply false
    // --- FIN DEL CAMBIO ---
}