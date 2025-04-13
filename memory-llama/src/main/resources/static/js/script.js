document.addEventListener('DOMContentLoaded', () => {
    const chatBody = document.getElementById('chat-body');
    const userInput = document.getElementById('user-input');
    const sendBtn = document.getElementById('send-btn');
    const newConversationBtn = document.getElementById('new-conversation-btn');
    const conversationSelect = document.getElementById('conversation-select');
    const historyPanel = document.getElementById('history-panel');
    const historyContent = document.getElementById('history-content');
    const closeHistoryBtn = document.getElementById('close-history');

    let currentConversationId = null;

    // 加载会话列表
    async function loadConversations() {
        try {
            const response = await fetch('/api/chat/conversations');
            const conversations = await response.json();
            console.log('Loaded conversations:', conversations);
            conversationSelect.innerHTML = '<option value="">Select a conversation</option>';
            conversations.forEach(id => {
                const option = document.createElement('option');
                option.value = id;
                option.textContent = `Conversation ${id.slice(0, 8)}...`;
                conversationSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading conversations:', error);
        }
    }

    // 创建新会话
    newConversationBtn.addEventListener('click', async () => {
        try {
            const response = await fetch('/api/chat/conversations', { method: 'POST' });
            const newConversationId = await response.text();
            console.log('Created conversation:', newConversationId);
            currentConversationId = newConversationId;
            conversationSelect.value = newConversationId;
            chatBody.innerHTML = '';
            await loadConversations();
            conversationSelect.value = newConversationId;
        } catch (error) {
            console.error('Error creating conversation:', error);
        }
    });

    // 切换会话
    conversationSelect.addEventListener('change', async () => {
        currentConversationId = conversationSelect.value;
        chatBody.innerHTML = '';
        if (currentConversationId) {
            try {
                const response = await fetch(`/api/chat/${currentConversationId}/history`);
                const history = await response.json();
                console.log('Loaded history for conversation:', currentConversationId, history);
                history.forEach(msg => {
                    const div = document.createElement('div');
                    div.className = `message ${msg.role}`;
                    div.textContent = msg.content;
                    chatBody.appendChild(div);
                });
                chatBody.scrollTop = chatBody.scrollHeight;
            } catch (error) {
                console.error('Error loading history:', error);
            }
        }
    });

    // 发送消息
    sendBtn.addEventListener('click', async () => {
        const message = userInput.value.trim();
        if (!message || !currentConversationId) {
            alert('Please select or create a conversation.');
            return;
        }

        // 显示用户消息
        const userMsg = document.createElement('div');
        userMsg.className = 'message user';
        userMsg.textContent = message;
        chatBody.appendChild(userMsg);
        chatBody.scrollTop = chatBody.scrollHeight;

        // 清空输入框
        userInput.value = '';

        // 显示加载动画
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'loading';
        loadingDiv.textContent = 'AI is thinking...';
        chatBody.appendChild(loadingDiv);
        chatBody.scrollTop = chatBody.scrollHeight;

        // 显示助手消息区域
        const assistantDiv = document.createElement('div');
        assistantDiv.className = 'message assistant';
        chatBody.appendChild(assistantDiv);

        // 处理流式响应
        try {
            const response = await fetch(`/api/chat/${currentConversationId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(message)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 移除加载动画
            if (loadingDiv.parentNode) {
                chatBody.removeChild(loadingDiv);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let done = false;
            let buffer = '';
            let messageBuffer = '';

            while (!done) {
                const { value, done: streamDone } = await reader.read();
                done = streamDone;
                if (value) {
                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n\n');
                    buffer = lines.pop();
                    for (const line of lines) {
                        console.log('Received raw line:', line);
                        const dataLines = line.split('\n').filter(l => l.startsWith('data:'));
                        for (const dataLine of dataLines) {
                            const content = dataLine.slice(5).trim();
                            if (content) {
                                console.log('Processed chunk:', content);
                                messageBuffer += content;
                                assistantDiv.textContent = messageBuffer;
                                chatBody.scrollTop = chatBody.scrollHeight;
                            }
                        }
                    }
                }
            }
            console.log('Stream completed for conversation:', currentConversationId);
        } catch (error) {
            if (loadingDiv.parentNode) {
                chatBody.removeChild(loadingDiv);
            }
            console.error('Error streaming message:', error);
            assistantDiv.textContent = 'Error communicating with AI.';
            chatBody.scrollTop = chatBody.scrollHeight;
        }
    });

    // 查看历史（侧边栏）
    conversationSelect.addEventListener('click', async () => {
        if (!currentConversationId) return;
        historyContent.innerHTML = '';
        try {
            const response = await fetch(`/api/chat/${currentConversationId}/history`);
            const history = await response.json();
            console.log('Loaded history panel:', history);
            history.forEach(msg => {
                const div = document.createElement('div');
                div.className = 'history-message';
                div.textContent = `${msg.role.toUpperCase()}: ${msg.content}`;
                historyContent.appendChild(div);
            });
            historyPanel.style.display = 'flex';
        } catch (error) {
            console.error('Error loading history panel:', error);
        }
    });

    // 关闭历史面板
    closeHistoryBtn.addEventListener('click', () => {
        historyPanel.style.display = 'none';
    });

    // Enter 键发送
    userInput.addEventListener('keypress', e => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendBtn.click();
        }
    });

    // 初始加载会话
    loadConversations();
});