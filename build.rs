fn main() {
        glib_build_tools::compile_resources(
        "src/resources",
        "src/resources/resources.gresource.xml",
        "temio_community.gresource",
    );
}