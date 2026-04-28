<template>
  <div class="werewolf-game">
    <div class="game-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>🐺 狼人杀</h2>
      <div class="room-info">房间ID: {{ roomId }}</div>
    </div>

    <div class="game-content">
      <div class="players-panel">
        <h3>玩家列表</h3>
        <div class="players-grid">
          <div
            v-for="player in players"
            :key="player.id"
            :class="['player-card', { 'dead': player.status === 1, 'speaking': player.isSpeaking }]"
          >
            <div class="player-avatar">
              <span>{{ player.nickname?.charAt(0) || '?' }}</span>
              <div v-if="player.status === 1" class="dead-mark">💀</div>
            </div>
            <div class="player-name">{{ player.nickname }}</div>
            <div v-if="player.role && showRoles" class="player-role">
              {{ getRoleName(player.role) }}
            </div>
          </div>
        </div>
      </div>

      <div class="game-panel">
        <div class="phase-indicator">
          <span :class="['phase', currentPhase]">{{ getPhaseText() }}</span>
        </div>

        <div v-if="currentPhase === 'night'" class="night-panel">
          <div class="night-actions">
            <button v-if="myRole === 1" class="action-btn" @click="showWolfAction = true">
              🐺 刀人
            </button>
            <button v-if="myRole === 3" class="action-btn" @click="showSeerAction = true">
              🔮 验人
            </button>
            <button v-if="myRole === 4" class="action-btn" @click="showWitchAction = true">
              🧪 使用药剂
            </button>
          </div>
        </div>

        <div v-if="currentPhase === 'day'" class="day-panel">
          <div class="speech-area">
            <div v-if="currentSpeaker" class="speaker-info">
              {{ currentSpeaker.nickname }} 正在发言...
            </div>
            <div v-else class="speaker-info">等待发言...</div>
          </div>
          <div class="vote-area">
            <button class="vote-btn" @click="showVoteModal = true">
              🗳️ 投票
            </button>
          </div>
        </div>

        <div class="chat-area">
          <h4>聊天</h4>
          <div class="chat-messages">
            <div v-for="msg in chatMessages" :key="msg.id" class="chat-message">
              <span class="msg-name">{{ msg.name }}:</span>
              <span class="msg-content">{{ msg.content }}</span>
            </div>
          </div>
          <div class="chat-input">
            <input v-model="chatInput" @keyup.enter="sendMessage" placeholder="输入消息..." />
            <button @click="sendMessage">发送</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showStartBtn" class="start-panel">
      <button class="start-btn" @click="startGame">开始游戏</button>
    </div>

    <div v-if="showWolfAction" class="modal-overlay" @click.self="showWolfAction = false">
      <div class="modal-content">
        <h3>选择要刀的玩家</h3>
        <div class="player-select">
          <div
            v-for="player in alivePlayers"
            :key="player.id"
            class="select-item"
            @click="killPlayer(player.id)"
          >
            {{ player.nickname }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="showSeerAction" class="modal-overlay" @click.self="showSeerAction = false">
      <div class="modal-content">
        <h3>选择要查验的玩家</h3>
        <div class="player-select">
          <div
            v-for="player in players"
            :key="player.id"
            class="select-item"
            @click="checkPlayer(player.id)"
          >
            {{ player.nickname }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="showWitchAction" class="modal-overlay" @click.self="showWitchAction = false">
      <div class="modal-content">
        <h3>选择药剂</h3>
        <div class="witch-actions">
          <button class="potion-btn heal" @click="usePotion('heal')">💚 解药</button>
          <button class="potion-btn poison" @click="usePotion('poison')">💀 毒药</button>
        </div>
      </div>
    </div>

    <div v-if="showVoteModal" class="modal-overlay" @click.self="showVoteModal = false">
      <div class="modal-content">
        <h3>投票出局</h3>
        <div class="player-select">
          <div
            v-for="player in alivePlayers"
            :key="player.id"
            class="select-item"
            @click="submitVote(player.id)"
          >
            {{ player.nickname }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useGameStore } from '@/stores/game'

const props = defineProps<{
  roomId: number
}>()

const gameStore = useGameStore()

const players = ref<any[]>([])
const currentPhase = ref<'waiting' | 'night' | 'day'>('waiting')
const currentSpeaker = ref<any>(null)
const myRole = ref(0)
const showRoles = ref(false)
const showStartBtn = ref(true)

const showWolfAction = ref(false)
const showSeerAction = ref(false)
const showWitchAction = ref(false)
const showVoteModal = ref(false)

const chatMessages = ref<any[]>([])
const chatInput = ref('')

const alivePlayers = computed(() => players.value.filter(p => p.status === 0))

const getRoleName = (role: number) => {
  const roles: Record<number, string> = {
    1: '🐺 狼人',
    2: '👨 平民',
    3: '🔮 预言家',
    4: '🧪 女巫',
    5: '🔫 猎人',
    6: '🤡 白痴'
  }
  return roles[role] || '未知'
}

const getPhaseText = () => {
  const phases: Record<string, string> = {
    waiting: '等待开始',
    night: '🌙 夜晚阶段',
    day: '☀️ 白天阶段'
  }
  return phases[currentPhase.value]
}

const goBack = () => {
  gameStore.exitRoom()
}

const startGame = async () => {
  const result = await gameStore.startGame('werewolf', props.roomId)
  if (result.code === 200) {
    players.value = result.data.players
    showStartBtn.value = false
    currentPhase.value = 'night'
  } else {
    alert(result.message)
  }
}

const killPlayer = async (targetId: number) => {
  showWolfAction.value = false
}

const checkPlayer = async (targetId: number) => {
  showSeerAction.value = false
}

const usePotion = (type: string) => {
  showWitchAction.value = false
}

const submitVote = async (targetId: number) => {
  const result = await gameStore.vote(props.roomId, 1, targetId)
  if (result.code === 200) {
    showVoteModal.value = false
  } else {
    alert(result.message)
  }
}

const sendMessage = () => {
  if (!chatInput.value.trim()) return
  chatMessages.value.push({
    id: Date.now(),
    name: '我',
    content: chatInput.value
  })
  chatInput.value = ''
}

onMounted(() => {
  players.value = [
    { id: 1, userId: 1, nickname: '玩家1', role: 0, status: 0, isSpeaking: false },
    { id: 2, userId: 2, nickname: '玩家2', role: 0, status: 0, isSpeaking: false },
    { id: 3, userId: 3, nickname: '玩家3', role: 0, status: 0, isSpeaking: false },
    { id: 4, userId: 4, nickname: '玩家4', role: 0, status: 0, isSpeaking: false },
    { id: 5, userId: 5, nickname: '玩家5', role: 0, status: 0, isSpeaking: false },
    { id: 6, userId: 6, nickname: '玩家6', role: 0, status: 0, isSpeaking: false }
  ]
})
</script>

<style scoped>
.werewolf-game {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.3);
}

.back-btn {
  background: transparent;
  border: 1px solid #666;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
}

.game-header h2 {
  color: white;
  margin: 0;
}

.room-info {
  color: #999;
  font-size: 14px;
}

.game-content {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
}

.players-panel {
  width: 280px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
}

.players-panel h3 {
  color: white;
  margin: 0 0 16px 0;
}

.players-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.player-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  transition: all 0.2s;
}

.player-card.dead {
  opacity: 0.4;
}

.player-card.speaking {
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.5);
}

.player-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  font-size: 20px;
  color: white;
  position: relative;
}

.dead-mark {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 16px;
}

.player-name {
  color: white;
  font-size: 14px;
}

.player-role {
  font-size: 12px;
  margin-top: 4px;
}

.game-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.phase-indicator {
  text-align: center;
}

.phase {
  padding: 12px 32px;
  border-radius: 20px;
  font-size: 18px;
  font-weight: bold;
}

.phase.waiting {
  background: #666;
  color: white;
}

.phase.night {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #b8b8ff;
  border: 1px solid #666;
}

.phase.day {
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  color: #333;
}

.night-panel {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.night-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  transition: background 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.day-panel {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.speech-area {
  margin-bottom: 16px;
}

.speaker-info {
  color: white;
  font-size: 16px;
}

.vote-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.chat-area {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.chat-area h4 {
  color: white;
  margin: 0 0 12px 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
}

.chat-message {
  margin-bottom: 8px;
}

.msg-name {
  color: #667eea;
  font-weight: bold;
}

.msg-content {
  color: white;
  margin-left: 8px;
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input input {
  flex: 1;
  padding: 10px;
  border: 1px solid #666;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.chat-input button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background: #667eea;
  color: white;
  cursor: pointer;
}

.start-panel {
  position: fixed;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
}

.start-btn {
  padding: 16px 48px;
  border: none;
  border-radius: 32px;
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #2a2a4a;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
}

.modal-content h3 {
  color: white;
  margin: 0 0 16px 0;
}

.player-select {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.select-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  text-align: center;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.select-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.witch-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.potion-btn {
  padding: 16px 32px;
  border: none;
  border-radius: 12px;
  font-size: 18px;
  cursor: pointer;
}

.potion-btn.heal {
  background: #4caf50;
  color: white;
}

.potion-btn.poison {
  background: #f44336;
  color: white;
}
</style>