import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useGameStore = defineStore('game', () => {
  const currentRoomId = ref<number | null>(null)
  const currentGameType = ref<'werewolf' | 'drawing' | null>(null)
  const players = ref<any[]>([])
  const gameStatus = ref<'waiting' | 'playing' | 'ended'>('waiting')

  const createRoom = async (
    gameType: 'werewolf' | 'drawing',
    roomName: string,
    password?: string,
    maxPlayers?: number
  ) => {
    const response = await fetch(`/game/${gameType}/room/create`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roomName,
        password,
        creatorId: 1,
        maxPlayers
      })
    })
    return await response.json()
  }

  const listRooms = async (gameType: 'werewolf' | 'drawing') => {
    const response = await fetch(`/game/${gameType}/room/list`)
    return await response.json()
  }

  const joinRoom = async (
    gameType: 'werewolf' | 'drawing',
    roomId: number,
    password?: string
  ) => {
    const response = await fetch(`/game/${gameType}/room/join`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roomId,
        userId: 1,
        password
      })
    })
    return await response.json()
  }

  const leaveRoom = async (
    gameType: 'werewolf' | 'drawing',
    roomId: number
  ) => {
    const response = await fetch(`/game/${gameType}/room/leave`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roomId,
        userId: 1
      })
    })
    return await response.json()
  }

  const startGame = async (
    gameType: 'werewolf' | 'drawing',
    roomId: number
  ) => {
    const response = await fetch(`/game/${gameType}/game/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId })
    })
    return await response.json()
  }

  const endGame = async (
    gameType: 'werewolf' | 'drawing',
    roomId: number
  ) => {
    const response = await fetch(`/game/${gameType}/game/end`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId })
    })
    return await response.json()
  }

  const vote = async (roomId: number, voterId: number, targetId: number) => {
    const response = await fetch('/game/werewolf/game/vote', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId, voterId, targetId })
    })
    return await response.json()
  }

  const useSkill = async (
    roomId: number,
    userId: number,
    skillType: string,
    targetId?: number,
    skillParam?: string
  ) => {
    const response = await fetch('/game/werewolf/game/skill', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId, userId, skillType, targetId, skillParam })
    })
    return await response.json()
  }

  const getWord = async (roomId: number) => {
    const response = await fetch(`/game/drawing/game/word?roomId=${roomId}`)
    return await response.json()
  }

  const guessWord = async (roomId: number, userId: number, guessWord: string) => {
    const response = await fetch('/game/drawing/game/guess', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId, userId, guessWord })
    })
    return await response.json()
  }

  const addWord = async (word: string, category?: string, difficulty?: number) => {
    const response = await fetch('/game/drawing/word/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ word, category, difficulty })
    })
    return await response.json()
  }

  const enterRoom = (roomId: number, gameType: 'werewolf' | 'drawing') => {
    currentRoomId.value = roomId
    currentGameType.value = gameType
  }

  const exitRoom = () => {
    currentRoomId.value = null
    currentGameType.value = null
    players.value = []
    gameStatus.value = 'waiting'
  }

  return {
    currentRoomId,
    currentGameType,
    players,
    gameStatus,
    createRoom,
    listRooms,
    joinRoom,
    leaveRoom,
    startGame,
    endGame,
    vote,
    useSkill,
    getWord,
    guessWord,
    addWord,
    enterRoom,
    exitRoom
  }
})