<script setup>
import { computed, ref } from 'vue'
const props = defineProps({ maxLength: {type:Number,required:true}, error:String })
const emit = defineEmits(['update'])
const editor = ref(null)
const text = ref('')
const count = computed(() => Array.from(text.value).length)
const linkOpen = ref(false)
const linkUrl = ref('')
const linkError = ref('')
const limitMessage = ref('')
let savedRange = null
let lastHtml = ''
function rememberSelection() {
 const selection = window.getSelection()
 if (selection.rangeCount && editor.value.contains(selection.anchorNode) && editor.value.contains(selection.focusNode)) savedRange = selection.getRangeAt(0).cloneRange()
}
function restoreSelection() {
 editor.value.focus()
 const selection = window.getSelection()
 if (savedRange && editor.value.contains(savedRange.commonAncestorContainer)) { selection.removeAllRanges(); selection.addRange(savedRange) }
}
function readText() { return editor.value.innerText.replace(/\u00a0/g,' ').replace(/\n$/, '') }
function changed() {
 let next = readText()
 if (Array.from(next).length > props.maxLength) {
   editor.value.innerHTML = lastHtml
   next = readText()
   limitMessage.value = '내용은 ' + props.maxLength.toLocaleString() + '자까지 입력할 수 있습니다.'
   const range = document.createRange(); range.selectNodeContents(editor.value); range.collapse(false)
   const selection = window.getSelection(); selection.removeAllRanges(); selection.addRange(range)
 } else { lastHtml = editor.value.innerHTML; limitMessage.value = '' }
 text.value = next
 emit('update', {text:next,html:lastHtml})
 rememberSelection()
}
function format(command) { restoreSelection(); document.execCommand(command, false); changed() }
function paste(event) {
 event.preventDefault(); restoreSelection()
 const selected = window.getSelection().toString()
 const remaining = props.maxLength - count.value + Array.from(selected).length
 const value = Array.from(event.clipboardData.getData('text/plain')).slice(0,Math.max(0,remaining)).join('')
 document.execCommand('insertText', false, value); changed()
}
function openLink() { rememberSelection(); linkOpen.value=true; linkUrl.value=''; linkError.value='' }
function addLink() {
 let url
 try { url = new URL(linkUrl.value); if (!['http:','https:','mailto:'].includes(url.protocol)) throw new Error() }
 catch { linkError.value='http 또는 https로 시작하는 올바른 링크를 입력해 주세요.'; return }
 restoreSelection()
 if (!window.getSelection().toString()) { linkError.value='링크로 만들 문구를 먼저 선택해 주세요.'; return }
 document.execCommand('createLink', false, url.href); changed(); linkOpen.value=false
}
</script>
<template>
 <div class="complaint-editor" :class="{'editor-invalid':error}">
  <div class="editor-toolbar" role="toolbar" aria-label="내용 편집 도구">
   <button type="button" aria-label="굵게" title="굵게" @mousedown.prevent @click="format('bold')"><b>B</b></button>
   <button type="button" aria-label="글머리 목록" title="글머리 목록" @mousedown.prevent @click="format('insertUnorderedList')">• ≡</button>
   <button type="button" aria-label="번호 목록" title="번호 목록" @mousedown.prevent @click="format('insertOrderedList')">1. ≡</button>
   <button type="button" aria-label="링크" title="링크" @mousedown.prevent @click="openLink"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 15 6-6M8 16l-2 2a4 4 0 0 1-6-6l5-5m11 1 2-2a4 4 0 0 1 6 6l-5 5" /></svg></button>
  </div>
  <div v-if="linkOpen" class="editor-link-panel"><label for="editor-link">선택한 문구의 링크</label><input id="editor-link" v-model="linkUrl" type="url" placeholder="https://example.com" @keydown.enter.prevent="addLink" /><button type="button" @click="addLink">적용</button><button type="button" @click="linkOpen=false">닫기</button><p v-if="linkError" role="alert">{{ linkError }}</p></div>
  <div ref="editor" class="editor-surface" contenteditable="true" role="textbox" aria-label="내용" aria-multiline="true" :aria-invalid="!!error" aria-describedby="complaint-content-error" data-placeholder="민원 내용을 자세히 작성해 주세요." @input="changed" @paste="paste" @drop.prevent @keyup="rememberSelection" @mouseup="rememberSelection" @blur="rememberSelection"></div>
  <div class="editor-counter"><span role="status">{{ limitMessage }}</span><span>{{ count.toLocaleString() }} / {{ maxLength.toLocaleString() }}</span></div>
 </div>
</template>
<style scoped>
.complaint-editor { flex: 1; min-height: 160px; display: flex; flex-direction: column; border: 1px solid #c8dce9; border-radius: 10px; background: white; }
.editor-invalid { border-color: #c53740; }
.editor-toolbar { display: flex; gap: 6px; padding: 6px 10px; border-bottom: 1px solid #e1edf4; background: #f8fbfd; border-radius: 10px 10px 0 0; }
.editor-toolbar button { display: grid; place-items: center; min-width: 32px; height: 30px; padding: 0 7px; border: 0; border-radius: 5px; background: none; color: #325975; font-size: 17px; cursor: pointer; }
.editor-toolbar button:hover { background: #e4f2fa; }
.editor-toolbar svg { width: 19px; height: 19px; fill: none; stroke: currentColor; stroke-width: 1.7; }
.editor-surface { flex: 1 1 0; min-height: 90px; overflow: auto; padding: 14px 18px; color: #243c50; font-size: 17px; line-height: 1.6; outline: none; overflow-wrap: anywhere; }
.editor-surface:empty::before { content: attr(data-placeholder); color: #91a0ab; pointer-events: none; }
.editor-surface :deep(a) { color: #0078ae; }
.editor-counter { display: flex; justify-content: space-between; gap: 12px; padding: 4px 14px 8px; font-size: 14px; color: #7a8b99; }
.editor-counter > :first-child { color: #c53740; }
.editor-link-panel { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; padding: 8px 12px; font-size: 15px; }
.editor-link-panel input { flex: 1; min-width: 150px; border: 1px solid #c8dce9; border-radius: 5px; padding: 6px; font: inherit; }
.editor-link-panel button { padding: 6px 10px; border: 1px solid #c8dce9; background: white; border-radius: 5px; color: #064b76; cursor: pointer; }
.editor-link-panel p { width: 100%; margin: 0; color: #c53740; }
.complaint-editor:focus-within { outline: 2px solid #3298db; outline-offset: 1px; }
</style>

