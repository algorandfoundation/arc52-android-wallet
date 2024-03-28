package com.algorandfoundation.arc52_android_wallet

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.algorandfoundation.bip32ed25519.Bip32Ed25519Android
import com.algorandfoundation.bip32ed25519.KeyContext
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.toSeed
import com.algorand.algosdk.crypto.Address
import com.algorand.algosdk.transaction.Transaction
import com.algorand.algosdk.util.Encoder
import com.algorand.algosdk.v2.client.common.AlgodClient
import com.algorandfoundation.arc52_android_wallet.databinding.WalletBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Wallet : Fragment() {

    private var _binding: WalletBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: WalletViewModel by viewModels()

    private var bip32Ed25519: Bip32Ed25519Android? = null
    private val algoDClient =
            AlgodClient(
                    "http://10.0.2.2",
                    4001,
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            )

    fun addInputTextWatcher(editText: EditText) {
        editText.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (!viewModel.isProgrammaticChange) {
                            viewModel.isProgrammaticChange = true
                            val cursorPosition = editText.selectionStart
                            val number = parseTextInput(editText)
                            editText.setText(number.toString())
                            val newCursorPosition =
                                    cursorPosition.coerceAtMost(editText.text.length)
                            editText.setSelection(newCursorPosition)
                            viewModel.updateAlgorandAddress(
                                    keyContext =
                                            binding.keyContextSpinner.selectedItem as KeyContext,
                                    numbers =
                                            listOf(
                                                    parseTextInput(binding.accountNumberEditText),
                                                    parseTextInput(binding.changeNumberEditText),
                                                    parseTextInput(binding.keyindexNumberEditText)
                                            ),
                                    bip32Ed25519 = bip32Ed25519
                            )
                            viewModel.isProgrammaticChange = false
                        }
                    }

                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                }
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = WalletBinding.inflate(inflater, container, false)
        setupUI()
        observeViewModel()
        return binding.root
    }

    private fun setupUI() {
        // Initialize UI components

        setupBip39WordsEditText()
        setupGenerateButton()
        setupKeyContextSpinner()
        setupAccountNumberEditText()
        setupChangeNumberEditText()
        setupKeyindexNumberEditText()
        setupSignPostButton()
    }

    private fun setupBip39WordsEditText() {
        val textWatcher =
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val words = s.toString()
                        val m = MnemonicCode(phrase = words)
                        var ok = false
                        try {
                            m.validate()
                            if (m.words.size == 24) {
                                ok = true
                            }
                        } catch (e: Exception) {
                            ok = false
                        }
                        if (ok) {
                            // The seed phrase is valid
                            binding.bip39WordsEditText.backgroundTintList =
                                    ColorStateList.valueOf(Color.GREEN)

                            bip32Ed25519 = Bip32Ed25519Android(m.toSeed())
                            viewModel.updateAlgorandAddress(
                                    keyContext =
                                            binding.keyContextSpinner.selectedItem as KeyContext,
                                    numbers =
                                            listOf(
                                                    parseTextInput(binding.accountNumberEditText),
                                                    parseTextInput(binding.changeNumberEditText),
                                                    parseTextInput(binding.keyindexNumberEditText)
                                            ),
                                    bip32Ed25519 = bip32Ed25519
                            )
                        } else {
                            // The seed phrase is invalid
                            binding.bip39WordsEditText.backgroundTintList =
                                    ColorStateList.valueOf(Color.RED)
                        }
                    }

                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                }

        binding.bip39WordsEditText.addTextChangedListener(textWatcher)
        binding.bip39WordsEditText.post {
            textWatcher.afterTextChanged(binding.bip39WordsEditText.text)
        }
    }

    private fun setupGenerateButton() {
        binding.generateButton.setOnClickListener {
            val generated = MnemonicCode(Mnemonics.WordCount.COUNT_24)
            val wordsAsString = generated.joinToString(" ")
            binding.bip39WordsEditText.setText(wordsAsString)
        }
    }

    private fun setupKeyContextSpinner() {
        val keyContextOptions = KeyContext.values()
        val adapter =
                ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        keyContextOptions
                )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.keyContextSpinner.adapter = adapter
        binding.keyContextSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                    ) {
                        val keyContext = parent.getItemAtPosition(position) as KeyContext
                        viewModel.updateAlgorandAddress(
                                keyContext = keyContext,
                                numbers =
                                        listOf(
                                                parseTextInput(binding.accountNumberEditText),
                                                parseTextInput(binding.changeNumberEditText),
                                                parseTextInput(binding.keyindexNumberEditText)
                                        ),
                                bip32Ed25519 = bip32Ed25519
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
    }

    private fun setupAccountNumberEditText() {
        addInputTextWatcher(binding.accountNumberEditText)
    }

    private fun setupChangeNumberEditText() {
        addInputTextWatcher(binding.changeNumberEditText)
    }

    private fun setupKeyindexNumberEditText() {
        addInputTextWatcher(binding.keyindexNumberEditText)
    }

    private fun setupSignPostButton() {
        binding.signPostButton.setOnClickListener {
            val txId = postSignedTransaction()
            //            if (txId != "Failed") {
            //                val address = Address(bip32Ed25519?.keyGen(KeyContext.Address, 0u, 0u,
            // 0u))
            //                val balance = lookUpAddressBalance(address)
            //                binding.balanceTextView.text = balance.toString()
            //            }
        }
    }

    private fun parseTextInput(input: EditText): Long {
        var number = if (input.text.toString().isEmpty()) 0L else input.text.toString().toLong()
        return number.coerceIn(0L, UInt.MAX_VALUE.toLong())
    }

    private fun observeViewModel() {
        viewModel.algoAddress.observe(viewLifecycleOwner) { address ->
            binding.algorandAddressTextView.text = address
        }
    }

    private fun postSignedTransaction() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val receiverAddress = binding.receiverEditText.text.toString()
                val amount = binding.amountEditText.text.toString().toLong()
                val note = binding.noteEditText.text.toString()

                val tx =
                        Transaction.PaymentTransactionBuilder()
                                .lookupParams(algoDClient) // lookup fee, firstValid, lastValid
                                .sender(
                                        Address(
                                                bip32Ed25519?.keyGen(KeyContext.Address, 0u, 0u, 0u)
                                        )
                                )
                                .receiver(receiverAddress)
                                .amount(amount)
                                .noteUTF8(note)
                                .build()

                val stx = bip32Ed25519?.signAlgoTransaction(KeyContext.Address, 0u, 0u, 0u, tx)
                val stxBytes = Encoder.encodeToMsgPack(stx)

                Log.d("stxBytes", stxBytes.toString())

                val post = algoDClient.RawTransaction().rawtxn(stxBytes).execute()

                if (!post.isSuccessful) {
                    Log.d("error:", "post is not successful")
                    throw RuntimeException("Failed to post transaction")
                }

                // Wait for confirmation
                var done = false
                while (!done) {
                    val txInfo =
                            algoDClient.PendingTransactionInformation(post.body()?.txId).execute()
                    if (!txInfo.isSuccessful) {
                        Log.d("error:", "txInfo is not successful")
                        throw RuntimeException("Failed to check on tx progress")
                    }
                    if (txInfo.body()?.confirmedRound != null) {
                        done = true
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.statusTextView.text = post.body()?.txId as String
                }
            } catch (e: Exception) {
                Log.d("error:", e.toString())
                withContext(Dispatchers.Main) { binding.statusTextView.text = "Failed" }
            }
        }
    }
    private fun lookUpAddressBalance(address: Address): Long {
        val accountInfo = algoDClient.AccountInformation(address).execute()
        if (!accountInfo.isSuccessful) {
            throw RuntimeException("Failed to lookup account")
        }
        return accountInfo.body()?.amount as Long
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class WalletViewModel : ViewModel() {
    val algoAddress = MutableLiveData<String>()
    var isProgrammaticChange = false

    fun updateAlgorandAddress(
            keyContext: KeyContext,
            numbers: List<Long>,
            bip32Ed25519: Bip32Ed25519Android?
    ) {
        // Let's make sure it's not null, which it is at the start
        if (bip32Ed25519 == null) {
            return
        }

        // Produce the PK and turn it into an Algorand formatted address
        val algoAddress =
                Address(
                        bip32Ed25519.keyGen(
                                keyContext,
                                numbers[0].toUInt(),
                                numbers[1].toUInt(),
                                numbers[2].toUInt()
                        )
                )

        this.algoAddress.value = algoAddress.toString()
    }
}
