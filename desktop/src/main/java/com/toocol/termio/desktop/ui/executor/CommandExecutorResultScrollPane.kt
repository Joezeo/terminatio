package com.toocol.termio.desktop.ui.executor

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.beans.value.ObservableValue
import javafx.scene.control.ScrollPane
import org.fxmisc.flowless.VirtualizedScrollPane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/11 23:15
 * @version: 0.0.1
 */
class CommandExecutorResultScrollPane(private val id: Long, terminalConsoleTextArea: CommandExecutorResultTextArea) :
    VirtualizedScrollPane<CommandExecutorResultTextArea?>(terminalConsoleTextArea), IStyleAble, IComponent {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-result-scroll-pane"
        )
    }

    override fun initialize() {
        styled()
        vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        val executorPanel = findComponent(CommandExecutorPanel::class.java, id)
        executorPanel.bottom = this
        totalHeightEstimateProperty().addListener { _: ObservableValue<out Double?>?, _: Double?, _: Double? -> content!!.requestFollowCaret() }
    }

    override fun id(): Long {
        return id
    }
}