#!/usr/bin/env node
'use strict';

// Chỉ gọi GET: FAILOVER_TOKEN là accessToken đăng nhập, không phải JWT_SECRET.
const crypto = require('node:crypto');

function canonical(value) {
  if (Array.isArray(value)) {
    return value.map(canonical).sort((a, b) => JSON.stringify(a).localeCompare(JSON.stringify(b)));
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.keys(value).sort().map(k => [k, canonical(value[k])]));
  }
  return value;
}

function insist(condition, message) {
  if (!condition) throw new Error(message);
}

async function health(url, fetcher) {
  try {
    const r = await fetcher(url, { signal: AbortSignal.timeout(4000) });
    return r.ok && (await r.json()).status === 'UP';
  } catch { return false; }
}

/** Kiểm tra trạng thái cụm; không quản lý hoặc dừng tiến trình. */
async function runChecks({
  phase, token, primary = 'http://localhost:8082', secondary = 'http://localhost:8085',
  gateway = 'http://localhost:8080', samples = 12, pauseMs = 500,
  fetcher = fetch, log = console.log
}) {
  insist(['both', 'secondary', 'restored', 'unavailable'].includes(phase),
    'Phase phải là both, secondary, restored hoặc unavailable.');
  insist(typeof token === 'string' && token.trim(),
    'Thiếu FAILOVER_TOKEN: dùng accessToken đăng nhập, không dùng JWT_SECRET.');
  const up = await Promise.all([
    health(`${primary}/actuator/health`, fetcher),
    health(`${secondary}/actuator/health`, fetcher)
  ]);
  const expected = phase === 'secondary' ? [false, true]
    : phase === 'unavailable' ? [false, false] : [true, true];
  insist(up.every((v, i) => v === expected[i]),
    `Health chưa đúng: 8082=${up[0]}, 8085=${up[1]}.`);
  const ids = new Set();
  let snapshot;
  const count = phase === 'unavailable' ? 3 : samples;
  for (let i = 0; i < count; i++) {
    const start = Date.now();
    let r;
    try {
      r = await fetcher(`${gateway}/api/topics`, {
        headers: { Authorization: `Bearer ${token.trim()}` },
        signal: AbortSignal.timeout(12000)
      });
    } catch {
      throw new Error('Gateway không trả response trong giới hạn 12 giây. Kiểm tra Gateway và profile demo.');
    }
    const elapsed = Date.now() - start;
    if (phase === 'unavailable') {
      insist(r.status >= 500 && r.status < 600, `Mong đợi 5xx, nhận ${r.status}.`);
      log(`${i + 1}/${count}: HTTP ${r.status}, ${elapsed}ms`);
    } else {
      insist(r.status === 200, `Mong đợi 200, nhận ${r.status}. Kiểm tra JWT và service.`);
      const id = r.headers.get('X-Topic-Instance');
      const allowed = phase === 'secondary' ? ['topic-service:8085']
        : ['topic-service:8082', 'topic-service:8085'];
      insist(allowed.includes(id), `X-Topic-Instance không đúng: ${id ?? 'thiếu header'}.`);
      const body = await r.json();
      insist(Array.isArray(body), 'GET /api/topics phải trả mảng đề tài.');
      const digest = crypto.createHash('sha256').update(JSON.stringify(canonical(body))).digest('hex');
      if (snapshot === undefined) snapshot = digest;
      insist(snapshot === digest, 'Dữ liệu khác nhau. Kiểm tra hai bản dùng chung DB và không sửa dữ liệu khi test.');
      ids.add(id);
      log(`${i + 1}/${count}: HTTP 200, ${id}, ${body.length} đề tài, ${elapsed}ms`);
    }
    if (i + 1 < count && pauseMs > 0) await new Promise(resolve => setTimeout(resolve, pauseMs));
  }
  if (phase === 'both' || phase === 'restored') {
    insist(ids.size === 2, 'Chỉ thấy một bản. Kiểm tra hai UP trên Eureka rồi chờ Gateway cập nhật.');
  }
  log(`PASS ${phase}: ${count} request GET đã đạt.`);
  return { phase, health: up, instances: [...ids].sort(), requests: count };
}

if (require.main === module) {
  runChecks({ phase: process.argv[2], token: process.env.FAILOVER_TOKEN })
    .then(result => {
      if (process.env.FAILOVER_REPORT) require('node:fs').writeFileSync(process.env.FAILOVER_REPORT,
        JSON.stringify({ ...result, checkedAt: new Date().toISOString() }, null, 2) + '\n');
    })
    .catch(error => { console.error(`FAIL: ${error.message}`); process.exitCode = 1; });
}
module.exports = { runChecks };
