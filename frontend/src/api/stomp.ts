import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_BASE = import.meta.env.DEV
  ? `${location.protocol}//${location.host}/ws`
  : `${location.origin}/api/ws`

export function createStompClient() {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_BASE) as any,
    reconnectDelay: 3000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })
  return client
}
