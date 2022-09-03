package io.github.acedroidx.danmaku.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import io.github.acedroidx.danmaku.ui.widgets.EditDanmakuProfile
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var mService: DanmakuService
    private var mBound: Boolean = false
    private val homeViewModel: HomeViewModel by viewModels()

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.d("HomeFragment", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()
            mBound = true

            mService.isRunning.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "isRunning: $it")
                if (homeViewModel.isRunning.value != it) {
                    homeViewModel.isRunning.value = it
                }
            }
            mService.isForeground.observe(viewLifecycleOwner) {
                if (homeViewModel.isForeground.value != it) {
                    homeViewModel.isForeground.value = it
                }
            }
            mService.logText.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "mService.logText.observe")
                if (homeViewModel.logText.value != it) {
                    homeViewModel.logText.value = it
                }
            }
            homeViewModel.isRunning.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.isRunning.observe:$it")
                viewLifecycleOwner.lifecycleScope.launch {
                    homeViewModel.danmakuConfig.value?.let { config ->
                        homeViewModel.updateDanmakuData(config)
                    }
                    mService.danmakuData.value = homeViewModel.serviceDanmakuData.value
                    if (mService.isRunning.value != it) {
                        mService.isRunning.value = it
                    }
                }
            }
            homeViewModel.isForeground.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "isForeground:$it")
                if (mService.isForeground.value != it) {
                    mService.isForeground.value = it
                    if (it) DanmakuService.startDanmakuService(context!!)
                }
            }
            homeViewModel.logText.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.logText.observe")
                if (mService.logText.value != it) {
                    mService.logText.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("HomeFragment", "onServiceDisconnected")
            mBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "onCreateView")
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        homeViewModel.getMainProfile()
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        MyComposable()
                    }
                }
            }
        }
    }

    @Composable
    fun MyComposable(viewModel: HomeViewModel = hiltViewModel()) {
        val profile by viewModel.danmakuConfig.observeAsState()
        val text by viewModel.text.observeAsState()
        val logText by viewModel.logText.observeAsState()
        val isForeground by viewModel.isForeground.observeAsState()
        val isRunning by viewModel.isRunning.observeAsState()
        Column {
            text?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            profile?.let {
                EditDanmakuProfile.Profile(profile = it) { p ->
                    viewModel.danmakuConfig.value = p
                    lifecycleScope.launch {
                        viewModel.saveDanmakuConfig(p)
                        viewModel.updateDanmakuData(p)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("启动后台服务", color = MaterialTheme.colorScheme.onBackground)
                isForeground?.let {
                    Switch(
                        checked = it,
                        onCheckedChange = { viewModel.isForeground.value = it })
                }
                Text("发送弹幕", color = MaterialTheme.colorScheme.onBackground)
                isRunning?.let {
                    Switch(
                        checked = it,
                        onCheckedChange = { viewModel.isRunning.value = it })
                }
            }
            Row {
                Button(onClick = { viewModel.clearLog() }) {
                    Text("清除日志")
                }
                Button(onClick = { viewModel.isAddProfile.value = true }) {
                    Text("添加配置")
                }
            }
            Text("输出日志", color = MaterialTheme.colorScheme.onBackground)
            logText?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        val openDialog = viewModel.isAddProfile.observeAsState()
        if (openDialog.value == true) {
            MyAlertDialog()
        }
    }

    @Preview(name = "Light Mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun PreviewCompose() {
        MyAlertDialog()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAlertDialog(viewModel: HomeViewModel = hiltViewModel()) {
        AppTheme {
            var name by remember { mutableStateOf("主页弹幕配置") }
            AlertDialog(
                title = {
                    Text(text = "配置名称")
                },
                text = {
                    TextField(value = name, onValueChange = { name = it })
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addProfile(name)
                        }) {
                        Text("添加")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            viewModel.isAddProfile.value = false
                        }) {
                        Text("取消")
                    }
                },
                onDismissRequest = {
                    viewModel.isAddProfile.value = false
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView")
        activity?.unbindService(connection)
        mBound = false
    }
}