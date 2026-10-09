#!/usr/bin/env bash
set -euo pipefail
task_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
task_classes=$(mktemp -d)
trap 'rm -rf "$task_classes"' EXIT
javac -encoding UTF-8 -d "$task_classes" \
  "$task_root/topic-service/src/main/java/com/graduation/topic/service/TopicCapacityPolicy.java" \
  "$task_root/topic-service/src/test/java/com/graduation/topic/service/TopicCapacityPolicyChecks.java"
java -cp "$task_classes" com.graduation.topic.service.TopicCapacityPolicyChecks
