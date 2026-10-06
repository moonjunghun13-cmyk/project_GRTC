<script setup>
import { computed, ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import train from '../../assets/train3.png'

const route = useRoute()
const stations = [
  { label: '대시보드', path: '/dashboard', prefix: 'admin-dashboard' },
  { label: '차량관리', path: '/dashboard/vehicles', prefix: 'admin-vehicle' },
  { label: '배차관리', path: '/dashboard/dispatches', prefix: 'admin-dispatch' },
  { label: '운행관리', path: '/dashboard/operations', prefix: 'admin-operation' },
  { label: '민원관리', path: '/dashboard/complaints', prefix: 'admin-complaint' },
  { label: '회원관리', path: '/dashboard/members', prefix: 'admin-member' },
]
const currentIndex = computed(() => stations.findIndex(item => String(route.name || '').startsWith(item.prefix)))
const track = ref(null)
const position = ref(0)
const piers = ref([])
const direction = ref(1)
const placed = ref(false)
const animate = ref(false)
let observer
let frame
let alive = true
async function place(immediate = false) {
  await nextTick()
  if (!alive || !track.value || currentIndex.value < 0) return
  const marker = track.value.querySelectorAll('.station-dot')[currentIndex.value]
  if (!marker || !track.value.clientWidth) return
    const railWidth = track.value.clientWidth - 64
  const centers = Array.from(track.value.querySelectorAll('.station-dot'), dot => {
    const rect = dot.getBoundingClientRect()
    return rect.left + rect.width / 2 - track.value.getBoundingClientRect().left - 32
  })
  const count = Math.max(1, Math.round(railWidth / 100))
  piers.value = Array.from({ length: count - 1 }, (_, i) => (i + 1) * railWidth / count)
    .filter(x => centers.every(center => Math.abs(x - center) > 34))
  const box = marker.getBoundingClientRect()
  const next = box.left + box.width / 2 - track.value.getBoundingClientRect().left
  if (next !== position.value) direction.value = next < position.value ? -1 : 1
  animate.value = placed.value && !immediate
  position.value = next
  placed.value = true
  if (immediate) {
    cancelAnimationFrame(frame)
    frame = requestAnimationFrame(() => { if (alive) animate.value = true })
  }
}
watch(currentIndex, () => place())
onMounted(() => {
  place(true)
  observer = new ResizeObserver(() => place(true))
  observer.observe(track.value)
})
onBeforeUnmount(() => {
  alive = false
  observer?.disconnect()
  cancelAnimationFrame(frame)
})
</script>

<template>
  <nav ref="track" class="header-route-map" aria-label="관리자 상단 메뉴">
    <span class="route-background-softener" aria-hidden="true"></span>
    <span class="station-line" aria-hidden="true"><span v-for="x in piers" :key="x" class="bridge-pier" :style="{ left: (x - 1.75) + 'px' }"></span><span v-if="placed" class="rail-selected" :style="{ left: (position - 32) + 'px' }"></span></span>
    <RouterLink v-for="(station, index) in stations" :key="station.path" :to="station.path" class="header-station" :class="{ current: index === currentIndex }" :aria-current="index === currentIndex ? 'page' : undefined">
      <span class="station-dot" aria-hidden="true"></span>
      <span class="station-label">{{ station.label }}</span>
    </RouterLink>
    <span v-show="placed && currentIndex >= 0" class="station-train-position" :class="{ animated: animate }" :style="{ transform: 'translateX(' + position + 'px)' }" aria-hidden="true">
      <span class="train-crop"><img :src="train" alt="" :style="{ transform: 'scaleX(' + direction + ')' }" /></span>
    </span>
  </nav>
</template>

<style scoped>
.header-route-map { position: relative; flex: 1; min-width: 0; height: 68px; display: flex; justify-content: space-between; padding: 0; isolation: isolate; }
.route-background-softener { position: absolute; inset: 0 -18px; z-index: -1; pointer-events: none; background: linear-gradient(to bottom, transparent 0%, rgb(255 255 255 / 18%) 22%, rgb(255 255 255 / 92%) 48%, rgb(255 255 255 / 95%) 76%, rgb(255 255 255 / 90%) 88%, transparent 100%); mask-image: linear-gradient(to right, transparent, black 4%, black 96%, transparent); }
.station-line { position: absolute; left: 32px; right: 32px; top: 36.1px; height: 11.5px; pointer-events: none; }
.bridge-pier { position: absolute; top: 5px; width: 3.5px; height: 6.5px; border-radius: 0 0 1.5px 1.5px; background: #DDE5EC; }
.station-line::before, .station-line::after { content: ''; position: absolute; left: 0; right: 0; }
.station-line::before { top: 0; height: 1.5px; background: #a4adb5; z-index: 1; }
.station-line::after { top: 1.5px; height: 3.5px; background: #CBD5DF; border-radius: 1px; }
.rail-selected { position: absolute; top: 0; width: 90px; height: 1.5px; transform: translateX(-50%); z-index: 2; background: linear-gradient(to right, transparent, #7bb7d4 25%, #7bb7d4 75%, transparent); }
.header-station { width: 64px; flex: 0 0 64px; position: relative; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 4px; padding-bottom: 6px; text-decoration: none; color: #6B7280; font-size: 12px; font-weight: 500; white-space: nowrap; }
.station-dot { width: 6px; height: 6px; border: 2px solid #9ca3af; border-radius: 50%; background: white; flex-shrink: 0; transition: border-color 180ms ease-out; box-shadow: 0 0 0 2px white; }
.station-label { line-height: 1.2; transition: color 180ms ease-out; }
.header-station:not(.current):hover { color: #4b5563; }
.header-station:not(.current):hover .station-dot { border-color: #6b7280; }
.header-station.current { font-weight: 800; color: #0078ae; }
.current .station-dot { background: #0078ae; border-color: #0078ae; }
.header-station:focus-visible { outline: 2px solid #0078ae; outline-offset: -2px; border-radius: 6px; }
.station-train-position { position: absolute; left: 0; top: 8.65px; width: 0; pointer-events: none; }
.train-crop { display: block; width: max-content; height: 27.45px; overflow: hidden; transform: translateX(-50%); }
.station-train-position.animated { transition: transform 750ms cubic-bezier(.22,.61,.36,1); }
.header-route-map .station-train-position img { display: block; height: 31.2px; width: auto; max-width: none; }
@media (max-width: 1200px) { .header-route-map { display: none; } }
@media (prefers-reduced-motion: reduce) { .train-crop { display: block; width: max-content; height: 27.45px; overflow: hidden; transform: translateX(-50%); }
.station-train-position.animated { transition: none; } }
</style>










