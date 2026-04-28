<template>
  <div class="drawing-game">
    <div class="game-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2>🎨 你画我猜</h2>
      <div class="room-info">房间ID: {{ roomId }}</div>
    </div>

    <div class="game-content">
      <div class="players-panel">
        <h3>玩家列表</h3>
        <div class="players-list">
          <div
            v-for="player in players"
            :key="player.id"
            :class="['player-item', { 'drawer': player.isDrawer }]"
          >
            <div class="player-avatar">
              <span>{{ player.nickname?.charAt(0) || '?' }}</span>
              <div v-if="player.isDrawer" class="drawer-badge">🎨</div>
            </div>
            <div class="player-info">
              <div class="player-name">{{ player.nickname }}</div>
              <div class="player-score">分数: {{ player.score }}</div>
            </div>
          </div>
        </div>
        <div class="ranking-section">
          <h4>🏆 排行榜</h4>
          <div class="ranking-list">
            <div
              v-for="(player, index) in sortedPlayers"
              :key="player.id"
              class="ranking-item"
            >
              <span class="rank">{{ getRankIcon(index) }}</span>
              <span class="rank-name">{{ player.nickname }}</span>
              <span class="rank-score">{{ player.score }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="game-panel">
        <div class="round-info">
          <span>第 {{ currentRound }} 轮</span>
          <span class="word-hint">提示: {{ currentWordHint }}</span>
        </div>

        <div class="canvas-container">
          <canvas
            ref="canvasRef"
            :width="canvasWidth"
            :height="canvasHeight"
            :class="['game-canvas', { 'disabled': !isDrawer }]"
            @mousedown="startDrawing"
            @mousemove="draw"
            @mouseup="stopDrawing"
            @mouseleave="stopDrawing"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="stopDrawing"
          ></canvas>
          
          <div v-if="!isDrawer && !showAnswer" class="canvas-overlay">
            <div class="waiting-text">等待 {{ currentDrawerName }} 绘画...</div>
          </div>
        </div>

        <div class="toolbar" v-if="isDrawer">
          <div class="tool-group">
            <button
              v-for="tool in tools"
              :key="tool.id"
              :class="['tool-btn', { 'active': currentTool === tool.id }]"
              @click="currentTool = tool.id"
              :title="tool.name"
            >
              {{ tool.icon }}
            </button>
          </div>
          <div class="color-group">
            <input
              type="color"
              v-model="currentColor"
              class="color-picker"
            />
            <div
              v-for="color in presetColors"
              :key="color"
              :class="['color-btn', { 'active': currentColor === color }]"
              :style="{ backgroundColor: color }"
              @click="currentColor = color"
            ></div>
          </div>
          <div class="brush-group">
            <label>画笔大小</label>
            <input
              type="range"
              v-model="brushSize"
              min="1"
              max="20"
              class="brush-slider"
            />
            <span>{{ brushSize }}px</span>
          </div>
          <div class="action-group">
            <button class="action-btn" @click="clearCanvas">🗑️ 清空</button>
            <button class="action-btn" @click="undoAction">↩️ 撤销</button>
          </div>
        </div>

        <div class="guess-area">
          <div class="guess-history">
            <div
              v-for="guess in guessHistory"
              :key="guess.id"
              :class="['guess-item', { 'correct': guess.isCorrect }]"
            >
              <span class="guess-player">{{ guess.player }}</span>
              <span class="guess-word">{{ guess.word }}</span>
              <span v-if="guess.isCorrect" class="correct-mark">✓</span>
            </div>
          </div>
          <div class="guess-input-container">
            <input
              v-model="guessInput"
              @keyup.enter="submitGuess"
              :disabled="isDrawer || gameEnded"
              placeholder="输入你的猜测..."
              class="guess-input"
            />
            <button
              @click="submitGuess"
              :disabled="isDrawer || gameEnded || !guessInput.trim()"
              class="guess-btn"
            >
              提交
            </button>
          </div>
        </div>

        <div class="timer-bar">
          <div class="timer-label">剩余时间</div>
          <div class="timer-progress">
            <div
              class="timer-fill"
              :style="{ width: timerPercent + '%' }"
              :class="{ 'warning': timeLeft <= 10 }"
            ></div>
          </div>
          <div class="timer-text">{{ timeLeft }}s</div>
        </div>
      </div>
    </div>

    <div v-if="showStartBtn" class="start-panel">
      <button class="start-btn" @click="startGame">开始游戏</button>
    </div>

    <div v-if="showWordModal" class="modal-overlay" @click.self="showWordModal = false">
      <div class="modal-content">
        <h3>🎨 你的词语</h3>
        <div class="word-display">{{ currentWord }}</div>
        <button class="confirm-btn" @click="confirmWord">知道了</button>
      </div>
    </div>

    <div v-if="showResultModal" class="modal-overlay">
      <div class="modal-content result-modal">
        <h3>🎉 本轮结束</h3>
        <div class="result-word">正确答案: {{ currentWord }}</div>
        <div class="result-ranking">
          <h4>本轮得分</h4>
          <div
            v-for="(player, index) in sortedPlayers"
            :key="player.id"
            class="result-item"
          >
            <span>{{ index + 1 }}. {{ player.nickname }}: {{ player.score }}分</span>
          </div>
        </div>
        <button class="next-btn" @click="nextRound">下一轮</button>
      </div>
    </div>

    <div v-if="showFinalModal" class="modal-overlay">
      <div class="modal-content final-modal">
        <h3>🏆 游戏结束</h3>
        <div class="final-winner">
          <span class="winner-icon">🥇</span>
          <span class="winner-name">{{ winner?.nickname }}</span>
          <span class="winner-score">{{ winner?.score }}分</span>
        </div>
        <div class="final-ranking">
          <h4>最终排名</h4>
          <div
            v-for="(player, index) in sortedPlayers"
            :key="player.id"
            class="final-item"
          >
            <span>{{ getRankIcon(index) }} {{ player.nickname }}: {{ player.score }}分</span>
          </div>
        </div>
        <button class="restart-btn" @click="restartGame">再来一局</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useGameStore } from '@/stores/game';
const props = defineProps<{
 roomId: number;
}>();
const gameStore = useGameStore();
const canvasRef = ref<HTMLCanvasElement | null>(null);
const canvasWidth = ref(600);
const canvasHeight = ref(400);
const isDrawing = ref(false);
const currentTool = ref('pen');
const currentColor = ref('#000000');
const brushSize = ref(5);
const players = ref<any[]>([]);
const currentRound = ref(1);
const currentWord = ref('');
const currentWordHint = ref('???');
const isDrawer = ref(false);
const gameEnded = ref(false);
const showStartBtn = ref(true);
const showWordModal = ref(false);
const showResultModal = ref(false);
const showFinalModal = ref(false);
const showAnswer = ref(false);
const guessInput = ref('');
const guessHistory = ref<any[]>([]);
const timeLeft = ref(90);
const timer = ref<number | null>(null);
const drawHistory = ref<any[]>([]);
const tools = [
 { id: 'pen', name: '画笔', icon: '✏️' },
 { id: 'eraser', name: '橡皮擦', icon: '🧹' },
 { id: 'line', name: '直线', icon: '📏' },
 { id: 'rect', name: '矩形', icon: '⬜' },
 { id: 'circle', name: '圆形', icon: '⭕' }
];
const presetColors = [
 '#000000', '#ffffff', '#ff0000', '#00ff00', '#0000ff',
 '#ffff00', '#ff00ff', '#00ffff', '#ff6600', '#9900ff'
];
const sortedPlayers = computed(() => {
 return [...players.value].sort((a, b) => b.score - a.score);
});
const winner = computed(() => {
 return sortedPlayers.value[0];
});
const currentDrawerName = computed(() => {
 const drawer = players.value.find(p => p.isDrawer);
 return drawer?.nickname || '未知';
});
const timerPercent = computed(() => {
 return (timeLeft.value / 90) * 100;
});
const getRankIcon = (index: number) => {
 const icons = ['🥇', '🥈', '🥉'];
 return icons[index] || `${index + 1}`;
};
const goBack = () => {
 gameStore.exitRoom();
};
const startGame = async () => {
 const result = await gameStore.startGame('drawing', props.roomId);
 if (result.code === 200) {
 showStartBtn.value = false;
 startRound();
 }
 else {
 alert(result.message);
 }
};
const startRound = async () => {
 const wordResult = await gameStore.getWord(props.roomId);
 if (wordResult.code === 200) {
 currentWord.value = wordResult.data.word;
 currentWordHint.value = '???';
 showAnswer.value = false;
 guessHistory.value = [];
 timeLeft.value = 90;
 isDrawer.value = players.value.some(p => p.isDrawer && p.userId === 1);
 if (isDrawer.value) {
 showWordModal.value = true;
 }
 startTimer();
 }
};
const confirmWord = () => {
 showWordModal.value = false;
};
const startTimer = () => {
 if (timer.value)
 clearInterval(timer.value);
 timer.value = window.setInterval(() => {
 timeLeft.value--;
 if (timeLeft.value <= 0) {
 endRound();
 }
 }, 1000);
};
const stopTimer = () => {
 if (timer.value) {
 clearInterval(timer.value);
 timer.value = null;
 }
};
const endRound = () => {
 stopTimer();
 showAnswer.value = true;
 currentWordHint.value = currentWord.value;
 setTimeout(() => {
 if (currentRound.value >= players.value.length) {
 gameEnded.value = true;
 showFinalModal.value = true;
 }
 else {
 showResultModal.value = true;
 }
 }, 2000);
};
const nextRound = async () => {
 showResultModal.value = false;
 currentRound.value++;
 await gameStore.endGame('drawing', props.roomId);
 startRound();
};
const restartGame = () => {
 showFinalModal.value = false;
 currentRound.value = 1;
 gameEnded.value = false;
 guessHistory.value = [];
 startGame();
};
const submitGuess = async () => {
 if (!guessInput.value.trim() || isDrawer.value)
 return;
 const result = await gameStore.guessWord(props.roomId, 1, guessInput.value);
 if (result.code === 200) {
 const isCorrect = result.data.isCorrect;
 guessHistory.value.push({
 id: Date.now(),
 player: '我',
 word: guessInput.value,
 isCorrect
 });
 if (isCorrect) {
 stopTimer();
 showAnswer.value = true;
 currentWordHint.value = currentWord.value;
 setTimeout(() => {
 if (currentRound.value >= players.value.length) {
 gameEnded.value = true;
 showFinalModal.value = true;
 }
 else {
 showResultModal.value = true;
 }
 }, 1500);
 }
 guessInput.value = '';
 }
};
const startDrawing = (e: MouseEvent) => {
 if (!isDrawer.value)
 return;
 isDrawing.value = true;
 const canvas = canvasRef.value;
 if (!canvas)
 return;
 const ctx = canvas.getContext('2d');
 if (!ctx)
 return;
 const rect = canvas.getBoundingClientRect();
 const x = e.clientX - rect.left;
 const y = e.clientY - rect.top;
 ctx.beginPath();
 ctx.moveTo(x, y);
 drawHistory.value.push(ctx.getImageData(0, 0, canvas.width, canvas.height));
};
const draw = (e: MouseEvent) => {
 if (!isDrawing.value || !isDrawer.value)
 return;
 const canvas = canvasRef.value;
 if (!canvas)
 return;
 const ctx = canvas.getContext('2d');
 if (!ctx)
 return;
 const rect = canvas.getBoundingClientRect();
 const x = e.clientX - rect.left;
 const y = e.clientY - rect.top;
 ctx.strokeStyle = currentTool.value === 'eraser' ? '#ffffff' : currentColor.value;
 ctx.lineWidth = brushSize.value;
 ctx.lineCap = 'round';
 ctx.lineJoin = 'round';
 ctx.lineTo(x, y);
 ctx.stroke();
};
const stopDrawing = () => {
 isDrawing.value = false;
};
const handleTouchStart = (e: TouchEvent) => {
 e.preventDefault();
 const touch = e.touches[0];
 const event = new MouseEvent('mousedown', {
 clientX: touch.clientX,
 clientY: touch.clientY
 });
 canvasRef.value?.dispatchEvent(event);
};
const handleTouchMove = (e: TouchEvent) => {
 e.preventDefault();
 const touch = e.touches[0];
 const event = new MouseEvent('mousemove', {
 clientX: touch.clientX,
 clientY: touch.clientY
 });
 canvasRef.value?.dispatchEvent(event);
};
const clearCanvas = () => {
 const canvas = canvasRef.value;
 if (!canvas)
 return;
 const ctx = canvas.getContext('2d');
 if (!ctx)
 return;
 ctx.fillStyle = '#ffffff';
 ctx.fillRect(0, 0, canvas.width, canvas.height);
};
const undoAction = () => {
 if (drawHistory.value.length === 0)
 return;
 const canvas = canvasRef.value;
 if (!canvas)
 return;
 const ctx = canvas.getContext('2d');
 if (!ctx)
 return;
 const lastState = drawHistory.value.pop();
 if (lastState) {
 ctx.putImageData(lastState, 0, 0);
 }
};
const initCanvas = () => {
 const canvas = canvasRef.value;
 if (!canvas)
 return;
 const ctx = canvas.getContext('2d');
 if (!ctx)
 return;
 ctx.fillStyle = '#ffffff';
 ctx.fillRect(0, 0, canvas.width, canvas.height);
};
onMounted(() => {
 initCanvas();
 players.value = [
 { id: 1, userId: 1, nickname: '玩家1', score: 0, isDrawer: true },
 { id: 2, userId: 2, nickname: '玩家2', score: 0, isDrawer: false },
 { id: 3, userId: 3, nickname: '玩家3', score: 0, isDrawer: false },
 { id: 4, userId: 4, nickname: '玩家4', score: 0, isDrawer: false }
 ];
});
onUnmounted(() => {
 stopTimer();
});
</script>

<style scoped>
.drawing-game {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.2);
}

.back-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
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
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.game-content {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}

.players-panel {
  width: 250px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
  overflow-y: auto;
}

.players-panel h3, .players-panel h4 {
  color: white;
  margin: 0 0 12px 0;
}

.players-list {
  margin-bottom: 20px;
}

.player-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-bottom: 8px;
}

.player-item.drawer {
  background: rgba(255, 215, 0, 0.2);
  border: 1px solid rgba(255, 215, 0, 0.3);
}

.player-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  position: relative;
}

.drawer-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 12px;
}

.player-info {
  flex: 1;
}

.player-name {
  color: white;
  font-size: 14px;
}

.player-score {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.ranking-section {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 12px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 14px;
}

.rank {
  width: 24px;
}

.rank-name {
  flex: 1;
}

.rank-score {
  color: #ffd700;
}

.game-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.round-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  font-size: 16px;
}

.word-hint {
  font-weight: bold;
  color: #ffd700;
}

.canvas-container {
  flex: 1;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.game-canvas {
  display: block;
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.game-canvas.disabled {
  cursor: not-allowed;
}

.canvas-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.waiting-text {
  color: white;
  font-size: 24px;
}

.toolbar {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.tool-group {
  display: flex;
  gap: 8px;
}

.tool-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-btn:hover, .tool-btn.active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.color-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-picker {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.color-btn {
  width: 32px;
  height: 32px;
  border: 2px solid transparent;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
}

.color-btn:hover, .color-btn.active {
  border-color: white;
  transform: scale(1.1);
}

.brush-group {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
}

.brush-slider {
  width: 80px;
}

.action-group {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.guess-area {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 16px;
}

.guess-history {
  max-height: 80px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.guess-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 14px;
  margin-bottom: 4px;
}

.guess-item.correct {
  color: #4caf50;
}

.guess-player {
  font-weight: bold;
}

.guess-word {
  flex: 1;
}

.correct-mark {
  color: #4caf50;
}

.guess-input-container {
  display: flex;
  gap: 12px;
}

.guess-input {
  flex: 1;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 14px;
}

.guess-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.guess-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.guess-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  background: #4caf50;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.guess-btn:disabled {
  background: #666;
  cursor: not-allowed;
}

.timer-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.timer-label {
  color: white;
  font-size: 14px;
}

.timer-progress {
  flex: 1;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  overflow: hidden;
}

.timer-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50, #8bc34a);
  transition: width 1s linear;
}

.timer-fill.warning {
  background: linear-gradient(90deg, #ff5722, #ff9800);
}

.timer-text {
  color: white;
  font-weight: bold;
  font-size: 16px;
  min-width: 40px;
  text-align: right;
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
  background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(76, 175, 80, 0.4);
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
  text-align: center;
}

.modal-content h3 {
  color: white;
  margin: 0 0 20px 0;
  font-size: 24px;
}

.word-display {
  font-size: 32px;
  font-weight: bold;
  color: #ffd700;
  margin-bottom: 24px;
}

.confirm-btn, .next-btn, .restart-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.result-modal .result-word {
  font-size: 24px;
  color: #ffd700;
  margin-bottom: 20px;
}

.result-ranking {
  margin-bottom: 20px;
}

.result-ranking h4 {
  color: white;
  margin: 0 0 12px 0;
}

.result-item {
  color: white;
  font-size: 16px;
  margin-bottom: 8px;
}

.final-modal {
  max-width: 500px;
}

.final-winner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.winner-icon {
  font-size: 48px;
}

.winner-name {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

.winner-score {
  font-size: 24px;
  color: #ffd700;
}

.final-ranking {
  margin-bottom: 24px;
}

.final-ranking h4 {
  color: white;
  margin: 0 0 12px 0;
}

.final-item {
  color: white;
  font-size: 16px;
  margin-bottom: 8px;
}

.restart-btn {
  background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%);
}
</style>