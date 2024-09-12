# ARC-0052 Android Wallet Reference Implementation

## Setup

- clone this repository.
- clone https://github.com/algorandfoundation/xHD-Wallet-API-kt
- Navigate into xHD-Wallet-API-kt and run ./initialize.sh, which will setup the repo with the underlying lazysodium-java (Algorand Foundation fork) git submodule, run builds for android and desktop version before putting the outputed files under dist/.
- Copy the .aar file under xHD-Wallet-API-kt/dist/android/.
- Paste it under arc52-android-wallet/app/libs/.
- Open arc52-android-wallet in Android Studio, gradle sync and then run the app

As a bash script:

```bash
git clone git@github.com:algorandfoundation/arc52-android-wallet.git
git clone git@github.com:algorandfoundation/xHD-Wallet-API-kt.git
cd xHD-Wallet-API-kt
./initialize.sh
cd ..
cp xHD-Wallet-API-kt/build/*-release.aar arc52-android-wallet/app/libs/
```

## AlgoD

The AlgoD Client parameters have been set to:

```kotlin
private val algoDClient =
        AlgodClient(
                "http://10.0.2.2",
                4001,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        )
```

If you are running this app in an Android emulator on your computer, e.g. through Android Studio, this will correspond to port 4001 on your computer. If you've spun up a a localnet/sandbox with Algokit it will interact with it.

```
algokit localnet start
```

## Send a Transaction

Note that the default seed `exact remain north lesson program series excess lava material second riot error boss planet brick rotate scrap army riot banner adult fashion casino bamboo` should NOT be used in production.

However, for testing the app, this seed produces the Algorand address `I7V63MENRB7L4K53PQGYRFQFI7ZWXD3N53XIGP5THNNT6BSAYWBFYGX4DE`. Funding it in your environment should allow the app to make a successful transaction and return the transaction id. If you don't fund it the app will simply return fail rather than providing transaction id.
