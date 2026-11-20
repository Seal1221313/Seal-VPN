package org.torproject.android.ui.more

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.torproject.android.R
import org.torproject.android.databinding.DiagnosticsBottomSheetBinding
import org.torproject.android.service.OrbotService
import org.torproject.android.ui.OrbotBottomSheetDialogFragment
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

class DiagnosticsBottomSheet : OrbotBottomSheetDialogFragment() {

    private var _binding: DiagnosticsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private data class IpInfo(val isTor: Boolean, val ip: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DiagnosticsBottomSheetBinding.inflate(layoutInflater)
        binding.btnRun.setOnClickListener { runChecks() }
        binding.btnBrowser.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CHECK_PAGE)))
            } catch (_: ActivityNotFoundException) {
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        runChecks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun runChecks() {
        val notConnected = getString(R.string.diagnostics_not_connected)
        binding.tvReport.text = getString(R.string.diagnostics_running)
        viewLifecycleOwner.lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) { buildReport(notConnected) }
            _binding?.tvReport?.text = report
        }
    }

    private fun buildReport(notConnected: String): String {
        val socks = OrbotService.mPortSOCKS
        if (socks <= 0) return notConnected

        val hops = OrbotService.fetchCircuitPath()
        val viaTor = fetchIp(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", socks)))
        val direct = fetchIp(Proxy.NO_PROXY)

        val verdict = when {
            viaTor == null -> "check failed"
            !viaTor.isTor -> "FAIL: exit is not a Tor relay"
            direct != null && direct.ip == viaTor.ip -> "n/a: this app is routed through the tunnel"
            else -> "ok"
        }

        return buildString {
            append("CIRCUIT\n")
            append(if (hops.isNullOrEmpty()) "n/a" else "you > " + hops.joinToString(" > "))
            append("\n\n")
            append(row("TOR", when (viaTor?.isTor) {
                true -> "yes"
                false -> "no"
                null -> "unreachable"
            }))
            append(row("EXIT IP", viaTor?.ip ?: "-"))
            append(row("DEVICE IP", direct?.ip ?: "-"))
            append(row("RESULT", verdict))
            append("\n")
            append(getString(R.string.diagnostics_note))
        }
    }

    private fun row(label: String, value: String) = label.padEnd(11) + value + "\n"

    private fun fetchIp(proxy: Proxy): IpInfo? = try {
        val connection = URL(CHECK_API).openConnection(proxy) as HttpURLConnection
        connection.connectTimeout = 20000
        connection.readTimeout = 20000
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        val json = JSONObject(body)
        IpInfo(json.optBoolean("IsTor"), json.optString("IP"))
    } catch (_: Exception) {
        null
    }

    companion object {
        const val TAG = "DiagnosticsBottomSheet"
        private const val CHECK_API = "https://check.torproject.org/api/ip"
        private const val CHECK_PAGE = "https://check.torproject.org/"

        fun show(fragmentManager: FragmentManager) {
            DiagnosticsBottomSheet().show(fragmentManager, TAG)
        }
    }
}
