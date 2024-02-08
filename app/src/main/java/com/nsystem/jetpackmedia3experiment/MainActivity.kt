package com.nsystem.jetpackmedia3experiment

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nsystem.jetpackmedia3experiment.ui.theme.JetpackMedia3ExperimentTheme

class MainActivity : ComponentActivity() {

    private lateinit var player: Player

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val isPlayerInitialized = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMediaController()
        setContent {
            JetpackMedia3ExperimentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isPlayerInitialized.value) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context -> PlayerView(context) },
                            update = { playerView ->
                                playerView.player = player
                                player.setMediaItem(MediaItem.fromUri(MEDIA_URL))
                                player.prepare()
                                player.play()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        MediaController.releaseFuture(controllerFuture)
        player.release()
        super.onDestroy()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()
                isPlayerInitialized.value = true
            },
            MoreExecutors.directExecutor()
        )
    }

    companion object {

        private const val MEDIA_URL =
            "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
    }
}