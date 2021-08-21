java_library(
    name = "tests",
    srcs = glob(["*.java"]),
    deps = [
        "//proto:protolib_java",
        "//server/java/com/google/finapp:server_lib",
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_cloud_google_cloud_spanner",
        "@maven//:com_google_cloud_google_cloud_spanner_tests",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_junit_jupiter_junit_jupiter_params",
    ],
)

java_test(
    name = "fin_app_it",
    size = "large",
    srcs = glob(["FinAppIT.java"]),
    data = ["//schema:schema.sdl"],
    test_class = "com.google.finapp.FinAppIT",
    jvm_flags = [
        "-Dspanner.testenv.config.class=com.google.cloud.spanner.GceTestEnvConfig",
        "-Dspanner.testenv.instance=projects/test-project/instances/test-instance"
    ],
    deps = [
        "//proto:protolib_java",
        "//server/java/com/google/finapp:server_lib",
        "@maven//:com_google_cloud_google_cloud_core",
        "@maven//:com_google_cloud_google_cloud_spanner",
        "@maven//:com_google_cloud_google_cloud_spanner_tests",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_junit_jupiter_junit_jupiter_params",
    ],
)

test_suite(
    name = "all_tests",
    tests = [
        "fin_app_it",
    ],
)
