<template>
  <div class="game-room-list">
    <div class="header">
      <h2>{{ title }}</h2>
      <button class="create-btn" @click="createRoom">创建房间</button>
    </div>
    
    <div class="room-grid">
      <div 
        v-for="room in rooms" 
        :key="room.id" 
        class="room-card"
        @click="joinRoom(room)"
      >
        <div class="room-name">{{ room.roomName }}</div>
        <div class="room-info">
          <span>{{ room.currentPlayerCount }}/{{ room.maxPlayers }} 人</span>
          <span :class="room.status === 0 ? 'waiting' : 'playing'">
            {{ room.status === 0 ? '等待中' : '游戏中' }}
          </span>
        </div>
        <div v-if="room.password" class="password-tag">🔒</div>
      </div>
    </div>

    <div v-if="rooms.length === 0" class="empty-state">
      <div class="empty-icon">🎮</div>
      <p>暂无房间，快来创建一个吧！</p>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal-content">
        <h3>创建{{ gameType === 'werewolf' ? '狼人杀' : '你画我猜' }}房间</h3>
        <div class="form-group">
          <label>房间名称</label>
          <input v-model="roomName" type="text" placeholder="请输入房间名称" />
        </div>
        <div class="form-group">
          <label>房间密码（可选）</label>
          <input v-model="roomPassword" type="password" placeholder="设置房间密码" />
        </div>
        <div class="form-group">
          <label>最大人数</label>
          <select v-model="maxPlayers">
            <option :value="gameType === 'werewolf' ? 6 : 2">默认</option>
            <option v-if="gameType === 'werewolf'" :value="6">6人</option>
            <option v-if="gameType === 'werewolf'" :value="8">8人</option>
            <option v-if="gameType === 'werewolf'" :value="10">10人</option>
            <option v-if="gameType === 'werewolf'" :value="12">12人</option>
            <option v-if="gameType === 'drawing'" :value="4">4人</option>
            <option v-if="gameType === 'drawing'" :value="6">6人</option>
            <option v-if="gameType === 'drawing'" :value="8">8人</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showCreateModal = false">取消</button>
          <button class="confirm-btn" @click="confirmCreate">创建</button>
        </div>
      </div>
    </div>

    <div v-if="showJoinModal" class="modal-overlay" @click.self="showJoinModal = false">
      <div class="modal-content">
        <h3>加入房间</h3>
        <div class="form-group">
          <label>房间ID</label>
          <input v-model="joinRoomId" type="number" placeholder="请输入房间ID" />
        </div>
        <div v-if="joinRoomPasswordRequired" class="form-group">
          <label>房间密码</label>
          <input v-model="joinPassword" type="password" placeholder="请输入房间密码" />
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showJoinModal = false">取消</button>
          <button class="confirm-btn" @click="confirmJoin">加入</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useGameStore } from '@/stores/game'

const props = defineProps<{
  gameType: 'werewolf' | 'drawing'
}>()

const emit = defineEmits<{
  (e: 'enterRoom', roomId: number): void
}>()

const gameStore = useGameStore()

const title = computed(() => props.gameType === 'werewolf' ? '🐺 狼人杀' : '🎨 你画我猜')

const rooms = ref<any[]>([])
const showCreateModal = ref(false)
const showJoinModal = ref(false)
const roomName = ref('')
const roomPassword = ref('')
const maxPlayers = ref(props.gameType === 'werewolf' ? 12 : 8)
const joinRoomId = ref<number | null>(null)
const joinPassword = ref('')
const joinRoomPasswordRequired = ref(false)

const currentRoom = ref<any>(null)

const fetchRooms = async () => {
  const result = await gameStore.listRooms(props.gameType)
  if (result.code === 200) {
    rooms.value = result.data
  }
}

const createRoom = () => {
  showCreateModal.value = true
}

const confirmCreate = async () => {
  if (!roomName.value.trim()) {
    alert('请输入房间名称')
    return
  }
  
  const result = await gameStore.createRoom(
    props.gameType,
    roomName.value,
    roomPassword.value || undefined,
    maxPlayers.value
  )
  
  if (result.code === 200) {
    showCreateModal.value = false
    roomName.value = ''
    roomPassword.value = ''
    fetchRooms()
    emit('enterRoom', result.data.roomId)
  } else {
    alert(result.message)
  }
}

const joinRoom = (room: any) => {
  currentRoom.value = room
  joinRoomId.value = room.id
  joinRoomPasswordRequired.value = !!room.password
  showJoinModal.value = true
}

const confirmJoin = async () => {
  if (!joinRoomId.value) {
    alert('请输入房间ID')
    return
  }
  
  const result = await gameStore.joinRoom(
    props.gameType,
    joinRoomId.value,
    joinPasswordRequired.value ? joinPassword.value : undefined
  )
  
  if (result.code === 200) {
    showJoinModal.value = false
    joinPassword.value = ''
    fetchRooms()
    emit('enterRoom', joinRoomId.value)
  } else {
    alert(result.message)
  }
}

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
.game-room-list {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  font-size: 24px;
  color: #333;
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: transform 0.2s;
}

.create-btn:hover {
  transform: translateY(-2px);
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.room-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.room-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.room-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.status.waiting {
  background: #e8f5e9;
  color: #2e7d32;
}

.status.playing {
  background: #fff3e0;
  color: #e65100;
}

.password-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  font-size: 16px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
}

.modal-content h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #666;
  cursor: pointer;
}

.confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
}
</style>