#!/bin/sh

# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

gcloud config configurations activate emulator
gcloud config set auth/disable_credentials true
gcloud config set project test-project
gcloud config set api_endpoint_overrides/spanner http://localhost:9020/
gcloud spanner instances create test-instance --config=emulator-config --description="Test Instance" --nodes=1
gcloud config set spanner/instance test-instance
gcloud spanner databases create test-database --ddl-file src/main/java/com/google/finapp/schema.sdl
export SPANNER_EMULATOR_HOST="localhost:9010"