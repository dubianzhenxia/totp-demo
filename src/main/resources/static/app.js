/**
 * TOTP验证程序前端JavaScript
 * 负责与后端API交互和页面逻辑处理
 */

// 当前配置信息
let currentConfig = null;

/**
 * 生成TOTP配置
 */
async function generateTOTP() {
    const accountName = document.getElementById('accountName').value.trim();
    const issuer = document.getElementById('issuer').value.trim();
    
    if (!accountName || !issuer) {
        alert('请输入账户名称和发行者');
        return;
    }
    
    // 显示加载动画
    showLoading('generateLoading');
    hideElement('configResult');
    
    try {
        // 构建表单数据
        const formData = new FormData();
        formData.append('accountName', accountName);
        formData.append('issuer', issuer);
        
        // 发送API请求
        const response = await fetch('/api/generate', {
            method: 'POST',
            body: new URLSearchParams({
                accountName: accountName,
                issuer: issuer
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            // 保存配置信息
            currentConfig = result.data;
            
            // 更新页面显示
            updateConfigDisplay(result.data);
            showElement('configResult');
            
            // 自动刷新验证码
            refreshCode();
            
            console.log('TOTP配置生成成功:', result.data);
        } else {
            throw new Error(result.message);
        }
        
    } catch (error) {
        console.error('生成TOTP配置失败:', error);
        alert('生成TOTP配置失败: ' + error.message);
    } finally {
        hideLoading('generateLoading');
    }
}

/**
 * 验证TOTP验证码
 */
async function verifyTOTP() {
    const verifyCode = document.getElementById('verifyCode').value.trim();
    
    if (!verifyCode) {
        alert('请输入验证码');
        return;
    }
    
    if (!/^\d{6}$/.test(verifyCode)) {
        alert('请输入6位数字验证码');
        return;
    }
    
    if (!currentConfig) {
        alert('请先生成TOTP配置');
        return;
    }
    
    // 显示加载动画
    showLoading('verifyLoading');
    hideElement('verifyResult');
    
    try {
        // 发送验证请求
        const response = await fetch('/api/verify', {
            method: 'POST',
            body: new URLSearchParams({
                code: verifyCode
            })
        });
        
        const result = await response.json();
        
        // 更新验证结果显示
        updateVerifyResult(result);
        showElement('verifyResult');
        
        console.log('验证结果:', result);
        
    } catch (error) {
        console.error('验证TOTP验证码失败:', error);
        alert('验证失败: ' + error.message);
    } finally {
        hideLoading('verifyLoading');
    }
}

/**
 * 刷新当前验证码
 */
async function refreshCode() {
    if (!currentConfig) {
        alert('请先生成TOTP配置');
        return;
    }
    
    try {
        const response = await fetch('/api/current-code');
        const result = await response.json();
        
        if (result.success) {
            // 更新验证码显示
            document.getElementById('currentCode').textContent = result.data.currentCode;
            
            // 更新当前配置中的验证码
            if (currentConfig) {
                currentConfig.currentCode = result.data.currentCode;
            }
            
            console.log('验证码已刷新:', result.data.currentCode);
        } else {
            throw new Error(result.message);
        }
        
    } catch (error) {
        console.error('刷新验证码失败:', error);
        alert('刷新验证码失败: ' + error.message);
    }
}

/**
 * 更新配置信息显示
 */
function updateConfigDisplay(configData) {
    // 显示配置信息
    document.getElementById('configInfo').textContent = configData.configInfo;
    
    // 显示二维码
    document.getElementById('qrCodeImage').src = configData.qrCodeImage;
    
    // 显示当前验证码
    document.getElementById('currentCode').textContent = configData.currentCode;
}

/**
 * 更新验证结果显示
 */
function updateVerifyResult(result) {
    const resultBox = document.getElementById('verifyResultBox');
    
    // 清空之前的样式和内容
    resultBox.className = 'result-box';
    
    if (result.success) {
        if (result.data.isValid) {
            // 验证成功
            resultBox.className += ' result-success';
            resultBox.innerHTML = `
                ✅ <strong>验证成功！</strong><br>
                您输入的验证码 <strong>${result.data.userCode}</strong> 是正确的。<br>
                <small>当前验证码: ${result.data.expectedCode}</small>
            `;
        } else {
            // 验证失败
            resultBox.className += ' result-error';
            resultBox.innerHTML = `
                ❌ <strong>验证失败！</strong><br>
                您输入的验证码 <strong>${result.data.userCode}</strong> 不正确。<br>
                <small>当前验证码: ${result.data.expectedCode}</small>
            `;
        }
    } else {
        // API调用失败
        resultBox.className += ' result-error';
        resultBox.innerHTML = `
            ⚠️ <strong>验证出错！</strong><br>
            ${result.message}
        `;
    }
}

/**
 * 显示加载动画
 */
function showLoading(loadingId) {
    document.getElementById(loadingId).style.display = 'block';
}

/**
 * 隐藏加载动画
 */
function hideLoading(loadingId) {
    document.getElementById(loadingId).style.display = 'none';
}

/**
 * 显示元素
 */
function showElement(elementId) {
    document.getElementById(elementId).classList.remove('hidden');
}

/**
 * 隐藏元素
 */
function hideElement(elementId) {
    document.getElementById(elementId).classList.add('hidden');
}

/**
 * 自动刷新验证码（每25秒刷新一次）
 */
function startAutoRefresh() {
    if (currentConfig) {
        setInterval(refreshCode, 25000); // 25秒刷新一次
    }
}

/**
 * 输入框回车键支持
 */
document.addEventListener('DOMContentLoaded', function() {
    // 账户名称输入框回车触发生成
    document.getElementById('accountName').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            generateTOTP();
        }
    });
    
    // 发行者输入框回车触发生成
    document.getElementById('issuer').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            generateTOTP();
        }
    });
    
    // 验证码输入框回车触发验证
    document.getElementById('verifyCode').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            verifyTOTP();
        }
    });
    
    // 限制验证码输入为数字
    document.getElementById('verifyCode').addEventListener('input', function(e) {
        this.value = this.value.replace(/[^0-9]/g, '');
        if (this.value.length > 6) {
            this.value = this.value.slice(0, 6);
        }
    });
    
    // 页面加载完成后自动生成一个默认配置
    setTimeout(() => {
        if (!currentConfig) {
            console.log('页面加载完成，准备生成默认TOTP配置...');
            // 可以取消注释下面的行来自动生成配置
            // generateTOTP();
        }
    }, 1000);
});

/**
 * 工具函数：格式化时间
 */
function formatTime(date) {
    return date.toLocaleTimeString('zh-CN', { 
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * 工具函数：显示通知
 */
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: bold;
        z-index: 1000;
        opacity: 0;
        transition: opacity 0.3s;
        ${type === 'success' ? 'background: #28a745;' : 
          type === 'error' ? 'background: #dc3545;' : 
          'background: #17a2b8;'}
    `;
    notification.textContent = message;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 显示动画
    setTimeout(() => {
        notification.style.opacity = '1';
    }, 100);
    
    // 3秒后自动移除
    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// 导出函数供全局使用
window.generateTOTP = generateTOTP;
window.verifyTOTP = verifyTOTP;
window.refreshCode = refreshCode;