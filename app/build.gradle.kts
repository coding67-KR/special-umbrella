plugins {
    id("com.android.application")
}

android {
    namespace = "com.coding67.kkeutmal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.coding67.kkeutmal"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

// GitHub 저장소 루트의 실제 txt.txt를 APK의 assets/txt.txt로 자동 포함한다.
// 따라서 사전을 바꿀 때 app 폴더 안에 복사본을 따로 만들 필요가 없다.
val dictionaryFile = rootProject.file("txt.txt")
val assetsDir = layout.projectDirectory.dir("src/main/assets")

if (dictionaryFile.exists()) {
    tasks.register<Copy>("syncDictionary") {
        from(dictionaryFile)
        into(assetsDir)
        rename { "txt.txt" }
    }

    tasks.matching { it.name.contains("pre", ignoreCase = true) && it.name.contains("Build", ignoreCase = true) }
        .configureEach { dependsOn("syncDictionary") }
}
