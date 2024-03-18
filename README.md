# ARC-0052 Android Wallet Reference Implemntation

## Setup

- clone https://github.com/algorandfoundation/arc52-android-wallet
- clone https://github.com/algorandfoundation/bip32-ed25519-kotlin
- Navigate into bip32-ed25519-kotlin and run ./initialize.sh, which will setup the repo with the underlying lazysodium-java (Algorand Foundation fork) git submodule.
- Run ./gradlew build. This will produce a .jar file.
- Create `libs` folder under app: `arc52-android-wallet/app/libs`
- In bip32-ed25519-kotlin copy the following .jar files into arc52-android-wallet/app/libs: 1) bip32ed25519/build/libs/bip32ed25519-0.1.0.jar, 2) bip32ed25519/libs/lazysodium-java-5.1.5.jar
- Copy over the Android binaries from lazysodium-java under src/main/jniLibs into the jniLibs in arc52-android-wallet.
- Open arc52-android-wallet in Android Studio, gradle sync and then run the app

As a bash script:

```bash
git clone git@github.com:algorandfoundation/arc52-android-wallet.git
git clone git@github.com:algorandfoundation/bip32-ed25519-kotlin.git
cd bip32-ed25519-kotlin
./initialize.sh
./gradlew build
cd ..
mkdir arc52-android-wallet/app/libs
cp bip32-ed25519-kotlin/bip32ed25519/build/libs/bip32ed25519-0.1.0.jar arc52-android-wallet/app/libs/
cp bip32-ed25519-kotlin/bip32ed25519/libs/lazysodium-java-5.1.5.jar arc52-android-wallet/app/libs/
cp -r bip32-ed25519-kotlin/lazysodium-java/src/main/jniLibs/* arc52-android-wallet/app/src/main/jniLibs/
```
