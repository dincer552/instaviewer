package com.dincer552.instaviewer.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dincer552.instaviewer.R
import com.dincer552.instaviewer.util.LinkValidator

class HomeFragment : Fragment() {

    private var _rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        _rootView = view

        val openButton = view.findViewById<Button>(R.id.open_button)
        openButton?.setOnClickListener {
            val input = view.findViewById<EditText>(R.id.link_input).text.toString().trim()
            if (LinkValidator.isValidLink(input)) {
                navigateToPlayer(input)
            } else {
                Toast.makeText(requireContext(), "Gecerli bir baglanti gir.", Toast.LENGTH_SHORT).show()
            }
        }

        val pasteButton = view.findViewById<Button>(R.id.paste_button)
        pasteButton?.setOnClickListener {
            pasteFromClipboard()
        }

        val downloadButton = view.findViewById<Button>(R.id.direct_download_button)
        downloadButton?.setOnClickListener {
            Toast.makeText(requireContext(), "Dogru indirme henuz uygulanmadi.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(requireContext()).toString()
            val url = LinkValidator.extractUrl(text)
            if (url != null) {
                _rootView?.findViewById<EditText>(R.id.link_input)?.setText(url)
            } else {
                Toast.makeText(requireContext(), "Panoda gecerli bir baglanti bulunamadi.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Pano bos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToPlayer(url: String) {
        val bundle = Bundle().apply { putString("url", url) }
        findNavController().navigate(R.id.action_navigation_home_to_player, bundle)
    }
}