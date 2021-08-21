package(default_visibility = ["//:__subpackages__"])

load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_proto_library")
load("@com_github_grpc_grpc//bazel:cc_grpc_library.bzl", "cc_grpc_library")

proto_library(
    name = "protos",
    srcs = glob(["*.proto"]),
    deps = [],
)

java_grpc_library(
    name = "protolib_java",
    protos = [":protos"],
)


cc_proto_library(
    name = "protolib_cc",
    deps = [":protos"],
)

cc_grpc_library(
    name = "protolib_cc_grpc",
    srcs = [":protos"],
    grpc_only = True,
    deps = [":protolib_cc"],
)