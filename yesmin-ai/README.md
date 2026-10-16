# Yesmin AI (Android)

Native Kotlin starter for the Yesmin AI requirements in the supplied project brief.

## Included in this first implementation
- Native Android app, package `com.yesmin.ai`
- Dark futuristic Compose dashboard and chat screen
- Gemini REST chat client with user-supplied API key (not hardcoded)
- Settings dialog to enter/change the key; key stored in Android private preferences
- Hindi/Hinglish/English system instruction
- Microphone permission declaration and clear roadmap for voice/automation work

## Important limitations
This is an initial foundation, not the full requested product. Gemini chat is implemented; continuous Gemini Live calling, persistent Room memory, app automation, notification reading, OCR, and coding mode are not yet implemented. No buttons should be interpreted as proof that those features work. The API key stored in app preferences is private to the app but is not encrypted by this starter; do not use a production API key in a distributed APK. For production, use a backend that keeps credentials server-side or implement an audited encrypted-storage approach. Never commit API keys.

## Build in Termux
Install a current Termux from a trusted source, then:
```sh
pkg update && pkg upgrade
pkg install openjdk-17 gradle android-tools git unzip
cd ~/storage/downloads
git clone https://github.com/rfahmed7350/Private-agent-6.git
cd Private-agent-6/yesmin-ai
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```
If `~/storage/downloads` is unavailable, run `termux-setup-storage` and accept the Android prompt. Android SDK command-line tools and platform 35 must also be installed/configured for your Termux environment; availability varies by device and repository. Set `ANDROID_HOME` to the SDK path and install platform/build-tools 35 where supported. This workflow has not been tested on every phone.

APK output: `app/build/outputs/apk/debug/app-debug.apk`. Install with `termux-open app/build/outputs/apk/debug/app-debug.apk` or `adb install ...` when debugging is configured.

## Gemini
Create an API key through Google AI Studio, then enter it in Settings in the app. The current model ID is configurable in source (`gemini-2.5-flash`) and may need changing based on account availability. API usage may incur quota or billing limits. The client uses the documented Gemini generateContent REST endpoint; it is not a Gemini Live audio connection.
