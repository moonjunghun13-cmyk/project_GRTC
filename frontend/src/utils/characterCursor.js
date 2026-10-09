export function installCursorPressState(root, target, windowTarget) {
  const reset = () => root.classList.remove('character-cursor-pressed')
  const down = (event) => {
    if (event.button === 0 && event.pointerType !== 'touch') root.classList.add('character-cursor-pressed')
  }
  const move = (event) => { if (!(event.buttons & 1)) reset() }
  const events = [
    [target, 'pointerdown', down], [windowTarget, 'pointerup', reset],
    [windowTarget, 'pointercancel', reset], [windowTarget, 'blur', reset],
    [windowTarget, 'dragend', reset], [windowTarget, 'pointermove', move],
    [target, 'visibilitychange', reset],
  ]
  events.forEach(([element, name, handler]) => element.addEventListener(name, handler, { capture: true, passive: true }))
  return () => {
    events.forEach(([element, name, handler]) => element.removeEventListener(name, handler, { capture: true }))
    reset()
  }
}

