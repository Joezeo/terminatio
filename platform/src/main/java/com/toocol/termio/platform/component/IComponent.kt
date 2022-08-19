package com.toocol.termio.platform.component

import com.toocol.termio.platform.component.ComponentsContainer.get
import com.toocol.termio.platform.component.ComponentsContainer.put
import com.toocol.termio.utilities.utils.Asable
import com.toocol.termio.utilities.utils.Castable
import javafx.scene.Node

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:44
 */
interface IComponent : Asable, Castable {
    /**
     * Initialize the component.
     */
    fun initialize()

    /**
     * Get current component's id
     */
    fun id(): Long

    /**
     * Find Javafx Node represented by Component by registered id
     *
     * @return Optional<Node>
    </Node> */
    fun <T : IComponent?> findComponent(clazz: Class<T>, id: Long): T {
        return get(clazz, id)
    }

    /**
     * Register the component, so you can invoke [IComponent.findComponent]
     * to get any components have registered by id.
     */
    fun registerComponent(id: Long) {
        put(this.javaClass, id, this)
    }

    /**
     * If the component is subclass of Node, hide this component.
     */
    fun hide() {
        if (this is Node) {
            val node = `as`<Node>()
            node.isManaged = false
            node.isVisible = false
        }
    }

    /**
     * If the component is subclass of Node, hide this component.
     */
    fun show() {
        if (this is Node) {
            val node = `as`<Node>()
            node.isManaged = true
            node.isVisible = true
        }
    }
}