package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.utils.Asable

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 18:45
 * @version: 0.0.1
 */
abstract class AnsiEscapeAction<T> : Asable {
    abstract fun focusMode(): Class<out IEscapeMode>

    abstract fun action(executeTarget: T, escapeMode: IEscapeMode, params: List<Any>?)
}