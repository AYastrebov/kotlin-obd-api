plugins {
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.kotlinx.binary.validator) apply false
    alias(libs.plugins.multiplatform).apply(false)

    alias(libs.plugins.caupain)
}