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
cp bip32-ed25519-kotlin/build/*-release.aar arc52-android-wallet/app/libs/
```

## Send a Transaction

Note that the default seed `exact remain north lesson program series excess lava material second riot error boss planet brick rotate scrap army riot banner adult fashion casino bamboo` should NOT be used in production.

However, for testing the app, this seed produces the Algorand address `I7V63MENRB7L4K53PQGYRFQFI7ZWXD3N53XIGP5THNNT6BSAYWBFYGX4DE`. Funding it in your default local algokit sandbox environment (the default algod this app references too) should allow the app to make a successful transaction. If you don't fund it the app will simply return fail rather than providing transaction id.
