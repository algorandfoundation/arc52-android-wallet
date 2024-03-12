package com.algorandfoundation.arc52_android_wallet

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import bip32ed25519.Bip32Ed25519Android
import bip32ed25519.KeyContext
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.toSeed
import com.algorand.algosdk.crypto.Address
import com.algorandfoundation.arc52_android_wallet.databinding.WalletBinding
import java.lang.Integer.min

class Wallet : Fragment() {

    private var _binding: WalletBinding? = null
    private val binding
        get() = _binding!!

    private var bip32Ed25519: Bip32Ed25519Android? = null

    private val inputTextWatcher =
            object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    updateAlgorandAddress()
                }

                override fun beforeTextChanged(
                        s: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                ) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = WalletBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        // Initialize UI components
        binding.bip39WordsEditText.addTextChangedListener(
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
                            updateAlgorandAddress()
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
        )

        binding.generateButton.setOnClickListener {
            val generated = MnemonicCode(Mnemonics.WordCount.COUNT_24)
            val wordsAsString = generated.joinToString(" ")
            binding.bip39WordsEditText.setText(wordsAsString)
        }

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
                        // Update selected KeyContext
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

        binding.accountNumberEditText.addTextChangedListener(inputTextWatcher)
        binding.changeNumberEditText.addTextChangedListener(inputTextWatcher)
        binding.keyindexNumberEditText.addTextChangedListener(inputTextWatcher)
    }

    private fun parseTextInput(input: EditText): Long {
        var number = if (input.text.toString().isEmpty()) 0L else input.text.toString().toLong()
        return number.coerceIn(0L, UInt.MAX_VALUE.toLong())
    }
    private fun updateAlgorandAddress() {
        val keyContext = binding.keyContextSpinner.selectedItem as KeyContext

        val bindingsTextArray = mutableListOf<EditText>()
        bindingsTextArray.add(binding.accountNumberEditText)
        bindingsTextArray.add(binding.changeNumberEditText)
        bindingsTextArray.add(binding.keyindexNumberEditText)

        // Turn off to prevent infinite loops
        for (binding in bindingsTextArray) {
            binding.removeTextChangedListener(inputTextWatcher)
        }

        val numbers = mutableListOf<Long>()

        // Collect input, parse and potentially clean
        // update, store number, move the cursor
        for (binding in bindingsTextArray) {
            val cursorPosition = binding.selectionStart
            val number = parseTextInput(binding)
            binding.setText(number.toString())
            numbers.add(number)
            binding.setSelection(min(cursorPosition, binding.text.length))
        }

        // Turn on again
        for (binding in bindingsTextArray) {
            binding.addTextChangedListener(inputTextWatcher)
        }

        // Let's make sure it's not null, which it is at the start
        if (bip32Ed25519 == null) {
            return
        }

        // Produce the PK and turn it into an Algorand formatted address
        val algoAddress =
                Address(
                        bip32Ed25519?.keyGen(
                                keyContext,
                                numbers[0].toUInt(),
                                numbers[1].toUInt(),
                                numbers[2].toUInt()
                        )
                )

        binding.algorandAddressTextView.text = algoAddress.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
