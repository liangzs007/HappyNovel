#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"

log() {
  printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

log "后端单元与集成测试"
(
  cd "$ROOT_DIR/backend"
  ../gradlew test
)

log "后台 Web API 单元测试与生产构建"
(
  cd "$ROOT_DIR/admin-web"
  if [ ! -d node_modules ]; then
    npm ci
  fi
  npm test -- --run src/adminApi.test.ts
  npm run build
)

log "Android 阅读器单元测试与 Debug 构建"
(
  cd "$ROOT_DIR/android-app"
  ./gradlew :app:testDebugUnitTest
  ./gradlew :app:assembleDebug
)

if [ "${RUN_API_SMOKE:-0}" = "1" ]; then
  log "运行后端 API smoke：$API_BASE_URL"
  curl -fsS "$API_BASE_URL/health" >/dev/null
  curl -fsS "$API_BASE_URL/api/app/home" >/dev/null
  curl -fsS "$API_BASE_URL/api/app/ad-config" >/dev/null
  curl -fsS "$API_BASE_URL/api/app/compliance-config" >/dev/null
  curl -fsS -X PUT "$API_BASE_URL/api/admin/compliance/ad-config" \
    -H "Content-Type: application/json" \
    -d '{"enabled":true,"readerBannerEnabled":true,"interstitialEveryChapters":5}' >/dev/null
  device_id="$(curl -fsS -X POST "$API_BASE_URL/api/app/devices/anonymous" | sed -E 's/.*"deviceId":"([^"]+)".*/\1/')"
  curl -fsS -X POST "$API_BASE_URL/api/app/reading-events" \
    -H "Content-Type: application/json" \
    -d "{\"deviceId\":\"$device_id\",\"bookId\":\"book-seed-1\",\"chapterId\":\"chapter-seed-1\",\"percent\":0.5}" >/dev/null
fi

log "MVP smoke 验收完成"
