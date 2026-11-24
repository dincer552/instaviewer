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
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rootView = view
        view.findViewById<Button>(R.id.open_button)?.setOnClickListener { navigateFromInput() }
        view.findViewById<Button>(R.id.paste_button)?.setOnClickListener { pasteFromClipboard() }
        view.findViewById<Button>(R.id.direct_download_button)?.setOnClickListener { navigateFromInput() }
        return view
    }

    private fun navigateFromInput() {
        val input = rootView?.findViewById<EditText>(R.id.link_input)?.text?.toString().orEmpty()
        val url = LinkValidator.normalizeUrl(input)
        if (url != null) navigateToPlayer(url)
        else Toast.makeText(requireContext(), "Geçerli bir desteklenen bağlantı gir.", Toast.LENGTH_SHORT).show()
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(requireContext()).toString()
            val url = LinkValidator.extractUrl(text)
            if (url != null) rootView?.findViewById<EditText>(R.id.link_input)?.setText(url)
            else Toast.makeText(requireContext(), "Panoda geçerli bir bağlantı bulunamadı.", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(requireContext(), "Pano boş.", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToPlayer(url: String) {
        val normalizedUrl = LinkValidator.normalizeUrl(url) ?: return
        findNavController().navigate(R.id.action_navigation_home_to_player, Bundle().apply { putString("url", normalizedUrl) })
    }

    override fun onDestroyView() {
        rootView = null
        super.onDestroyView()
    }
}