open module sample.coroutines {
    requires java.base;
    requires java.instrument;
    requires javafx.controls;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.javafx;

    exports com.toocol.coroutines;
}