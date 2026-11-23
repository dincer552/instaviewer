package com.dincer552.instaviewer.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dincer552.instaviewer.R
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private var exoPlayer: ExoPlayer? = null
    private var _playerView: PlayerView? = null
    private var _statusText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        _playerView = view.findViewById(R.id.player_view)
        _statusText = view.findViewById(R.id.player_status)
        val pipButton = view.findViewById<ImageButton>(R.id.pip_button)

        val url = arguments?.getString("url")
        if (url != null) {
            _statusText?.text = "Hazirlaniyor: $url"
            initPlayer(url)
        }

        pipButton?.setOnClickListener { enterPictureInPictureMode() }

        return view
    }

    private fun initPlayer(url: String) {
        val player = ExoPlayer.Builder(requireContext()).build()
        player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true

        exoPlayer = player
        _playerView?.player = player
        _statusText?.text = "Oynatiliyor..."
    }

    private fun enterPictureInPictureMode() {
        activity?.enterPictureInPictureMode(
            android.app.PictureInPictureParams.Builder().build()
        )
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        _playerView?.player = null
        super.onDestroyView()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }
}