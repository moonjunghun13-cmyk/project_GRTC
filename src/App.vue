<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import pointCursor from './assets/point-cursor.png'
import pressedCursor from './assets/point2-cursor.png'
import { installCursorPressState } from './utils/characterCursor'

let dispose
let cancelled = false
onMounted(async () => {
  const root = document.documentElement
  const load = (url) => new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = resolve
    image.onerror = reject
    image.src = url
  })
  try {
    await Promise.all([load(pointCursor), load(pressedCursor)])
    if (cancelled) return
    root.style.setProperty('--character-cursor-normal', `url("${pointCursor}") 2 9, auto`)
    root.style.setProperty('--character-cursor-down', `url("${pressedCursor}") 2 9, auto`)
    root.classList.add('character-cursor')
    dispose = installCursorPressState(root, document, window)
  } catch {
    // Keep the native cursors if either asset cannot be loaded.
  }
})
onBeforeUnmount(() => {
  cancelled = true
  dispose?.()
  const root = document.documentElement
  root.classList.remove('character-cursor')
  root.style.removeProperty('--character-cursor-normal')
  root.style.removeProperty('--character-cursor-down')
})
</script>

<template>
  <RouterView />
</template>
