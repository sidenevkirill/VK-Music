package com.flaco_music.ui.screens

import android.annotation.SuppressLint
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.flaco_music.R
import com.flaco_music.broadcast.NotificationActionService
import com.flaco_music.databinding.ActivityMainBinding
import com.flaco_music.service.PlayerService
import com.flaco_music.ui.dialogs.artist_options.ArtistOptionsBottomSheetDialog
import com.flaco_music.ui.dialogs.playlist_options.PlaylistOptionsBottomSheetDialog
import com.flaco_music.ui.dialogs.track_options.TrackOptionsBottomSheetDialog
import com.flaco_music.ui.screens.explore.ExploreFragment
import com.flaco_music.ui.screens.home.HomeFragment
import com.flaco_music.ui.screens.my_music.MyMusicFragment
import com.flaco_music.ui.screens.player.OnPageChangeCallback
import com.flaco_music.ui.screens.player.PlayerImagesViewPagerAdapter
import com.flaco_music.utils.CoverResolutionHelper
import com.flaco_music.utils.RateDialogManager
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.new_features_dialog.NextReleaseDialog
import com.flaco_music.utils.options.OptionsCallbackListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import java.util.*


@KoinApiExtension
class MainActivity : AppCompatActivity() {

    private var onPageChangeCallback: OnPageChangeCallback? = null

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()

    private val snackbarManager: SnackbarManager by inject()
    private val rateDialogManager: RateDialogManager by inject()

    private val navHostFragment: NavHostFragment
        get() = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            when (intent?.extras?.getString(NotificationActionService.KEY_ACTION_NAME)) {
                PlayerService.ACTION_PLAY,
                PlayerService.ACTION_PAUSE -> viewModel.onPlayOrPauseBroadcastReceived()
                PlayerService.ACTION_PREVIOUS -> viewModel.onPreviousBroadcastReceived()
                PlayerService.ACTION_NEXT -> viewModel.onNextBroadcastReceived()
                PlayerService.ACTION_PROGRESS_CHANGED -> viewModel.onProgressChangedBroadcastReceived(intent)
                PlayerService.ACTION_SETUP_TRACK_DATA -> viewModel.onSetupTrackDataBroadcastReceived(intent)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${Locale.getDefault().language}")

        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerReceiver()
        setupBottomNavigation()
        observeViewModel()

        binding.playerBottomControlView.playerBottomControlView.setOnClickListener {
            showPlayer()
        }

        binding.playerView.nameText.isSelected = true

        binding.playerView.playImage.onClick {
            viewModel.sendBroadcastPlay()
        }

        binding.playerView.previousImage.onClick {
            viewModel.sendBroadcastPrevious()
        }

        binding.playerView.nextImage.onClick {
            viewModel.sendBroadcastNext()
        }

        binding.playerBottomControlView.playImage.onClick {
            viewModel.sendBroadcastPlay()
        }

        binding.playerView.addTrackImage.onClick {
            viewModel.addOrRemoveTrack(
                onFailure = { message ->
                    Snackbar.make(
                        findViewById(R.id.navHostFragment),
                        message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            )
        }

        binding.playerView.optionsImage.onClick {
            val currentPlayingTrack = viewModel.currentPlayingTrack ?: return@onClick
            openTrackOptionsBottomSheetDialog(currentPlayingTrack.id, currentPlayingTrack.ownerId)
        }

        binding.playerView.shuffleImage.onClick {
            viewModel.toggleShuffleMode()
        }

        binding.playerView.repeatModeImage.onClick {
            viewModel.toggleRepeatMode()
        }

        binding.playerBottomControlView.optionsImage.onClick {
            val currentPlayingTrack = viewModel.currentPlayingTrack ?: return@onClick
            openTrackOptionsBottomSheetDialog(currentPlayingTrack.id, currentPlayingTrack.ownerId)
        }

        binding.playerView.trackProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.changeTrackProgress(seekBar?.progress ?: 0, true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.changeTrackProgress(seekBar?.progress ?: 0, false)
            }
        })

        binding.playerBottomControlView.trackProgressBar.setOnTouchListener { _, _ -> true }

        binding.bottomNavigationView.menu.forEach {
            val view = binding.bottomNavigationView.findViewById<View>(it.itemId)
            view.setOnLongClickListener {
                true
            }
        }

        snackbarManager.rootView = findViewById(android.R.id.content)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        stopService(Intent(this, PlayerService::class.java))
    }

    override fun onBackPressed() {
        if (binding.root.currentState == R.id.playerViewVisible) {
            hidePlayer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (viewModel.isPlayerActivatedLiveData.value == true) {
            showPlayer()
        }
    }

    fun setBackgroundColor(color: Int) {
        binding.motionLayout.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun openTrackOptionsBottomSheetDialog(
        trackId: Int,
        ownerId: Int,
        playlistId: Int? = null,
        listener: OptionsCallbackListener? = null
    ) {
        val dialog = TrackOptionsBottomSheetDialog(
            trackId = trackId,
            ownerId = ownerId,
            playlistId = playlistId,
            listener = listener
        )
        dialog.show(supportFragmentManager, TrackOptionsBottomSheetDialog.TAG)
    }

    fun openPlaylistOptionsBottomSheetDialog(
        playlistId: Int,
        ownerId: Int,
        listener: OptionsCallbackListener? = null
    ) {
        val dialog = PlaylistOptionsBottomSheetDialog(
            playlistId = playlistId,
            ownerId = ownerId,
            listener = listener
        )
        dialog.show(supportFragmentManager, TrackOptionsBottomSheetDialog.TAG)
    }

    fun openArtistOptionsBottomSheetDialog(artistId: String) {
        val dialog = ArtistOptionsBottomSheetDialog(
            artistId = artistId,
        )
        dialog.show(supportFragmentManager, TrackOptionsBottomSheetDialog.TAG)
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(NotificationActionService.INTENT_FILTER)
        registerReceiver(receiver, intentFilter)
    }

    private fun setupBottomNavigation() {
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener {
            val fragment = navHostFragment.childFragmentManager.fragments[0]
            if (!(fragment is HomeFragment
                        || fragment is ExploreFragment
                        || fragment is MyMusicFragment)
            ) {
                NavigationUI.onNavDestinationSelected(it, navController)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.observeCurrentPlaylist { currentPlaylist ->
            val imagesUrls = currentPlaylist.map { CoverResolutionHelper.getCoverResolution(it) }
            setupCoversViewPager(imagesUrls)
        }

        viewModel.observeRepeatMode { repeatMode ->
            val imageResId = when (repeatMode) {
                ExoPlayer.REPEAT_MODE_OFF -> R.drawable.exo_controls_repeat_off
                ExoPlayer.REPEAT_MODE_ALL -> R.drawable.exo_controls_repeat_all
                ExoPlayer.REPEAT_MODE_ONE -> R.drawable.exo_controls_repeat_one
                else -> throw Exception("Unknown player repeat mode.")
            }
            val imageTintColorId = when (repeatMode) {
                ExoPlayer.REPEAT_MODE_ALL,
                ExoPlayer.REPEAT_MODE_ONE -> R.color.primary
                ExoPlayer.REPEAT_MODE_OFF -> R.color.icon_tint
                else -> throw Exception("Unknown player repeat mode.")
            }
            binding.playerView.repeatModeImage.setImageResource(imageResId)
            binding.playerView.repeatModeImage.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, imageTintColorId))
        }

        viewModel.observeShuffleMode { isShuffle ->
            val imageResId = if (isShuffle) R.drawable.exo_controls_shuffle_on else R.drawable.exo_controls_shuffle_off
            val imageTintColorId = if (isShuffle) {
                R.color.primary
            } else {
                R.color.icon_tint
            }
            binding.playerView.shuffleImage.setImageResource(imageResId)
            binding.playerView.shuffleImage.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, imageTintColorId))
        }

        viewModel.viewPagerPositionLiveData.observe(this) { position ->

            onPageChangeCallback?.let {
                binding.playerView.coversViewPager.unregisterOnPageChangeCallback(it)
            }

            try {
                val currentPlayingTrack = viewModel.currentPlayingTrack ?: return@observe

                viewModel.observeIsTrackInUserAudios { isExists ->
                    val imageResId = if (isExists) {
                        R.drawable.ic_baseline_done_24
                    } else {
                        R.drawable.ic_baseline_add_24
                    }
                    binding.playerView.addTrackImage.setImageResource(imageResId)
                }

                binding.playerView.nameText.text = currentPlayingTrack.name
                binding.playerBottomControlView.nameText.text = currentPlayingTrack.name

                binding.playerView.artistNameText.text = currentPlayingTrack.artist
                binding.playerBottomControlView.artistNameText.text = currentPlayingTrack.artist

                binding.playerView.coversViewPager.setCurrentItem(position, true)

                onPageChangeCallback = OnPageChangeCallback(position) { action ->
                    when (action) {
                        OnPageChangeCallback.Action.NEXT -> viewModel.sendBroadcastNext()
                        OnPageChangeCallback.Action.PREVIOUS -> viewModel.sendBroadcastPrevious()
                    }
                }

                binding.playerView.coversViewPager.registerOnPageChangeCallback(onPageChangeCallback!!)
            } catch (e: Exception) {
                // do nothing
            }
        }
        viewModel.musicProgressLiveData.observe(this) {
            binding.playerView.trackProgressBar.progress = it.toInt()
            binding.playerBottomControlView.trackProgressBar.progress = it.toInt()
        }
        viewModel.isPlayingLiveData.observe(this) {
            val icon = if (it) R.drawable.exo_controls_pause else R.drawable.exo_controls_play
            binding.playerView.playImage.setImageResource(icon)
            binding.playerBottomControlView.playImage.setImageResource(icon)
        }
        viewModel.isPlayerActivatedLiveData.observe(this) {
            if (it) binding.motionLayout.transitionToState(R.id.playerViewHidden)
        }
        viewModel.showNextReleaseComingDialog.observe(this) {
            if (it) {
                NextReleaseDialog.showNextReleaseIsComingDialog(
                    context = this
                ) {
                    viewModel.setNextReleaseIsComingDialogIsShown()
                }
            }
        }
        viewModel.showNextReleaseAvailableDialog.observe(this) {
            if (it) {
                NextReleaseDialog.showNextReleaseAvailableDialog(
                    context = this,
                    onPositiveClicked = {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                        } catch (e: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        }
                    })
            }
        }
        viewModel.showRateAppDialog.observe(this) {
            if (it) {
                rateDialogManager.show(this)
            }
        }
    }

    private fun setupCoversViewPager(urls: List<String?>) {
        if (urls != (binding.playerView.coversViewPager.adapter as PlayerImagesViewPagerAdapter?)?.items) {
            onPageChangeCallback?.let {
                binding.playerView.coversViewPager.unregisterOnPageChangeCallback(it)
            }

            val oldAdapter = binding.playerView.coversViewPager.adapter as PlayerImagesViewPagerAdapter?
            if ((oldAdapter) != null) {
                if (oldAdapter.items != urls) {
                    binding.playerView.coversViewPager.adapter = PlayerImagesViewPagerAdapter(urls)
                }
            } else {
                binding.playerView.coversViewPager.adapter = PlayerImagesViewPagerAdapter(urls)
            }
        }
    }

    private fun showPlayer() {
        binding.root.transitionToState(R.id.playerViewVisible)
    }

    private fun hidePlayer() {
        binding.root.transitionToState(R.id.playerViewHidden)
    }

    companion object {
        private const val TAG = "MainActivity"

        fun startMe(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}