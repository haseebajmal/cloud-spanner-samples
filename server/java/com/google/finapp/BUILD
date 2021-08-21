package(default_visibility = ["//:__subpackages__"])

java_library(
    name = "server_lib",
    srcs = glob(["*.java"]),
    deps = [
        "//proto:protolib_java",
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_cloud_google_cloud_spanner",
        "@maven//:com_google_cloud_google_cloud_spanner_jdbc",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:javax_inject_javax_inject_1",
        "@maven//:org_apache_tomcat_annotations_api",
    ],
)

java_binary(
    name = "server_bin",
    main_class = "com.google.finapp.ServerMain",
    runtime_deps = [":server_lib"],
)
