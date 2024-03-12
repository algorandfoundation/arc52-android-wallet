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
import androidx.fragment.app.Fragment
import bip32ed25519.Bip32Ed25519Android
import bip32ed25519.KeyContext
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.toSeed
import com.algorand.algosdk.crypto.Address
import com.algorandfoundation.arc52_android_wallet.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding
        get() = _binding!!

    private var bip32Ed25519: Bip32Ed25519Android? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
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
                        var ok = true
                        try {
                            m.validate()
                            if (m.words.size != 24) {
                                ok = false
                            }
                        } catch (e: Exception) {
                            ok = false
                        }

                        if (ok) {
                            // The seed phrase is valid
                            binding.bip39WordsEditText.backgroundTintList =
                                    ColorStateList.valueOf(Color.GREEN)

                            val a = Bip32Ed25519Android(m.toSeed())
                            val pk = a.keyGen(KeyContext.Address, 0u, 0u, 0u)
                            val sti = Address(pk).toString()
                            Log.d("pk", sti)
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

        // Set text watchers on the other EditText fields to update the corresponding values
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
