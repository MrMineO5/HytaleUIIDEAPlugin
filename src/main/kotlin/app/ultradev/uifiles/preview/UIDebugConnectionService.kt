package app.ultradev.uifiles.preview

import app.ultradev.hytaleuiparser.renderer.command.UIDebugClient
import app.ultradev.hytaleuiparser.spec.command.CustomUIInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.EventDispatcher
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

@Service(Service.Level.PROJECT)
class UIDebugConnectionService : Disposable {
    private val scheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "UI Debug Reconnect").apply { isDaemon = true }
        }

    private val disposed = AtomicBoolean(false)
    private val autoReconnect = AtomicBoolean(true)
    private val listeners = EventDispatcher.create(Listener::class.java)
    private val sessionCounter = AtomicInteger(0)
    private val latestPageInfoByKey = ConcurrentHashMap<String, CustomUIInfo>()

    @Volatile
    private var address: String = "127.0.0.1"
    @Volatile
    private var port: Int = 14152

    @Volatile
    private var client: UIDebugClient? = null
    @Volatile
    private var attempt = 0
    @Volatile
    var status: ConnectionStatus = ConnectionStatus.Stopped
        private set

    interface Listener : EventListener {
        fun statusChanged(status: ConnectionStatus) {}
    }

    sealed interface ConnectionStatus {
        data object Stopped : ConnectionStatus
        data class Connecting(val address: String, val port: Int) : ConnectionStatus
        data class Connected(val address: String, val port: Int) : ConnectionStatus
        data class Reconnecting(val address: String, val port: Int, val attempt: Int, val delayMs: Long) :
            ConnectionStatus

        data class Disconnected(val address: String, val port: Int) : ConnectionStatus
    }

    fun updateEndpoint(socketAddress: String) {
        val split = socketAddress.lastIndexOf(':')
        address = socketAddress.substring(0, split).trim()
        port = socketAddress.substring(split + 1).trim().toIntOrNull() ?: 14152
    }

    fun start() {
        if (disposed.get()) return
        if (status is ConnectionStatus.Connected || status is ConnectionStatus.Connecting || status is ConnectionStatus.Reconnecting) return

        autoReconnect.set(true)
        attempt = 0
        val session = sessionCounter.incrementAndGet()

        updateStatus(ConnectionStatus.Connecting(address, port))
        tryConnectWithFreshClient(session, address, port)
    }

    fun stop() {
        autoReconnect.set(false)
        sessionCounter.incrementAndGet()
        client?.shutdown()
        client = null
        attempt = 0
        updateStatus(ConnectionStatus.Stopped)
    }

    fun setAutoReconnect(enabled: Boolean) {
        autoReconnect.set(enabled)
    }

    fun addListener(listener: Listener) {
        listeners.addListener(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.removeListener(listener)
    }

    fun canStart(): Boolean {
        return address.isNotBlank() && port > 0 && !disposed.get() && status == ConnectionStatus.Stopped
    }

    fun canStop(): Boolean {
        return !disposed.get() && status != ConnectionStatus.Stopped
    }

    fun getLatestPageInfo(key: String): CustomUIInfo? = latestPageInfoByKey[key]

    private fun tryConnectWithFreshClient(session: Int, address: String, port: Int) {
        scheduler.execute {
            if (!isSessionActive(session)) return@execute
            val c = createClient(session, address, port) ?: return@execute
            try {
                c.connect(address, port)
                if (!isSessionActive(session)) return@execute
                attempt = 0
                thisLogger().info("UIDebugClient connected to $address:$port")
                updateStatus(ConnectionStatus.Connected(address, port))
            } catch (t: Throwable) {
                thisLogger().info("Connect failed: ${t.message}")
                if (client === c) {
                    client = null
                }
                runCatching { c.shutdown() }
                if (!isSessionActive(session)) return@execute
                updateStatus(ConnectionStatus.Disconnected(address, port))
                if (autoReconnect.get()) scheduleReconnect(session, address, port)
            }
        }
    }

    private fun scheduleReconnect(session: Int, address: String, port: Int) {
        val delay = 1000L shl min(attempt, 5)
        attempt++
        updateStatus(ConnectionStatus.Reconnecting(address, port, attempt, delay))

        scheduler.schedule({
            if (!isSessionActive(session)) return@schedule
            if (!autoReconnect.get()) return@schedule
            updateStatus(ConnectionStatus.Connecting(address, port))
            tryConnectWithFreshClient(session, address, port)
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun createClient(session: Int, address: String, port: Int): UIDebugClient? {
        if (!isSessionActive(session)) return null

        val c = UIDebugClient()
        c.listen { info ->
            if (client !== c) return@listen
            if (info.type != CustomUIInfo.Type.Page || !info.clear) return@listen
            latestPageInfoByKey[info.key] = info
        }
        c.listenDisconnect {
            if (!isSessionActive(session)) return@listenDisconnect
            if (client !== c) return@listenDisconnect
            thisLogger().info("UIDebugClient disconnected")
            updateStatus(ConnectionStatus.Disconnected(address, port))
            if (autoReconnect.get()) {
                scheduleReconnect(session, address, port)
            }
        }

        val previous = client
        client = c
        runCatching { previous?.shutdown() }
        return c
    }

    private fun isSessionActive(session: Int): Boolean {
        return !disposed.get() && sessionCounter.get() == session
    }

    private fun updateStatus(newStatus: ConnectionStatus) {
        status = newStatus
        listeners.multicaster.statusChanged(newStatus)
    }

    override fun dispose() {
        disposed.set(true)
        runCatching { stop() }
        runCatching { scheduler.shutdownNow() }
    }
}