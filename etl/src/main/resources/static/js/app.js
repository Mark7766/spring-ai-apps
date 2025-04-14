document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chatForm');
    const questionInput = document.getElementById('questionInput');
    const chatHistory = document.getElementById('chatHistory');
    const uploadForm = document.getElementById('uploadForm');
    const fileInput = document.getElementById('fileInput');
    const uploadButton = document.getElementById('uploadButton');
    const uploadLoading = document.getElementById('uploadLoading');
    const fileList = document.getElementById('fileList');
    const uploadMessage = document.getElementById('uploadMessage');

    // 格式化时间
    function formatTime(date) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    // 添加对话消息
    function addMessage(text, isUser) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isUser ? 'message-user' : 'message-ai'}`;
        messageDiv.innerHTML = `
            <div class="message-content">
                <p>${text}</p>
                <span class="message-time">${formatTime(new Date())}</span>
            </div>
        `;
        chatHistory.appendChild(messageDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight; // 滚动到底部
    }

    // 聊天表单提交
    if (chatForm) {
        chatForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const question = questionInput.value.trim();
            if (!question) return;

            // 添加用户消息
            addMessage(question, true);

            try {
                const response = await fetch('/api/documents/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(question) // 发送字符串
                });
                const text = await response.text(); // 解析为文本
                if (response.ok) {
                    // 添加 AI 回复
                    addMessage(text || '抱歉，我暂时无法回答，请稍后再试。', false);
                } else {
                    addMessage('错误: ' + text, false);
                }
            } catch (error) {
                addMessage('网络错误，请稍后重试', false);
                console.error('Chat error:', error);
            }

            // 清空输入框
            questionInput.value = '';
        });
    }

    // 上传表单提交
    if (uploadForm) {
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (uploadButton.disabled) return; // 防止重复提交

            // 显示加载动画，禁用按钮
            uploadButton.disabled = true;
            uploadLoading.classList.remove('d-none');
            uploadMessage.textContent = ''; // 清空提示

            const formData = new FormData(uploadForm);
            try {
                const response = await fetch('/api/documents/upload', {
                    method: 'POST',
                    body: formData
                });
                const text = await response.text(); // 解析为文本
                uploadMessage.className = 'mt-3 text-' + (response.ok ? 'success' : 'danger');
                uploadMessage.textContent = text || (response.ok ? '上传成功' : '上传失败');
                if (response.ok) {
                    loadFileList();
                    uploadForm.reset();
                }
            } catch (error) {
                uploadMessage.className = 'mt-3 text-danger';
                uploadMessage.textContent = '上传失败: 网络错误';
                console.error('Upload error:', error);
            } finally {
                // 隐藏加载动画，恢复按钮
                uploadButton.disabled = false;
                uploadLoading.classList.add('d-none');
            }
        });
    }

    // 加载文件列表
    async function loadFileList() {
        try {
            const response = await fetch('/api/documents/list');
            const files = await response.json(); // 解析为 JSON
            fileList.innerHTML = '';
            if (files.length === 0) {
                fileList.innerHTML = '<tr><td colspan="2" class="text-center">暂无文档</td></tr>';
                return;
            }
            files.forEach(file => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${file}</td>
                    <td><button class="btn btn-danger btn-sm delete-btn" data-file="${file}">删除</button></td>
                `;
                fileList.appendChild(tr);
            });

            // 绑定删除按钮
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const fileName = btn.dataset.file;
                    try {
                        const response = await fetch(`/api/documents/delete/${encodeURIComponent(fileName)}`, {
                            method: 'DELETE'
                        });
                        const text = await response.text(); // 解析为文本
                        uploadMessage.className = 'mt-3 text-' + (response.ok ? 'success' : 'danger');
                        uploadMessage.textContent = text || (response.ok ? '删除成功' : '删除失败');
                        if (response.ok) {
                            loadFileList();
                        }
                    } catch (error) {
                        uploadMessage.className = 'mt-3 text-danger';
                        uploadMessage.textContent = '删除失败: 网络错误';
                        console.error('Delete error:', error);
                    }
                });
            });
        } catch (error) {
            uploadMessage.className = 'mt-3 text-danger';
            uploadMessage.textContent = '加载文件列表失败';
            console.error('Load files error:', error);
        }
    }

    // 初始化文件列表
    if (fileList) {
        loadFileList();
    }
});