<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <div
      v-if="!isOpen"
      class="assistant-button"
      ref="assistantButton"
      @click.stop="toggleChat"
    >
      <icon-robot class="robot-icon" />
      <span>AI助手</span>
    </div>

    <!-- 聊天窗口 -->
    <div v-else class="chat-window" ref="chatWindow">
      <div class="chat-header">
        <div class="header-title">
          <icon-robot class="robot-icon" />
          <span>AI助手</span>
        </div>
        <icon-close class="close-icon" @click.stop="toggleChat" />
      </div>

      <div class="chat-messages" ref="messageContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="[
            'message',
            message.type === 'user' ? 'user-message' : 'ai-message',
          ]"
        >
          <div class="message-content">{{ message.content }}</div>
        </div>
      </div>

      <div class="chat-input">
        <a-textarea
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          :rows="3"
          @keydown.enter.prevent="sendMessage"
        />
        <a-button type="primary" @click="sendMessage" :loading="loading">
          发送
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconRobot, IconClose } from "@arco-design/web-vue/es/icon";
import { useStore } from "vuex";

const store = useStore();
const isOpen = ref(false);
const inputMessage = ref("");
const loading = ref(false);
const messageContainer = ref<HTMLElement | null>(null);
const chatWindow = ref<HTMLElement | null>(null);
const assistantButton = ref<HTMLElement | null>(null);

// 从 Vuex 获取当前用户的对话记录
const messages = computed(() => {
  const userId = store.state.user.loginUser?.id;
  return userId ? store.state.user.aiMessages[userId] || [] : [];
});

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim()) return;

  const userMessage = inputMessage.value;
  const userId = store.state.user.loginUser?.id;
  if (!userId) {
    Message.error("请先登录");
    return;
  }

  // 添加用户消息
  store.dispatch("user/addAiMessage", {
    userId,
    message: {
      type: "user",
      content: userMessage,
    },
  });

  // 添加一个空的AI消息用于流式更新
  store.dispatch("user/addAiMessage", {
    userId,
    message: {
      type: "ai",
      content: "",
    },
  });

  inputMessage.value = "";
  loading.value = true;

  const apiKey = "sk-4962bb75201c45959f53b3bda82689d7";
  const appId = "9c28e76f4d1a40ea8351a27d0c98d23a";

  const url = `https://dashscope.aliyuncs.com/api/v1/apps/${appId}/completion`;

  const data = {
    input: {
      prompt: userMessage,
    },
    parameters: {
      incremental_output: "true",
    },
    debug: {},
  };

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "X-DashScope-SSE": "enable",
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error("No reader available");
    }

    const decoder = new TextDecoder();
    let aiMessage = "";

    // eslint-disable-next-line no-constant-condition
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value);
      const lines = chunk.split("\n");

      for (const line of lines) {
        if (line.startsWith("data:")) {
          try {
            const jsonData = JSON.parse(line.slice(5));
            if (jsonData.output && jsonData.output.text) {
              aiMessage += jsonData.output.text;
              // 更新最后一条AI消息的内容
              const messages = store.state.user.aiMessages[userId];
              if (messages && messages.length > 0) {
                messages[messages.length - 1].content = aiMessage;
                // 滚动到底部
                nextTick(() => {
                  scrollToBottom();
                });
              }
            }
          } catch (e) {
            console.error("Error parsing chunk:", e);
          }
        }
      }
    }
  } catch (error: any) {
    console.error("Error calling DashScope:", error);
    Message.error("网络错误，请稍后重试");
    // 移除空的AI消息
    const messages = store.state.user.aiMessages[userId];
    if (messages && messages.length > 0) {
      messages.pop();
    }
  } finally {
    loading.value = false;
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

// 处理点击事件
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Node;
  const isClickingAssistantButton = assistantButton.value?.contains(target);
  const isClickingChatWindow = chatWindow.value?.contains(target);

  if (isOpen.value && !isClickingAssistantButton && !isClickingChatWindow) {
    toggleChat();
  }
};

// 添加和移除点击事件监听器
onMounted(() => {
  const userId = store.state.user.loginUser?.id;
  if (userId) {
    // 如果用户没有对话记录，添加欢迎消息
    if (
      !store.state.user.aiMessages[userId] ||
      store.state.user.aiMessages[userId].length === 0
    ) {
      store.dispatch("user/addAiMessage", {
        userId,
        message: {
          type: "ai",
          content: "你好！我是AI助手，有什么可以帮你的吗？",
        },
      });
    }
  }
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
}

.assistant-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background-color: #165dff;
  color: white;
  border-radius: 24px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.assistant-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.chat-window {
  width: 360px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.close-icon {
  cursor: pointer;
  color: #666;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.message {
  margin-bottom: 16px;
  max-width: 80%;
}

.user-message {
  margin-left: auto;
}

.ai-message {
  margin-right: auto;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f5f5;
}

.user-message .message-content {
  background: #165dff;
  color: white;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
}

.robot-icon {
  font-size: 20px;
}
</style>
