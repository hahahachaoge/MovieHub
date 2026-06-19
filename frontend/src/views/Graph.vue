<template>
  <div class="graph-page page-container">
    <div class="graph-header">
      <h1 class="page-title">🕸️ 关系图谱</h1>
      <p class="page-subtitle">可视化展示电影、演员、导演和分类之间的关联关系</p>
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索电影、演员或导演名称..." size="large" clearable @keyup.enter="doSearch" @clear="loadHot">
        <template #append><el-button @click="doSearch" :loading="loading">搜索</el-button></template>
      </el-input>
    </div>
    <div class="graph-info">
      <span class="hint">💡 默认展示 TOP20 热门电影</span>
      <span class="legend">
        <span class="dot" style="background:#e50914"></span>电影
        <span class="dot" style="background:#3498db"></span>演员
        <span class="dot" style="background:#2ecc71"></span>导演
        <span class="dot" style="background:#8b5cf6"></span>导演&演员
        <span class="dot" style="background:#d4a017"></span>分类
      </span>
      <span class="node-count">节点:{{ nodeCount }} 关系:{{ edgeCount }}</span>
    </div>
    <div ref="graphRef" class="graph-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotGraph, searchGraph } from '../api/graph'

const keyword = ref('')
const loading = ref(false)
const graphRef = ref<HTMLElement>()
const nodeCount = ref(0)
const edgeCount = ref(0)
let network: any = null

const GROUPS: Record<string, any> = {
  movie: { shape: 'dot', color: { background: '#e50914', border: '#fff' } },
  actor: { shape: 'dot', color: { background: '#3498db', border: '#fff' } },
  director: { shape: 'square', color: { background: '#2ecc71', border: '#fff' } },
  both: { shape: 'diamond', color: { background: '#8b5cf6', border: '#fff' } },
  category: { shape: 'diamond', color: { background: '#d4a017', border: '#fff' } }
}

function loadVis(cb: () => void) {
  if ((window as any).vis?.Network) { cb(); return }
  const s = document.createElement('script')
  s.src = '/vis.min.js'; s.onload = cb
  s.onerror = () => ElMessage.error('图谱引擎加载失败')
  document.head.appendChild(s)
}

async function loadHot() {
  loading.value = true
  try {
    const r = await getHotGraph(20)
    if (r.data.code === 200) render(r.data.data)
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function doSearch() {
  if (!keyword.value.trim()) { loadHot(); return }
  loading.value = true
  try {
    const r = await searchGraph(keyword.value.trim(), 30)
    if (r.data.code === 200) render(r.data.data)
    if (r.data.data.nodes.length === 0) ElMessage.info('未找到结果')
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function render(data: { nodes: any[], edges: any[] }) {
  nodeCount.value = data.nodes.length; edgeCount.value = data.edges.length
  if (!graphRef.value) return
  if (network) network.destroy()

  const bothSet = new Set(data.nodes.filter(n => n.group === 'both').map(n => n.id))
  const cnt: Record<string, number> = {}
  const win = window as any
  const nds = data.nodes.map(n => ({
    id: n.id, label: n.label, group: n.group,
    color: { background: n.color, border: '#fff' },
    font: { color: '#e0e0e0', size: 12 }, borderWidth: 2,
    size: n.group === 'movie' ? 25 : n.group === 'category' ? 18 : 22
  }))
  const eds = data.edges.map(e => {
    const k = e.from + '|' + e.to
    const isBoth = bothSet.has(e.from) || bothSet.has(e.to)
    cnt[k] = (cnt[k] || 0) + 1
    return {
      from: e.from, to: e.to, label: e.label,
      color: { color: e.color, highlight: '#fff' },
      font: { color: '#999', size: 9, strokeWidth: 0 }, width: 1.5,
      arrows: e.label ? { to: { enabled: true, scaleFactor: 0.5 } } : { to: { enabled: false } },
      smooth: isBoth ? { type: 'curvedCW', roundness: cnt[k] === 1 ? 0.2 : -0.2 } : { type: 'curvedCW', roundness: 0.1 }
    }
  })
  network = new win.vis.Network(graphRef.value,
    { nodes: new win.vis.DataSet(nds), edges: new win.vis.DataSet(eds) },
    { physics: { solver: 'forceAtlas2Based', forceAtlas2Based: { gravitationalConstant: -40, centralGravity: 0.005, springLength: 150, springConstant: 0.02 }, stabilization: { iterations: 200 } },
      layout: { improvedLayout: true }, interaction: { hover: true, tooltipDelay: 200, zoomView: true, dragView: true }, groups: GROUPS }
  )
}

onMounted(() => {
  getHotGraph(5).then(r => {
    if (r.data.code === 200) loadVis(() => loadHot())
    else ElMessage.error('图谱接口不可用')
  }).catch(() => ElMessage.error('后端未启动或Neo4j未连接'))
})
onUnmounted(() => { if (network) network.destroy() })
</script>

<style scoped>
.graph-page { padding-top:20px; padding-bottom:40px; }
.page-title { font-size:24px; font-weight:700; margin-bottom:4px; color:var(--text-primary); }
.page-subtitle { color:var(--text-secondary); margin-bottom:20px; }
.search-bar { margin-bottom:12px; }
.graph-info { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; color:var(--text-secondary); font-size:13px; }
.dot { display:inline-block; width:10px; height:10px; border-radius:50%; margin:0 4px 0 12px; vertical-align:middle; }
.legend .dot:first-child { margin-left:0; }
.hint { color:var(--text-muted); font-size:13px; }
.node-count { color:var(--text-muted); }
.graph-container { width:100%; height:650px; background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--card-radius); overflow:hidden; }
</style>
