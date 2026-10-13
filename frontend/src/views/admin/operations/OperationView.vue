<script setup>
import pin from '../../../assets/operations/pin.webp'
import duck from '../../../assets/operations/duck.webp'
import rabbit from '../../../assets/operations/rabbit.webp'
import star from '../../../assets/operations/star.webp'
import bottom from '../../../assets/operations/bottom.webp'
const positions = [200, 386, 572, 758, 944, 1130]
const first = [['평동'], ['도산'], ['광주송정역'], ['송정공원'], ['공항'], ['김대중컨벤션센터', '(마륵)']]
const second = [['양동시장'], ['돌고개'], ['농성'], ['화정'], ['쌍촌'], ['운천']]
const third = [['금남로4가'], ['문화전당', '(구도청)'], ['남광주'], ['학동·증심사입구'], ['소태'], ['녹동']]
const stations = [first, second, third].flatMap((row, r) => row.map((lines, i) => ({
  lines, x: positions[i], y: 170 + r * 150,
  color: r === 0 && i === 0 ? '#0078d7' : r === 2 && i === 4 ? '#9700cc' : r === 2 && i === 5 ? '#159b39' : null,
})))
</script>
<template>
  <section class="operations-page" aria-labelledby="operations-title">
    <header class="operations-heading">
      <div><h1 id="operations-title">노선도</h1><p class="operations-description">노선과 역 정보를 확인하고 운행 현황을 한눈에 살펴볼 수 있습니다.</p></div>
      <img class="destination-legend" :src="pin" alt="목적지 범례: 평동행, 소태행, 녹동행" />
    </header>
    <div class="route-scroll" tabindex="0" role="region" aria-label="광주 도시철도 1호선 노선도, 좁은 화면에서 가로 스크롤 가능">
      <svg v-once class="operation-route" viewBox="0 0 1400 565" role="img" aria-labelledby="route-title route-description">
        <title id="route-title">광주 도시철도 1호선</title>
        <desc id="route-description">평동에서 녹동까지 20개 역을 연결한 노선도. 열차 위치와 실시간 운행 정보는 표시하지 않습니다.</desc>
        <path class="rail" d="M200 170 H1145 A75 75 0 0 1 1220 245 A75 75 0 0 1 1145 320 H175 A75 75 0 0 0 100 395 A75 75 0 0 0 175 470 H1130" />
        <g v-for="station in stations" :key="station.lines[0]" class="station">
          <circle :cx="station.x" :cy="station.y" r="12" :fill="station.color || '#fff'" :stroke="station.color || '#b4bbc2'" stroke-width="4" />
          <text :x="station.x" :y="station.y + 40" text-anchor="middle"><tspan v-for="(line, index) in station.lines" :key="line" :x="station.x" :dy="index ? 27 : 0">{{ line }}</tspan></text>
        </g>
        <g class="station"><circle cx="1220" cy="245" r="12" fill="white" stroke="#b4bbc2" stroke-width="4" /><text x="1248" y="253">상무</text></g>
        <g class="station"><circle cx="100" cy="395" r="12" fill="white" stroke="#b4bbc2" stroke-width="4" /><text x="100" y="438" text-anchor="middle">금남로5가</text></g>
        <image :href="duck" x="25" y="95" width="120" height="120" preserveAspectRatio="xMidYMid meet" />
        <image :href="rabbit" x="1030" y="25" width="180" height="135" preserveAspectRatio="xMidYMid meet" />
        <image :href="star" x="1250" y="395" width="96" height="128" preserveAspectRatio="xMidYMid meet" />
        <g class="decoration-bubble" transform="translate(0 -30)"><path d="M42 65 H240 Q254 65 254 79 V101 Q254 115 240 115 H129 L112 132 L112 115 H42 Q28 115 28 101 V79 Q28 65 42 65Z" /><text x="141" y="97" text-anchor="middle">운행을 확인해요</text></g>
        <g class="decoration-bubble"><path d="M837 45 H1005 Q1019 45 1019 59 V81 Q1019 95 1005 95 H999 L1025 112 L984 95 H837 Q823 95 823 81 V59 Q823 45 837 45Z" /><text x="921" y="77" text-anchor="middle">안전하게 운행 중</text></g>
        <g class="decoration-bubble" transform="translate(0 15)"><path d="M1044 365 H1210 Q1224 365 1224 379 V401 Q1224 415 1210 415 H1200 L1256 434 L1180 415 H1044 Q1030 415 1030 401 V379 Q1030 365 1044 365Z" /><text x="1127" y="397" text-anchor="middle">운행을 확인해요</text></g>
      </svg>
    </div>

    <div class="city-scenery" aria-hidden="true"><img :src="bottom" alt="" /></div>
  </section>
</template>
<style scoped>
.operations-description{margin:8px 0 0;color:var(--grtc-muted);font-size:18px;line-height:1.5}

.operations-page { position: relative; min-height: calc(100svh - 134px); display: flex; flex-direction: column; isolation: isolate; }
.operations-heading { position: relative; z-index: 1; padding: 0 8px; display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; }
.operations-heading h1 { margin: 0; color: var(--grtc-primary); font-size: 44px; font-weight: 800; line-height: 1.3; }
.destination-legend { display: block; width: 270px; height: auto; margin-top: 0; flex-shrink: 0; }
.route-scroll { position: relative; z-index: 1; overflow-x: auto; flex-shrink: 0; margin-top: 0; padding-bottom: 0; }
.route-scroll:focus-visible { outline: 2px solid var(--grtc-primary); outline-offset: -2px; }
.operation-route { display: block; width: 100%; min-width: 1200px; height: auto; }
.rail { fill: none; stroke: #d8dde2; stroke-width: 12; stroke-linecap: round; stroke-linejoin: round; }
.station text { fill: #142e58; font-size: 22px; font-weight: 800; }
.decoration-bubble path { fill: #fff; stroke: #b9dfee; stroke-width: 2; }
.decoration-bubble text { fill: #183e68; font-size: 20px; font-weight: 700; }
.city-scenery { position: absolute; left: -32px; right: -32px; bottom: -32px; height: clamp(90px, 8vw, 150px); overflow: hidden; pointer-events: none; z-index: -1; }
.city-scenery img { position: absolute; display: block; width: 100%; height: auto; left: 0; bottom: 0; transform: translateY(37%); }
@media (max-width: 760px) { .city-scenery { left: -20px; right: -20px; bottom: -20px; } }
@media (max-width: 1400px) { .destination-legend { width: 250px; margin-top: 0; } .operations-heading h1 { font-size: 40px; } }
@media (max-width: 600px) { .operations-page { min-height: calc(100svh - 102px); } .operations-heading h1 { font-size: 36px; } }
</style>

