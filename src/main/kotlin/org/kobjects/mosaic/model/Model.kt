package org.kobjects.mosaic.model

import org.kobjects.mosaic.json.JsonParser
import org.kobjects.mosaic.model.builtin.BuiltinFunctions
import org.kobjects.mosaic.pluginapi.*
import org.kobjects.mosaic.plugins.homeassistant.HomeAssistantPlugin
import org.kobjects.mosaic.plugins.pi4j.Pi4jPlugin
import org.kobjects.mosaic.svg.SvgManager
import java.io.File
import java.io.FileWriter
import org.kobjects.mosaic.tomson.TomsonParser
import java.io.Writer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

object Model : ModelInterface {

    val STORAGE_FILE = File("storage/data.tc")

    var modificationTag: Long = 0

    /* Can't be a proper property as the setter needs a modification token. */
    private var simulationMode_: Boolean = false
    private var pendingUpdates = mutableListOf<(ModificationToken)->Unit>()

    var runMode_: Boolean = false
    var settingsTag: Long = 0

    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))

    val updateListeners = mutableSetOf<UpdateListenerData>()

    val plugins = mutableListOf<Plugin>()

    val functions = Functions()
    val factories = Factories()
    val ports = Ports()
    val integrations = Integrations()

    val svgs = SvgManager(File("src/main/resources/static/img"))

    val restValues = mutableMapOf<String, Any?>()
    var refreshRequested: Boolean = false

    private val lock = ReentrantLock()

    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin(this))
        addPlugin(svgs)
        addPlugin(HomeAssistantPlugin(this))
        // addPlugin(MqttPlugin)

        applySynchronizedWithToken { runtimeContext ->
            runtimeContext.loading = true
            var fileData = ""
            try {
                fileData = STORAGE_FILE.readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadData(fileData, runtimeContext)
        }
    }

    override fun addUpdateListener(permanent: Boolean, onChangeOnly: Boolean, listener: (modificationTag: Long, anyChanged: Boolean) -> Unit) {
        updateListeners.add(UpdateListenerData(permanent = permanent, onChangeOnly = onChangeOnly, listener = listener))
    }


    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (spec in plugin.operationSpecs) {
            when (spec) {
                is FunctionSpec -> functions.add(spec)
                is AbstractFactorySpec -> factories.add(spec)
                else -> throw IllegalArgumentException("Function or factory expected; got $spec (${spec::class.simpleName})")
            }
        }
    }

    override val simulationMode: Boolean
        get() = simulationMode_

    fun setSimulationMode(enabled: Boolean, token: ModificationToken) {
        simulationMode_ = enabled
        settingsTag = token.tag

        if (enabled) {
            for (port in ports) {
                port.notifySimulationModeChanged(token)
            }
            for (integration in integrations) {
                integration.notifySimulationModeChanged(token)
            }
            for (plugin in plugins) {
                plugin.notifySimulationModeChanged(token)
            }
        } else {
            for (plugin in plugins) {
                plugin.notifySimulationModeChanged(token)
            }
            for (integration in integrations) {
                integration.notifySimulationModeChanged(token)
            }
            for (port in ports) {
                port.notifySimulationModeChanged(token)
            }

        }

    }

    fun setRunMode(value: Boolean, token: ModificationToken) {
        runMode_ = value
        settingsTag = token.tag
    }



    fun loadData(data: String, token: ModificationToken) {
        try {
            val toml = TomsonParser.parse(data)
            for ((key, map) in toml) {
                if (key.isEmpty()) {
                    setSimulationMode(map["simulationMode"] as Boolean? ?: false, token)
                    setRunMode(map["runMode"] as Boolean? ?: false, token)
                } else if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                    val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                    val sheet = Sheet(name)
                    sheets[name] = sheet
                    sheet.parseToml(map, token)
                } else if (key == "ports") {
                    for ((name, value) in map) {
                        try {
                            ports.definePort(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if (key == "integrations") {
                    for ((name, value) in map) {
                        try {
                            integrations.configureIntegration(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            System.err.println("Failed to load integration '$name'.")
                            e.printStackTrace()
                        }
                    }
                } else if (key == "simulationValues") {
                    for((key, value) in map) {
                        val port = ports[key]
                        if (port is InputPortHolder) {
                            port.simulationValueChanged(token, value)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    fun getOrCreate(name: String): Cell {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        return sheet.getOrCreateCell(name.substring(cut + 1))
    }

    fun serialize(writer: Writer, forClient: Boolean = false, tag: Long = -1) {
        if (settingsTag > tag) {
            writer.write("simulationMode = $simulationMode_\n")
            writer.write("runMode = $runMode_\n")
        }
        integrations.serialize(writer, forClient, tag)

        if (forClient) {
            writer.write(factories.serialize(tag))
            writer.write(functions.serialize(tag))
        }

        ports.serialize(writer, forClient, tag)

        writer.write("\n")

        for (sheet in sheets.values) {
            writer.write(sheet.serialize(tag, forClient))
            writer.write("\n")
        }
    }


    fun save() {
        STORAGE_FILE.parentFile.mkdirs()
        val writer = FileWriter(STORAGE_FILE)
        serialize(writer)
        writer.close()
    }


    fun updateSheet(name: String?, jsonSpec: Map<String, Any?>, token: ModificationToken) {
        val previousName = jsonSpec["previousName"] as? String?

        if (!name.isNullOrBlank()) {
            if (name != previousName) {
                val newSheet = Sheet(name, token.tag)
                sheets[name] = newSheet
                token.symbolsChanged = true
                if (!previousName.isNullOrBlank()) {
                    val oldSheet = sheets[previousName]
                    if (oldSheet != null) {
                        for (oldCell in oldSheet.cells.values) {
                            val newCell = newSheet.getOrCreateCell(oldCell.id)
                            newCell.setJson(JsonParser.parseObject(oldCell.toJson()), token)
                        }
                    }
                    sheets[previousName]?.delete(token)
                }
            }

        } else if (!previousName.isNullOrBlank()) {
            sheets[previousName]?.delete(token)
        }
    }


    fun clearAll(modificationToken: ModificationToken) {
        modificationToken.symbolsChanged = true
        modificationToken.formulaChanged = true

        for (key in ports.keys.toList()) {
            ports.deletePort(key, modificationToken)
        }

        for (sheet in sheets.values) {
            sheet.clear(modificationToken)
        }
    }

    fun setSimulationValue(name: String, value: Any?, token: ModificationToken) {
        if (simulationMode_) {
            val port = ports[name]
            if (port is InputPortHolder) {
                port.simulationValueChanged(token, value)
            }
        }
    }


    @OptIn(ExperimentalContracts::class)
    override fun applySynchronizedWithToken(
        callback: ((modificationTag: Long, anyChanged: Boolean) -> Unit)?,
        action: (ModificationToken) -> Unit
    ) {
        applySynchronized {
            val modificationToken = ModificationToken()

            if (callback != null) {
                updateListeners.add(UpdateListenerData(false, false, callback))
            }

            val pendingUpdatesLocal = pendingUpdates
            pendingUpdates = mutableListOf()
            for (update in pendingUpdatesLocal) {
                try {
                    update(modificationToken)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            action(modificationToken)

            if (modificationToken.symbolsChanged) {
                for (port in ports.filterIsInstance<OutputPortHolder>()) {
                    port.reparse()
                }
                for (sheet in sheets.values) {
                    for (cell in sheet.cells.values) {
                        cell.reparse()
                    }
                }
            }

            var anyChanged = modificationToken.symbolsChanged || modificationToken.formulaChanged || settingsTag >= modificationToken.tag
            if (anyChanged) {
                // Other changes are not relevant for saving.
                save()
            }

            if (modificationToken.symbolsChanged) {
                // Mark everything "dirty"
                for (sheet in sheets.values) {
                    for (cell in sheet.cells.values) {
                        modificationToken.addRefresh(cell)
                    }
                }
                for (port in ports.filterIsInstance<OutputPortHolder>()) {
                    modificationToken.addRefresh(port)
                }
            }

            println("Root nodes needing refresh: ${modificationToken.refreshRoots}; other: ${modificationToken.refreshNodes}")

            while (modificationToken.refreshRoots.isNotEmpty()) {
                val current = modificationToken.refreshRoots.first()
                modificationToken.refreshRoots.remove(current)
                // println("Updating: $current")
                try { // Ports may fail if simulation mode is turned off and there is no "real" value
                    if (current.recalculateValue(modificationToken.tag)) {
                        anyChanged = true
                        // println("adding new dependencies: ${current.dependencies}")
                        for (dep in current.outputs) {
                            if (dep.inputs.size == 1) {
                                modificationToken.refreshNodes.remove(dep)
                                modificationToken.refreshRoots.add(dep)
                            } else {
                                modificationToken.refreshNodes.add(dep)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // modificationToken.refreshNodes.addAll(modificationToken.refreshRoots)
            // modificationToken.refreshRoots.clear()

            for (dep in modificationToken.refreshNodes.toList()) {
                modificationToken.addAllDependencies(dep)
            }

            println("Saturated dependencies: ${modificationToken.refreshNodes}")

            while (modificationToken.refreshNodes.isNotEmpty()) {
                for (node in modificationToken.refreshNodes) {
                    var found = true
                    for (dep in node.inputs) {
                        if (modificationToken.refreshNodes.contains(dep)) {
                            found = false
                            break
                        }
                    }
                    if (found) {
                        println("Updating node: $node")
                        modificationToken.refreshNodes.remove(node)
                        if (node.recalculateValue(modificationToken.tag)) {
                            anyChanged = true
                        }
                        break
                    }
                }
            }

            modificationTag = modificationToken.tag
            val removalSet = mutableSetOf<Any>()
            for(listener in updateListeners) {
                if (anyChanged || !listener.onChangeOnly) {
                    listener.listener(modificationTag, anyChanged)
                    if (!listener.permanent) {
                        removalSet.add(listener)
                    }
                }
            }
            updateListeners.removeAll(removalSet)
        }
    }

    fun requestSynchronizedWithToken(action: (ModificationToken) -> Unit) {
        applySynchronized {
            pendingUpdates.add(action)
            requestRefresh()
        }
    }

    fun <T> applySynchronized(action: () -> T) = lock.withLock(action)

    data class UpdateListenerData (
        val permanent: Boolean,
        val onChangeOnly: Boolean,
        val listener: (modificationTag: Long, anyChanged: Boolean) -> Unit
    )

    override fun runAsync(delay: Long, task: () -> Unit) {
        Thread {
            if (delay > 0) {
                Thread.sleep(delay)
            }
            task()
        }.start()
    }

    override fun scheduleAsync(interval: Long, initialDelay: Long, task: () -> Boolean) {
        Thread {
            if (initialDelay > 0) {
                Thread.sleep(initialDelay)
            }
            while (task()) {
                Thread.sleep(interval)
            }
        }.start()
    }

    fun requestRefresh() {
        applySynchronized {
            if (!refreshRequested) {
                refreshRequested = true
                applySynchronizedWithToken {
                    refreshRequested = false
                }
            }
        }
    }

    /** A nullable listener simplifies call sites where the listener is a var and might be null */
    override fun notifyValueChanged(listener: ValueChangeListener?) {
        if (listener != null) {
            requestSynchronizedWithToken {
                listener.notifyValueChanged(it)
            }
        }
    }

    override fun setPortValue(port: InputPortListener, value: Any?) {
        requestSynchronizedWithToken {
            port.portValueChanged(it, value)
        }
    }
}