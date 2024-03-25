# ARC-0052 Android Wallet Reference Implementation

## Setup

- clone this repository.
- clone https://github.com/algorandfoundation/bip32-ed25519-kotlin
- Navigate into bip32-ed25519-kotlin and run ./initialize.sh, which will setup the repo with the underlying lazysodium-java (Algorand Foundation fork) git submodule, run builds for android and desktop version before putting the outputed files under dist/.
- Copy the .aar file under bip32-ed25519-kotlin/dist/android/.
- Paste it under arc52-android-wallet/app/libs/.
- Open arc52-android-wallet in Android Studio, gradle sync and then run the app

As a bash script:

```bash
git clone git@github.com:algorandfoundation/arc52-android-wallet.git
git clone git@github.com:algorandfoundation/bip32-ed25519-kotlin.git
cd bip32-ed25519-kotlin
./initialize.sh
cd ..
cp bip32-ed25519-kotlin/android/bip32ed25519/build/outputs/aar/*-release.aar arc52-android-wallet/app/libs/
```
