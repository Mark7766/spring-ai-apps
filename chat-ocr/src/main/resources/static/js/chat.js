// 全局变量
let selectedImages = [];

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // initTheme() 已经在HTML中内联调用
    initializeInputArea();
    scrollToBottom();
});

// 初始化输入区域
function initializeInputArea() {
    const messageInput = document.getElementById('messageInput');
    const fileInput = document.getElementById('fileInput');

    // 自动调整输入框高度
    messageInput.addEventListener('input', function() {
        this.style.height = 'auto';
        this.style.height = (this.scrollHeight) + 'px';
    });

    // 支持 Enter 发送，Shift+Enter 换行
    messageInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // 文件选择处理
    fileInput.addEventListener('change', function(e) {
        const files = Array.from(e.target.files);
        files.forEach(file => {
            if (file.type.startsWith('image/')) {
                addImagePreview(file);
            }
        });
        // 清空input，允许重复选择同一文件
        e.target.value = '';
    });

    // 支持粘贴图片
    messageInput.addEventListener('paste', function(e) {
        const items = e.clipboardData.items;
        for (let i = 0; i < items.length; i++) {
            if (items[i].type.indexOf('image') !== -1) {
                const file = items[i].getAsFile();
                addImagePreview(file);
            }
        }
    });
}

// 添加图片预览
function addImagePreview(file) {
    selectedImages.push(file);

    const reader = new FileReader();
    reader.onload = function(e) {
        const container = document.getElementById('imagePreviews');
        const index = selectedImages.length - 1;

        const previewItem = document.createElement('div');
        previewItem.className = 'image-preview-item';
        previewItem.innerHTML = `
            <img src="${e.target.result}" alt="预览">
            <button class="image-preview-remove" onclick="removeImage(${index})">
                <svg viewBox="0 0 24 24">
                    <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
            </button>
        `;

        container.appendChild(previewItem);
    };
    reader.readAsDataURL(file);
}

// 移除图片
function removeImage(index) {
    selectedImages.splice(index, 1);

    // 重新渲染预览
    const container = document.getElementById('imagePreviews');
    container.innerHTML = '';

    selectedImages.forEach((file, i) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const previewItem = document.createElement('div');
            previewItem.className = 'image-preview-item';
            previewItem.innerHTML = `
                <img src="${e.target.result}" alt="预览">
                <button class="image-preview-remove" onclick="removeImage(${i})">
                    <svg viewBox="0 0 24 24">
                        <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                    </svg>
                </button>
            `;
            container.appendChild(previewItem);
        };
        reader.readAsDataURL(file);
    });
}

// 发送消息
async function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const message = messageInput.value.trim();

    if (!message && selectedImages.length === 0) {
        return;
    }

    // 从 URL 中获取 sessionId
    const pathParts = window.location.pathname.split('/');
    const sessionId = pathParts[pathParts.length - 1];

    if (!sessionId || sessionId === 'chat') {
        console.error('无法获取 session ID');
        alert('无法获取会话ID，请刷新页面重试');
        return;
    }

    const sendBtn = document.getElementById('sendBtn');
    sendBtn.disabled = true;

    try {
        // 构建 FormData
        const formData = new FormData();
        formData.append('message', message);

        selectedImages.forEach(file => {
            formData.append('images', file);
        });

        // 显示用户消息
        addUserMessage(message, selectedImages);

        // 清空输入
        messageInput.value = '';
        messageInput.style.height = 'auto';
        selectedImages = [];
        document.getElementById('imagePreviews').innerHTML = '';

        // 显示加载指示器
        addTypingIndicator();

        // 发送请求
        const response = await fetch(`/api/chat/${sessionId}/message`, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        // 移除加载指示器
        removeTypingIndicator();

        // 显示AI回复
        if (data.success) {
            addAssistantMessage(data.message);
        } else {
            addErrorMessage(data.message || '发送失败，请重试');
        }

    } catch (error) {
        console.error('发送消息失败:', error);
        removeTypingIndicator();
        addErrorMessage('网络错误，请检查连接后重试');
    } finally {
        sendBtn.disabled = false;
        messageInput.focus();
    }
}

// 添加用户消息到界面
function addUserMessage(text, images) {
    const messagesArea = document.getElementById('messagesArea');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message user';

    let imagesHtml = '';
    if (images && images.length > 0) {
        const imagePromises = images.map(file => {
            return new Promise((resolve) => {
                const reader = new FileReader();
                reader.onload = (e) => resolve(e.target.result);
                reader.readAsDataURL(file);
            });
        });

        Promise.all(imagePromises).then(dataUrls => {
            imagesHtml = '<div class="message-images">';
            dataUrls.forEach(url => {
                imagesHtml += `<img src="${url}" class="message-image" onclick="viewImage(this.src)">`;
            });
            imagesHtml += '</div>';

            messageDiv.innerHTML = `
                <div class="message-content">
                    <div class="message-text">${escapeHtml(text)}</div>
                    ${imagesHtml}
                </div>
                <div class="message-time">${getCurrentTime()}</div>
            `;
        });
    } else {
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">${escapeHtml(text)}</div>
            </div>
            <div class="message-time">${getCurrentTime()}</div>
        `;
    }

    messagesArea.appendChild(messageDiv);
    scrollToBottom();
}

// 添加AI消息
function addAssistantMessage(text) {
    const messagesArea = document.getElementById('messagesArea');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message assistant';

    messageDiv.innerHTML = `
        <div class="message-content">
            <div class="message-text">${escapeHtml(text).replace(/\n/g, '<br>')}</div>
        </div>
        <div class="message-time">${getCurrentTime()}</div>
    `;

    messagesArea.appendChild(messageDiv);
    scrollToBottom();
}

// 添加错误消息
function addErrorMessage(text) {
    const messagesArea = document.getElementById('messagesArea');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message assistant';
    messageDiv.style.color = '#f4212e';

    messageDiv.innerHTML = `
        <div class="message-content">
            <div class="message-text">❌ ${escapeHtml(text)}</div>
        </div>
        <div class="message-time">${getCurrentTime()}</div>
    `;

    messagesArea.appendChild(messageDiv);
    scrollToBottom();
}

// 添加加载指示器
function addTypingIndicator() {
    const messagesArea = document.getElementById('messagesArea');
    const indicator = document.createElement('div');
    indicator.className = 'message assistant';
    indicator.id = 'typingIndicator';
    indicator.innerHTML = `
        <div class="typing-indicator">
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
        </div>
    `;
    messagesArea.appendChild(indicator);
    scrollToBottom();
}

// 移除加载指示器
function removeTypingIndicator() {
    const indicator = document.getElementById('typingIndicator');
    if (indicator) {
        indicator.remove();
    }
}

// 滚动到底部
function scrollToBottom() {
    const messagesArea = document.getElementById('messagesArea');
    messagesArea.scrollTop = messagesArea.scrollHeight;
}

// 获取当前时间
function getCurrentTime() {
    const now = new Date();
    return now.getHours().toString().padStart(2, '0') + ':' +
           now.getMinutes().toString().padStart(2, '0');
}

// HTML转义
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 快捷操作
function quickAction(action) {
    const messageInput = document.getElementById('messageInput');
    messageInput.value = action;
    messageInput.focus();
}

// 快捷操作处理器（从data属性读取）
function quickActionHandler(element) {
    const action = element.getAttribute('data-action');
    quickAction(action);
}

// 导航到指定会话
function navigateToSession(element) {
    const sessionId = element.getAttribute('data-session-id');
    location.href = '/chat/' + sessionId;
}

// 创建新会话
function createNewSession() {
    const scenario = document.getElementById('scenarioSelect').value;
    window.location.href = `/new?scenario=${scenario}`;
}

// 切换场景
function changeScenario(scenario) {
    // 创建自定义对话框
    const dialog = document.createElement('div');
    dialog.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
    `;

    dialog.innerHTML = `
        <div style="
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 24px;
            max-width: 400px;
            width: 90%;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
        ">
            <h3 style="
                margin: 0 0 16px 0;
                color: var(--text-primary);
                font-size: 18px;
                font-weight: 600;
            ">切换场景</h3>
            <p style="
                margin: 0 0 24px 0;
                color: var(--text-secondary);
                font-size: 14px;
                line-height: 1.5;
            ">请选择切换方式：</p>
            <div style="display: flex; flex-direction: column; gap: 12px;">
                <button id="createNewBtn" style="
                    padding: 12px 20px;
                    background: var(--button-bg);
                    color: var(--text-inverse);
                    border: none;
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s;
                ">确定并新增会话</button>
                <button id="switchOnlyBtn" style="
                    padding: 12px 20px;
                    background: var(--bg-tertiary);
                    color: var(--text-primary);
                    border: 1px solid var(--border-color);
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s;
                ">仅切换场景（不创建会话）</button>
                <button id="cancelBtn" style="
                    padding: 12px 20px;
                    background: transparent;
                    color: var(--text-secondary);
                    border: 1px solid var(--border-color);
                    border-radius: 8px;
                    font-size: 14px;
                    cursor: pointer;
                    transition: all 0.2s;
                ">取消</button>
            </div>
        </div>
    `;

    document.body.appendChild(dialog);

    // 添加悬停效果
    const createNewBtn = dialog.querySelector('#createNewBtn');
    const switchOnlyBtn = dialog.querySelector('#switchOnlyBtn');
    const cancelBtn = dialog.querySelector('#cancelBtn');

    createNewBtn.addEventListener('mouseenter', function() {
        this.style.background = 'var(--button-hover)';
    });
    createNewBtn.addEventListener('mouseleave', function() {
        this.style.background = 'var(--button-bg)';
    });

    switchOnlyBtn.addEventListener('mouseenter', function() {
        this.style.background = 'var(--bg-hover)';
    });
    switchOnlyBtn.addEventListener('mouseleave', function() {
        this.style.background = 'var(--bg-tertiary)';
    });

    cancelBtn.addEventListener('mouseenter', function() {
        this.style.background = 'var(--bg-hover)';
    });
    cancelBtn.addEventListener('mouseleave', function() {
        this.style.background = 'transparent';
    });

    // 确定并新增会话
    createNewBtn.addEventListener('click', function() {
        document.body.removeChild(dialog);
        window.location.href = `/new?scenario=${scenario}`;
    });

    // 仅切换场景
    switchOnlyBtn.addEventListener('click', function() {
        document.body.removeChild(dialog);
        const select = document.getElementById('scenarioSelect');
        select.value = scenario;
        // 这里只是更新下拉框的值，不创建会话，也不跳转
        // 用户可以继续在当前会话中使用，下次创建新会话时会使用新场景
    });

    // 取消
    cancelBtn.addEventListener('click', function() {
        document.body.removeChild(dialog);
        // 恢复原来的选择
        const select = document.getElementById('scenarioSelect');
        const originalScenario = select.getAttribute('data-original-scenario');
        if (originalScenario) {
            select.value = originalScenario;
        }
    });

    // 点击背景关闭
    dialog.addEventListener('click', function(e) {
        if (e.target === dialog) {
            document.body.removeChild(dialog);
            const select = document.getElementById('scenarioSelect');
            const originalScenario = select.getAttribute('data-original-scenario');
            if (originalScenario) {
                select.value = originalScenario;
            }
        }
    });
}

// 查看图片
function viewImage(src) {
    const viewer = document.getElementById('imageViewer');
    viewer.innerHTML = `
        <img src="${src}" style="width: 100%; border-radius: 8px;" alt="查看图片">
        <div style="margin-top: 12px; text-align: center;">
            <button class="btn-toolbar" onclick="clearImageViewer()">关闭</button>
        </div>
    `;
}

// 清空图片查看器
function clearImageViewer() {
    const viewer = document.getElementById('imageViewer');
    viewer.innerHTML = `
        <div class="empty-state">
            <svg viewBox="0 0 24 24" width="48" height="48">
                <path fill="currentColor" opacity="0.3" d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
            </svg>
            <p>暂无图片</p>
        </div>
    `;
}

// 导出报告
async function exportReport() {
    try {
        const response = await fetch(`/api/chat/${SESSION_ID}/messages`);
        const data = await response.json();

        if (data.success) {
            // 简单的文本导出
            let report = `对话记录导出\n`;
            report += `================\n\n`;

            data.messages.forEach(msg => {
                const role = msg.role === 'USER' ? '用户' : 'AI';
                const time = new Date(msg.timestamp).toLocaleString('zh-CN');
                report += `[${role}] ${time}\n${msg.content}\n\n`;
            });

            // 下载文件
            const blob = new Blob([report], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `chat-report-${SESSION_ID}.txt`;
            a.click();
            URL.revokeObjectURL(url);
        }
    } catch (error) {
        console.error('导出失败:', error);
        alert('导出失败，请重试');
    }
}

// 删除会话
async function deleteSession() {
    if (!confirm('确定要删除当前会话吗？此操作不可恢复。')) {
        return;
    }

    try {
        const response = await fetch(`/api/sessions/${SESSION_ID}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            window.location.href = '/';
        } else {
            alert('删除失败: ' + data.message);
        }
    } catch (error) {
        console.error('删除失败:', error);
        alert('删除失败，请重试');
    }
}

// 从会话列表删除会话
async function deleteSessionFromList(event, sessionId) {
    event.stopPropagation(); // 防止触发导航

    if (!confirm('确定要删除此会话吗？此操作不可恢复。')) {
        return;
    }

    try {
        const response = await fetch(`/api/sessions/${sessionId}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (data.success) {
            // 如果删除的是当前会话，跳转到首页
            if (sessionId === SESSION_ID) {
                window.location.href = '/';
            } else {
                // 否则刷新当前页面以更新会话列表
                window.location.reload();
            }
        } else {
            alert('删除失败: ' + data.message);
        }
    } catch (error) {
        console.error('删除失败:', error);
        alert('删除失败，请重试');
    }
}

